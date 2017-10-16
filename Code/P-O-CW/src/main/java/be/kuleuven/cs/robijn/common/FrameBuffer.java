package be.kuleuven.cs.robijn.common;

/**
 * Represents a buffer that contains the pixels produced by the renderer
 */
public interface FrameBuffer {
    /**
     * Returns the width of the image stored in the framebuffer, in pixels
     */
    int getWidth();

    /**
     * Returns the height of the image stored in the framebuffer, in pixels
     */
    int getHeight();

    /**
     * Retrieve the color values of the pixels from the buffer and store them in the specified byte array.
     * Each pixel is represented by 3 bytes: one red, one green, one blue (RGB).
     * The pixels are stored in the buffer row-linearized.
     * The buffer must be at least 3*width*height bytes long.
     * @param data the array to store the values in.
     * @throws IllegalArgumentException when 'data' is null or not large enough.
     */
    void readPixels(byte[] data);
}
