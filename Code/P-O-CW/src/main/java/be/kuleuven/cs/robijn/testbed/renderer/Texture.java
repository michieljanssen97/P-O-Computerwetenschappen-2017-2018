package be.kuleuven.cs.robijn.testbed.renderer;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

public class Texture implements AutoCloseable {
    private final int textureId, width, height;
    private Vector2D textureScale = new Vector2D(1, 1);

    public static Texture load(BufferedImage img){
        //Copy pixel values to array of integers
        int[] pixels = new int[img.getWidth() * img.getHeight()];
        img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());

        //Copy pixels to native buffer
        IntBuffer buffer = BufferUtils.createIntBuffer(img.getWidth() * img.getHeight());
        buffer.rewind();
        buffer.put(pixels);
        buffer.rewind();

        //Create new OpenGL texture
        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        //Load image data into texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, img.getWidth(), img.getHeight(), 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);

        return new Texture(textureId, img.getWidth(), img.getHeight());
    }

    private Texture(int textureId, int width, int height){
        this.textureId = textureId;
        this.width = width;
        this.height = height;
    }

    public int getTextureId(){
        return this.textureId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setTextureScale(Vector2D textureScale) {
        if(textureScale == null){
            throw new IllegalArgumentException("textureScale cannot be null");
        }

        this.textureScale = textureScale;
    }

    public Vector2D getTextureScale() {
        return textureScale;
    }

    @Override
    public void close() {
        glDeleteTextures(textureId);
    }
}
