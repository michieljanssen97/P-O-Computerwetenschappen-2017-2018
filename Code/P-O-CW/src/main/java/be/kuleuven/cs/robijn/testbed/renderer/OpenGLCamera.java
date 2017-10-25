package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.Camera;
import be.kuleuven.cs.robijn.common.math.Vector3f;

public class OpenGLCamera implements Camera {
	private Vector3f position;
	private Vector3f rotation;

	private float fovHorizontal = (float)Math.PI/2f;
	private float fovVertical = (float)Math.PI/2f;

	// Initialize the camers's position and rotation.
	public OpenGLCamera() {
		position = new Vector3f();
		rotation = new Vector3f();
	}

	public OpenGLCamera(Vector3f position, Vector3f rotation) {
		this.position = position;
		this.rotation = rotation;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(float x, float y, float z){
		setPosition(new Vector3f(x,y,z));	
	}
	
	// Set the camera to a given position.
	public void setPosition(Vector3f vector) {
		this.position = vector;
	}

	public Vector3f getRotation() {
		return rotation;
	}

	public void setRotation(float x, float y, float z){
		setRotation(new Vector3f(x,y,z));	
	}

	//Set the camera to a given rotation.
	public void setRotation(Vector3f vector) {
		this.rotation = vector;
	}

	// Rotate the camera with a given amount.
	public void moveRotation(float offsetX, float offsetY, float offsetZ) {
		setRotation(position.translate(offsetX, offsetY, offsetZ));
	}

	@Override
	public float getHorizontalFOV() {
		return fovHorizontal;
	}

	@Override
	public void setHorizontalFOV(float fov) {
		this.fovHorizontal = fov;
	}

	@Override
	public float getVerticalFOV() {
		return fovVertical;
	}

	@Override
	public void setVerticalFOV(float fov) {
		this.fovVertical = fov;
	}
}
