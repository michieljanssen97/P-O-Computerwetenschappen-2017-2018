package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.math.VectorMath;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import be.kuleuven.cs.robijn.worldObjects.OrthographicCamera;
import be.kuleuven.cs.robijn.worldObjects.WorldObject;

import org.apache.commons.math3.linear.RealVector;

public class OpenGLOrthographicCamera extends OrthographicCamera {
    private float width = 100f;
    private float height = 100f;
    private float zNear = 0.1f;
    private float zFar = 100000f;
    private float iconSize = 1.0f;
    private Vector2D iconOffset = new Vector2D(0, 0);

    @Override
    public void centerObject(WorldObject obj) {
        RealVector cameraPos = this.getWorldPosition();
        RealVector objPos = obj.getWorldPosition();

        //Get camera 'normal' (= vector from center of camera through the middle of the screen)
        Vector3D normal = this.getWorldRotation().applyTo(new Vector3D(0,  0, -1));

        //Project the object position unto the line defined by the camera normal.
        Vector3D cameraToObj = VectorMath.realTo3D(objPos.subtract(cameraPos));
        Rotation alpha = new Rotation(cameraToObj, normal);
        Vector3D cameraToProjectedPos = normal.scalarMultiply(cameraToObj.getNorm() * Math.cos(alpha.getAngle()));
        RealVector projectedPos = cameraPos.add(VectorMath.vector3DToReal(cameraToProjectedPos));

        //Calculate the delta vector from the projection point to the object position
        RealVector deltaVector = objPos.subtract(projectedPos);

        //Apply the delta vector to this object
        this.setRelativePosition(deltaVector.add(this.getRelativePosition()));
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public void setWidth(float width) {
        if(width < 0 || Float.isNaN(width) || Float.isInfinite(width)){
            throw new IllegalArgumentException("'width' must be a positive number, not NaN or infinite");
        }
        this.width = width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void setHeight(float height) {
        if(height < 0 || Float.isNaN(height) || Float.isInfinite(height)){
            throw new IllegalArgumentException("'height' must be a positive number, not NaN or infinite");
        }
        this.height = height;
    }

    @Override
    public float getIconSize() {
        return iconSize;
    }

    @Override
    public void setIconSize(float size) {
        iconSize = size;
    }

    @Override
    public Vector2D getIconOffset() {
        return iconOffset;
    }

    @Override
    public void setIconOffset(Vector2D offset) {
        this.iconOffset = offset;
    }

    @Override
    public float getNearPlane() {
        return zNear;
    }

    @Override
    public void setNearPlane(float zNear) {
        if(zNear < 0 || Float.isNaN(zNear) || Float.isInfinite(zNear)){
            throw new IllegalArgumentException("'zNear' must be a positive number, not NaN or infinite");
        }
        this.zNear = zNear;
    }

    @Override
    public float getFarPlane() {
        return zFar;
    }

    @Override
    public void setFarPlane(float zFar) {
        if(zFar < 0 || Float.isNaN(zFar) || Float.isInfinite(zFar)){
            throw new IllegalArgumentException("'zFar' must be a positive number, not NaN or infinite");
        }
        this.zFar = zFar;
    }
}
