package be.kuleuven.cs.robijn.testbed.renderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture implements AutoCloseable {
    private final int textureId;

    public static Texture load(BufferedImage img){
        //Copy pixel values to array of integers
        int[] pixels = new int[img.getWidth() * img.getHeight()];
        img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());

        //Create native buffer and load the pixel values into the buffer.
        ByteBuffer buffer = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 3); //3 because RGB
        for(int x = 0; x < img.getWidth(); x++){
            for(int y = 0; y < img.getHeight(); y++){
                int pixel = pixels[y * img.getWidth() + x];
                //Get RGB values and append to buffer
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
            }
        }
        buffer.flip();

        //Create new OpenGL texture
        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        //Load image data into texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, img.getWidth(), img.getHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE, buffer);

        return new Texture(textureId);
    }

    private Texture(int textureId){
        this.textureId = textureId;
    }

    public int getTextureId(){
        return this.textureId;
    }

    @Override
    public void close() throws Exception {
        glDeleteTextures(textureId);
    }
}
