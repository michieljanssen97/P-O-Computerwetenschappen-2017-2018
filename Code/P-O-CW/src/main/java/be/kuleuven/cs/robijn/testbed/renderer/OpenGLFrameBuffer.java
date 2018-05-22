package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.FrameBuffer;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL32.*;

public class OpenGLFrameBuffer implements FrameBuffer {
    public static OpenGLFrameBuffer create(int width, int height){
        return create(width, height, false, true);
    }

    public static OpenGLFrameBuffer create(Texture texture, boolean createDepthBuffer){
        if(texture.getDimensions() != 2){
            throw new IllegalArgumentException("Can only create framebuffer for a 2D texture");
        }

        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        int depthRenderBuffer = -1;
        if(createDepthBuffer){
            depthRenderBuffer = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, depthRenderBuffer);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, texture.getSize(0), texture.getSize(1));
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderBuffer);
        }

        //Set texture as GL_COLOR_ATTACHMENT0
        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture.getTextureId(), 0);
        //Render to GL_COLOR_ATTACHMENT0
        glDrawBuffer(GL_COLOR_ATTACHMENT0);

        int framebufferStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (framebufferStatus != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Frame buffer incomplete! (errorcode = "+framebufferStatus+")");
        }

        return new OpenGLFrameBuffer(fbo, -1, depthRenderBuffer, texture.getSize(0), texture.getSize(1), texture.getBytesPerPixel());
    }

    public static OpenGLFrameBuffer create(int width, int height, boolean greyscale, boolean createDepthBuffer){
        if(width <= 0 || height <= 0){
            throw new IllegalArgumentException("Invalid framebuffer width or height");
        }

        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        int renderBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);

        int bytesPerPixel;
        if(greyscale){
            bytesPerPixel = 1; //Only red channel
            glRenderbufferStorage(GL_RENDERBUFFER, GL_R8, width, height);
        }else{
            //Render as RGB, read as BGR. See OpenGLRenderer.createProjectionMatrix() for an explanation.
            bytesPerPixel = 3; //Because RGB
            glRenderbufferStorage(GL_RENDERBUFFER, GL_RGB8, width, height);
        }
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, renderBuffer);

        int depthRenderBuffer = -1;
        if(createDepthBuffer){
            depthRenderBuffer = glGenRenderbuffers();
            glBindRenderbuffer(GL_RENDERBUFFER, depthRenderBuffer);
            glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
            glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderBuffer);
        }

        int framebufferStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (framebufferStatus != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Frame buffer incomplete! (errorcode = "+framebufferStatus+")");
        }

        return new OpenGLFrameBuffer(fbo, renderBuffer, depthRenderBuffer, width, height, bytesPerPixel);
    }

    private int frameBufferId;
    private int renderBufferId;
    private int depthBufferId;
    private int width;
    private int height;
    private int bytesPerPixel;
    private ByteBuffer buf;

    private OpenGLRenderTask lastRenderTask;
    private boolean isClosed = false;

    private OpenGLFrameBuffer(int frameBufferId, int renderBufferId, int depthBufferId, int width, int height, int bytesPerPixel){
        this.frameBufferId = frameBufferId;
        this.renderBufferId = renderBufferId;
        this.depthBufferId = depthBufferId;
        this.width = width;
        this.height = height;
        this.bytesPerPixel = bytesPerPixel;
    }

    public int getId(){
        return frameBufferId;
    }

    public int getRenderBufferId() {
        return renderBufferId;
    }

    public int getDepthBufferId() {
        return depthBufferId;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    void setCurrentRenderTask(OpenGLRenderTask task){
        this.lastRenderTask = task;
    }

    public boolean isReady(){
        return lastRenderTask == null || lastRenderTask.isDone();
    }

    @Override
    public Future<byte[]> readPixels(byte[] data) {
        if(this.isClosed){
            throw new IllegalStateException("Cannot read pixels from a closed framebuffer!");
        }

        int dataLength = width*height*bytesPerPixel;
        if(data == null){
            data = new byte[dataLength];
        }else if(data.length < dataLength){
            throw new IllegalArgumentException("target buffer is too small");
        }

        if(buf == null){
            buf = BufferUtils.createByteBuffer(bytesPerPixel*width*height);
        }

        //Wait until GPU has finished rendering
        if(lastRenderTask != null){
            lastRenderTask.waitUntilFinished();
        }

        //Read from this buffer
        glBindFramebuffer(GL_FRAMEBUFFER, this.getId());

        //Make sure we write to the begin of the buffer
        buf.position(0);
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        //Read frame from GPU to RAM
        //Render as RGB, read as BGR. See OpenGLRenderer.createProjectionMatrix() for an explanation.
        glReadPixels(0, 0, width, height, GL_BGR, GL_UNSIGNED_BYTE, buf); //TODO: can't handle special bpp counts

        //Move cursor in buffer back to the begin and copy the contents to the java image
        buf.position(0);
        buf.get(data);

        return CompletableFuture.completedFuture(data);
    }

    public float readDepth(int x, int y){
        if(this.isClosed){
            throw new IllegalStateException("Cannot read pixels from a closed framebuffer!");
        }

        if(depthBufferId == -1){
            throw new IllegalStateException("This framebuffer has no depth buffer");
        }

        if(x < 0 || y < 0 || x >= getWidth() || y >= getHeight()){
            throw new IllegalArgumentException("Pixel position is out of bounds");
        }

        //Wait until GPU has finished rendering
        if(lastRenderTask != null){
            lastRenderTask.waitUntilFinished();
        }

        //Read from this buffer
        glBindFramebuffer(GL_FRAMEBUFFER, this.getId());

        //No padding
        glPixelStorei(GL_PACK_ALIGNMENT, 1);

        //Read frame from GPU to RAM
        float[] value = new float[1];
        glReadPixels(x, y, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT, value);
        return value[0];
    }

    @Override
    public void close() {
        if(this.isClosed){
            return;
        }

        //Cleanup LWJGL resources
        glDeleteRenderbuffers(renderBufferId);
        if(depthBufferId != -1){
            glDeleteRenderbuffers(depthBufferId);
        }
        glDeleteFramebuffers(frameBufferId);

        this.buf = null;
        this.isClosed = true;
    }
}
