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
     * @throws IllegalArgumentException if the specified value is NaN or infinite
     */
    public abstract void setHorizontalFOV(float fov);

    /**
     * Returns the vertical FOV, in radians
     */
    public abstract float getVerticalFOV();

    /**
     * Sets the horizontal FOV, in radians
     * @throws IllegalArgumentException if the specified value is NaN or infinite
     */
    public abstract void setVerticalFOV(float fov);
}
