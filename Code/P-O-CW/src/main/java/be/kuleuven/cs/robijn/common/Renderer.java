package be.kuleuven.cs.robijn.common;

/**
 * Produces images of the 3D world being simulated.
 */
public interface Renderer {
    /**
     * Creates a new framebuffer associated with this renderer.
     * @param width the width of the images stored in this framebuffer, in pixels.
     * @param height the height of the images stored in this framebuffer, in pixels.
     * @return the new framebuffer
     */
    FrameBuffer createFrameBuffer(int width, int height);

    /**
     * Creates a new camera associated with this renderer.
     * @return the new camera.
     */
    Camera createCamera();

    /**
     * Renders a new image of the world in its current state to the framebuffer, as viewed through the camera.
     * @param frameBuffer the framebuffer to store the rendered image in.
     * @param camera the camera from which the world is viewed.
     */
    void render(FrameBuffer frameBuffer, Camera camera);
}