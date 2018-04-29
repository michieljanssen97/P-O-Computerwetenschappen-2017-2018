package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.worldObjects.Camera;
import be.kuleuven.cs.robijn.worldObjects.OrthographicCamera;
import be.kuleuven.cs.robijn.worldObjects.PerspectiveCamera;
import org.apache.commons.math3.linear.RealVector;

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
     * Renders a new image of the world in its current state to the framebuffer, as viewed through the camera.
     * @param worldRoot the root of the tree of world objects to be rendered.
     * @param frameBuffer the framebuffer to store the rendered image in.
     * @param camera the camera from which the world is viewed.
     */
    RenderTask startRender(WorldObject worldRoot, FrameBuffer frameBuffer, Camera camera);

    /**
     * Converts a screen-space vector into a world-space vector.
     * @param camera the camera with which the image was made.
     * @param frameBuffer the framebuffer to which the image was rendered.
     * @param screenX the X position on the screen (0 <= screenX < frameBuffer.getWidth())
     * @param screenY the Y position on the screen (0 <= screenY < frameBuffer.getHeight())
     * @return a 3D vector representing the position in world space that is visible at the specified screen space vector.
     */
    RealVector screenPointToWorldSpace(Camera camera, FrameBuffer frameBuffer, int screenX, int screenY);

    /**
     * Converts a screen-space vector into a world-space vector.
     * @param camera the camera with which the image was made.
     * @param frameBuffer the framebuffer to which the image was rendered.
     * @param screenX the X position on the screen (0 <= screenX < frameBuffer.getWidth())
     * @param screenY the Y position on the screen (0 <= screenY < frameBuffer.getHeight())
     * @param z how far from the camera should the returned point be? (0 <= z <= 1, maps to [nearZ; farZ])
     * @return a 3D vector representing the position in world space that is visible at the specified screen space vector.
     */
    RealVector screenPointToWorldSpace(Camera camera, FrameBuffer frameBuffer, int screenX, int screenY, float z);

    BoundingBox getBoundingBoxFor(WorldObject obj);

    void clearDebugObjects();
}
