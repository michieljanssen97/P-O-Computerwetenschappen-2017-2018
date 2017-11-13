package be.kuleuven.cs.robijn.common;

/**
 * Represents a camera that views the world through orthographic projection.
 */
public abstract class OrthographicCamera extends Camera {
    /**
     * Returns the width of the cuboid that is used during projection and frustum culling.
     * Only the objects inside the cuboid defined by getWidth(), getHeight(),
     * getNearPlane() and getFarPlane() will be visible.
     */
    public abstract float getWidth();

    /**
     * Sets the width of the cuboid used during projection and frustum culling. (see getWidth())
     * @throws IllegalArgumentException thrown if width is zero, negative, NaN or infinite
     */
    public abstract void setWidth(float width);

    /**
     * Returns the height of the cuboid that is used during projection and frustum culling.
     * Only the objects inside the cuboid defined by getWidth(), getHeight(),
     * getNearPlane() and getFarPlane() will be visible.
     */
    public abstract float getHeight();

    /**
     * Sets the height of the cuboid used during projection and frustum culling. (see getHeight())
     * @throws IllegalArgumentException thrown if height is zero, negative, NaN or infinite
     */
    public abstract void setHeight(float height);
}
