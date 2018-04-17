package be.kuleuven.cs.robijn.worldObjects;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

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

    /**
     * Retrieves the threshold value that is used to decide whether or not to render an icon for an object.
     * The ratio that is compared to this value is based on the size of the object compared to the camera view cuboid size.
     * A higher threshold value means that larger objects will get icons too.
     * @return the current icons threshold ratio, 0 by default
     */
    public abstract double getRenderIconsThresholdRatio();

    /**
     * Sets the camera icon rendering threshold ratio. See getRenderIconsThresholdRatio() for more details.
     * @param threshold the new value
     * @throws IllegalArgumentException thrown if threshold is negative or NaN
     */
    public abstract void setRenderIconsThresholdRatio(double threshold);

    /**
     * Returns the visual size of the icon in the resulting image in world units.
     */
    public abstract float getIconSize();

    /**
     * Sets the size of the icon. (see getIconSize())
     * @param size the new icon size
     * @throws IllegalArgumentException thrown if size is infinite or NaN
     */
    public abstract void setIconSize(float size);

    /**
     * Returns the visual offset, relative to the object, of the icon in world units, but on the view axis.
     */
    public abstract Vector2D getIconOffset();

    /**
     * Sets the offset of the icon. (see getIconOffset())
     * @param offset the new offset of the icon
     * @throws IllegalArgumentException thrown if offset is null
     */
    public abstract void setIconOffset(Vector2D offset);
}
