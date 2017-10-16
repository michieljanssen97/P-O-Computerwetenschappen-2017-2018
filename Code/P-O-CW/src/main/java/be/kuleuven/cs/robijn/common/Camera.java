package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.common.math.Vector3f;

/**
 * Represents a camera in the 3D simulated world.
 */
public interface Camera {
    /**
     * Returns the position vector of the camera
     * @return a non-null vector
     */
    Vector3f getPosition();

    /**
     * Sets the position vector of the camera
     * @throws IllegalArgumentException if the new position vector is null.
     */
    void setPosition(Vector3f newPosition);

    /**
     * Returns the rotation vector of the camera, with each value being the rotation around the specified axis in radians.
     * @return a non-null vector
     */
    Vector3f getRotation();

    /**
     * Returns the rotation vector of the camera, with each value being the rotation around the specified axis in radians.
     * @throws IllegalArgumentException if the new position vector is null.
     */
    void setRotation(Vector3f newRotation);

    /**
     * Returns the horizontal FOV, in radians
     */
    float getHorizontalFOV();

    /**
     * Sets the horizontal FOV, in radians
     * @throws IllegalArgumentException if the specified value is NaN or infinite
     */
    void setHorizontalFOV(float fov);

    /**
     * Returns the vertical FOV, in radians
     */
    float getVerticalFOV();

    /**
     * Sets the horizontal FOV, in radians
     * @throws IllegalArgumentException if the specified value is NaN or infinite
     */
    void setVerticalFOV(float fov);
}
