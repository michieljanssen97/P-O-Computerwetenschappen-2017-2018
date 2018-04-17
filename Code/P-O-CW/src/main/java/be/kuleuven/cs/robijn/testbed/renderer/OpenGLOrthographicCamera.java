package be.kuleuven.cs.robijn.testbed.renderer;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import be.kuleuven.cs.robijn.worldObjects.OrthographicCamera;

public class OpenGLOrthographicCamera extends OrthographicCamera {
    private float width = 100f;
    private float height = 100f;
    private float zNear = 0.1f;
    private float zFar = 100000f;
    private double renderIconsThresholdRatio = 0;
    private float iconSize = 1.0f;
    private Vector2D iconOffset = new Vector2D(0, 0);

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
    public double getRenderIconsThresholdRatio() {
        return renderIconsThresholdRatio;
    }

    @Override
    public void setRenderIconsThresholdRatio(double threshold) {
        this.renderIconsThresholdRatio = threshold;
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
