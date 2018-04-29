package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.worldObjects.Camera;
import be.kuleuven.cs.robijn.worldObjects.OrthographicCamera;
import be.kuleuven.cs.robijn.worldObjects.PerspectiveCamera;

import java.util.concurrent.Semaphore;

/**
 * Produces images of the 3D world being simulated.
 */
public interface Renderer extends AutoCloseable {
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
    PerspectiveCamera createPerspectiveCamera();

    /**
     * Creates a new camera associated with this renderer.
     * @return the new camera.
     */
    OrthographicCamera createOrthographicCamera();

    /**
     * Loads the font with the specified name.
     * @param fontName the font name, or null to load the default font.
     * @return the font instance.
     */
    Font loadFont(String fontName);

    /**
     * Renders a new image of the world in its current state to the framebuffer, as viewed through the camera.
     * @param worldRoot the root of the tree of world objects to be rendered.
     * @param frameBuffer the framebuffer to store the rendered image in.
     * @param camera the camera from which the world is viewed.
     * @param lock a semaphore to acquire before accessing world state.
     */
    RenderTask startRender(WorldObject worldRoot, FrameBuffer frameBuffer, Camera camera, Semaphore lock);

    void clearDebugObjects();
}
