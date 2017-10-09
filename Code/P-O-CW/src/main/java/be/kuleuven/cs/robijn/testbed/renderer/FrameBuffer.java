package be.kuleuven.cs.robijn.testbed.renderer;

import org.lwjgl.BufferUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL32.*;

public class FrameBuffer implements Closeable {
    public static FrameBuffer create(int width, int height){
        int fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        int renderBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_RGB8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, renderBuffer);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Frame buffer incomplete!");
        }

        int bytesPerPixel = 3; //Because RGB
        ByteBuffer buf = BufferUtils.createByteBuffer(bytesPerPixel*width*height);

        return new FrameBuffer(fbo, renderBuffer, width, height, bytesPerPixel, buf);
    }

    private int frameBufferId;
    private int renderBufferId;
    private int width;
    private int height;
    private int bytesPerPixel;
    private ByteBuffer buf;

    private FrameBuffer(int frameBufferId, int renderBufferId, int width, int height, int bytesPerPixel, ByteBuffer buffer){
        this.frameBufferId = frameBufferId;
        this.renderBufferId = renderBufferId;
        this.width = width;
        this.height = height;
        this.bytesPerPixel = bytesPerPixel;
        this.buf = buffer;
    }

    public void read(byte[] targetBuffer){
        if(targetBuffer.length < width * height * bytesPerPixel){
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
        //Read frame from GPU to RAM
        glReadPixels(0, 0, width, height, GL_RGB, GL_UNSIGNED_BYTE, buf);

        //Move cursor in buffer back to the begin and copy the contents to the java image
        buf.position(0);
        buf.get(targetBuffer);
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
    public void close() throws IOException {
        //Cleanup LWJGL resources
        glDeleteRenderbuffers(frameBufferId);
        glDeleteFramebuffers(renderBufferId);
    }
}
