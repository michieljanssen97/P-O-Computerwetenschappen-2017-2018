package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.worldObjects.PerspectiveCamera;

public class OpenGLPerspectiveCamera extends PerspectiveCamera {
	private float fovHorizontal = (float)Math.PI/2f;
	private float fovVertical = (float)Math.PI/2f;
	private float zNear = 0.1f;
	private float zFar = 100000f;

	@Override
	public float getHorizontalFOV() {
		return fovHorizontal;
	}

	@Override
	public void setHorizontalFOV(float fov) {
		if(Float.isNaN(fov) || Float.isInfinite(fov) || fov < 0 || fov > Math.PI){
			throw new IllegalArgumentException();
		}
		this.fovHorizontal = fov;
	}

	@Override
	public float getVerticalFOV() {
		return fovVertical;
	}

	@Override
	public void setVerticalFOV(float fov) {
		if(Float.isNaN(fov) || Float.isInfinite(fov) || fov < 0 || fov > Math.PI){
			throw new IllegalArgumentException();
		}
		this.fovVertical = fov;
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
