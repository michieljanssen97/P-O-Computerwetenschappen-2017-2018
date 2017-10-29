package be.kuleuven.cs.robijn.common;

/**
 * Represents a camera in the 3D simulated world.
 */
public abstract class Camera extends WorldObject {
    /**
     * Returns the horizontal FOV, in radians
     */
    public abstract float getHorizontalFOV();

    /**
     * Sets the horizontal FOV, in radians
     * @throws IllegalArgumentException if the specified value is NaN, infinite, negative or larger than Math.PI
     */
    public abstract void setHorizontalFOV(float fov);

    /**
     * Returns the vertical FOV, in radians
     */
    public abstract float getVerticalFOV();

    /**
     * Sets the horizontal FOV, in radians
     * @throws IllegalArgumentException if the specified value is NaN, infinite, negative or larger than Math.PI
     */
    public abstract void setVerticalFOV(float fov);

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
}
