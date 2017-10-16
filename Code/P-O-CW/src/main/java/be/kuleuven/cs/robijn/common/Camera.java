package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.common.math.Vector3f;

public interface Camera {
    Vector3f getPosition();
    void setPosition(Vector3f newPosition);

    Vector3f getRotation();
    void setRotation(Vector3f newRotation);

    float getHorizontalFOV();
    void setHorizontalFOV(float fov);

    float getVerticalFOV();
    void setVerticalFOV(float fov);
}
