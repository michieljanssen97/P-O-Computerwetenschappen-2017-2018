package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.Camera;

public class OpenGLCamera extends Camera {
	private float fovHorizontal = (float)Math.PI/2f;
	private float fovVertical = (float)Math.PI/2f;
	private boolean areDronesHidden;
	private boolean drawGround;

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
	public void setDronesHidden(boolean renderDrones) {
		areDronesHidden = renderDrones;
	}

	@Override
	public boolean areDronesHidden() {
		return areDronesHidden;
	}

	public void setDrawGround(boolean drawGround) {
		this.drawGround = drawGround;
	}

	public boolean isGroundDrawn() {
		return drawGround;
	}
}
