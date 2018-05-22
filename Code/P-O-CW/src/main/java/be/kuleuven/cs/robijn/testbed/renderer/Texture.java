package be.kuleuven.cs.robijn.testbed.renderer;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.*;

public class Texture implements AutoCloseable {
    private final int textureId, internalFormat, format, dataType, bytesPerPixel;
    private final int[] dimensions;
    private Vector2D textureScale = new Vector2D(1, 1);

    public static Texture createEmpty(int[] dimensions, int channels){
        if(dimensions.length < 1 || dimensions.length > 3){
            throw new IllegalArgumentException("Invalid dimensions count "+channels+". Value must be 1, 2 or 3.");
        }
        for (int i = 0; i < dimensions.length; i++) {
            int dimension = dimensions[i];
            if (dimension <= 0) {
                throw new IllegalArgumentException("Invalid size " + dimension + " for dimension "+i+". Value must be greater than 0.");
            }
        }
        if(channels < 1 || channels > 4){
            throw new IllegalArgumentException("Invalid channel count "+channels+". Value must be 1, 2, 3 or 4.");
        }

        //Create new OpenGL texture
        int textureId = createNewTextureId(dimensions.length);

        int internalFormat = 0, format = 0;
        switch(channels){
            case 1:
                internalFormat = GL_R8;
                format = GL_RED;
                break;
            case 2:
                internalFormat = GL_RG8;
                format = GL_RG;
                break;
            case 3:
                internalFormat = GL_RGB8;
                format = GL_RGB;
                break;
            case 4:
                internalFormat = GL_RGBA8;
                format = GL_RGBA;
                break;
        }

        int dataType = GL_UNSIGNED_BYTE;

        int[] dimensionsCopy = new int[dimensions.length];
        System.arraycopy(dimensions, 0, dimensionsCopy, 0, dimensions.length);

        int bpp = channels; //One byte per channel

        Texture tex = new Texture(textureId, internalFormat, format, dataType, dimensionsCopy, bpp);
        tex.setData(null);
        return tex;
    }

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
        int textureId = createNewTextureId(2);

        //Load image data into texture
        int internalFormat = GL_RGBA8;
        int format = GL_BGRA;
        int dataType = GL_UNSIGNED_INT_8_8_8_8_REV;
        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, img.getWidth(), img.getHeight(), 0, format, dataType, buffer);

        return new Texture(textureId, internalFormat, format, dataType, new int[]{img.getWidth(), img.getHeight()}, 4);
    }

    private static int createNewTextureId(int dimensions){
        int textureId = glGenTextures();
        int target = getTargetForDimensionCount(dimensions);

        glBindTexture(target, textureId);
        glTexParameteri(target, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(target, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        return textureId;
    }

    private static int getTargetForDimensionCount(int dimensions){
        switch (dimensions){
            case 1: return GL_TEXTURE_1D;
            case 2: return GL_TEXTURE_2D;
            case 3: return GL_TEXTURE_3D;
            default: throw new RuntimeException("Invalid dimension count");
        }
    }

    private Texture(int textureId, int internalFormat, int format, int dataType, int[] dimensions, int bytesPerPixel){
        this.textureId = textureId;
        this.internalFormat = internalFormat;
        this.dataType = dataType;
        this.format = format;
        this.dimensions = dimensions;
        this.bytesPerPixel = bytesPerPixel;
    }

    public int getTextureId(){
        return this.textureId;
    }

    public int getSize(int dimension) {
        if(dimension < 0 || dimension >= dimensions.length){
            throw new IndexOutOfBoundsException("Invalid dimension "+dimension+". This texture only has "+dimensions.length+" dimensions.");
        }
        return dimensions[dimension];
    }

    public int getDimensions(){
        return dimensions.length;
    }

    public void setData(ByteBuffer data){
        if(data != null){
            data.rewind();
            switch (dimensions.length){
                case 1: glTexImage1D(GL_TEXTURE_1D, 0, internalFormat, dimensions[0], 0, format, dataType, data); break;
                case 2: glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, dimensions[0], dimensions[1], 0, format, dataType, data); break;
                case 3: glTexImage3D(GL_TEXTURE_3D, 0, internalFormat, dimensions[0], dimensions[1], dimensions[2], 0, format, dataType, data); break;
            }
        }else if(dataType == GL_UNSIGNED_BYTE){
            switch (dimensions.length){
                case 1: glTexImage1D(GL_TEXTURE_1D, 0, internalFormat, dimensions[0], 0, format, dataType, 0); break;
                case 2: glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, dimensions[0], dimensions[1], 0, format, dataType, 0); break;
                case 3: glTexImage3D(GL_TEXTURE_3D, 0, internalFormat, dimensions[0], dimensions[1], dimensions[2], 0, format, dataType, 0); break;
            }
        }else{
            throw new RuntimeException("Uploading bytes is not supported on this texture");
        }
    }

    public int getBytesPerPixel() {
        return bytesPerPixel;
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
