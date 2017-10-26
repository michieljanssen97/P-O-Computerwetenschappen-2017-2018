package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.FrameBuffer;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGR;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL32.*;

public class OpenGLFrameBuffer implements FrameBuffer {
    public static OpenGLFrameBuffer create(int width, int height){
        if(width <= 0 || height <= 0){
            throw new IllegalArgumentException("Invalid framebuffer width or height");
        }

        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        int renderBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_RGB8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, renderBuffer);

        int depthRenderBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, depthRenderBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthRenderBuffer);

        int framebufferStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (framebufferStatus != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Frame buffer incomplete! (errorcode = "+framebufferStatus+")");
        }

        int bytesPerPixel = 3; //Because RGB
        ByteBuffer buf = BufferUtils.createByteBuffer(bytesPerPixel*width*height);

        return new OpenGLFrameBuffer(fbo, renderBuffer, width, height, bytesPerPixel, buf);
    }

    private int frameBufferId;
    private int renderBufferId;
    private int width;
    private int height;
    private int bytesPerPixel;
    private ByteBuffer buf;

    private OpenGLFrameBuffer(int frameBufferId, int renderBufferId, int width, int height, int bytesPerPixel, ByteBuffer buffer){
        if(buffer.capacity() < width*height*bytesPerPixel){
            throw new IllegalArgumentException("buffer is too small");
        }

        this.frameBufferId = frameBufferId;
        this.renderBufferId = renderBufferId;
        this.width = width;
        this.height = height;
        this.bytesPerPixel = bytesPerPixel;
        this.buf = buffer;
    }

    public int getId(){
        return frameBufferId;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    @Override
    public void readPixels(byte[] data) {
        if(data.length < width*height*bytesPerPixel){
            throw new IllegalArgumentException("target buffer is too small");
        }

        //Wait until GPU has finished rendering triangle
        long sync = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
        int waitResult;
        do {
            waitResult = glClientWaitSync(sync, GL_SYNC_FLUSH_COMMANDS_BIT, 2000);
        } while(waitResult == GL_TIMEOUT_EXPIRED);

        if(waitResult != GL_CONDITION_SATISFIED && waitResult != GL_ALREADY_SIGNALED){
            throw new RuntimeException("waiting for gpu failed: "+waitResult);
        }

        //Make sure we write to the begin of the buffer
        buf.position(0);
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        //Read frame from GPU to RAM
        glReadPixels(0, 0, width, height, GL_BGR, GL_UNSIGNED_BYTE, buf);

        //Move cursor in buffer back to the begin and copy the contents to the java image
        buf.position(0);
        buf.get(data);
    }

    @Override
    public void close() {
        //Cleanup LWJGL resources
        glDeleteRenderbuffers(frameBufferId);
        glDeleteFramebuffers(renderBufferId);
    }
}
