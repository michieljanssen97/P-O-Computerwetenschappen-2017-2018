package be.kuleuven.cs.robijn.worldObjects;

/**
 * Represents a camera that views the world through perspective projection
 */
public abstract class PerspectiveCamera extends Camera {
    /**
     * Returns the horizontal field-of-view, in radians
     */
    public abstract float getHorizontalFOV();

    /**
     * Sets the horizontal field-of-view, in radians
     * @throws IllegalArgumentException if the specified value is NaN, infinite, negative or larger than Math.PI
     */
    public abstract void setHorizontalFOV(float fov);

    /**
     * Returns the vertical field-of-view, in radians
     */
    public abstract float getVerticalFOV();

    /**
     * Sets the horizontal field-of-view, in radians
     * @throws IllegalArgumentException if the specified value is NaN, infinite, negative or larger than Math.PI
     */
    public abstract void setVerticalFOV(float fov);

}
