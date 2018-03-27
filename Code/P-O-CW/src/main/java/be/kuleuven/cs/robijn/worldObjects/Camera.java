package be.kuleuven.cs.robijn.worldObjects;

import be.kuleuven.cs.robijn.common.WorldObject;

/**
 * Represents a camera in the 3D simulated world.
 */
public abstract class Camera extends WorldObject {
    /**
     * Returns the distance from the camera to the near plane.
     * Any object closer than this distance to the camera will not be visible.
     * @return the near plane distance
     */
    public abstract float getNearPlane();

    /**
     * Sets the near plane distance. Must not be NaN or infinite.
     */
    public abstract void setNearPlane(float zNear);

    /**
     * Returns the distance from the camera to the far plane.
     * Any object further than this distance to the camera will not be visible.
     * @return the far plane distance.
     */
    public abstract float getFarPlane();

    /**
     * Sets the far plane distance. Must not be NaN or infinite.
     */
    public abstract void setFarPlane(float zFar);

    /**
     * Sets whether or not drones are invisible on images rendered through this camera
     * @param renderDrones if true, drones will be invisible. If false, drones are visible
     */
    public abstract void setDronesHidden(boolean renderDrones);

    /**
     * Returns whether or not drones are invisible on images rendered through this camera.
     * False by default.
     */
    public abstract boolean areDronesHidden();

    /**
     * Sets whether or not the ground is visible on images rendered through this camera
     * @param drawGround if true, the ground will be visible. If false, the ground will be invisible
     */
    public abstract void setDrawGround(boolean drawGround);

    /**
     * Returns whether or not the ground is visible on images rendered through this camera.
     * False by default.
     */
    public abstract boolean isGroundDrawn();

    /**
     * Returns whether or not any objects specified using RenderDebug are drawn.
     */
    public abstract boolean areDebugObjectsDrawn();

    /**
     * Sets whether or not any objects specified using RenderDebug are drawn.
     * False by default.
     */
    public abstract void setDrawnDebugObjects(boolean draw);
}
