package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.math.Vector3f;

public class Camera {

	private Vector3f position;
	private Vector3f rotation;

	// Initialize the camers's position and rotation.
	public Camera() {
		position = new Vector3f();
		rotation = new Vector3f();
	}

	public Camera(Vector3f position, Vector3f rotation) {
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

//	// Move the camera with a given amount.
//	public void movePosition(float offsetX, float offsetY, float offsetZ) {
//		if (offsetZ != 0) {
//			setPosition(position.translate((float) Math.sin(Math.toRadians(rotation.getY())) * -1.0f * offsetZ,
//					0, (float) Math.cos(Math.toRadians(rotation.getY())) * offsetZ));
//		}
//		if (offsetX != 0) {
//			setPosition(position.translate((float) Math.sin(Math.toRadians(rotation.getY() - 90)) * -1.0f * offsetX, 
//					0, (float) Math.cos(Math.toRadians(rotation.getY() - 90)) * offsetX));
//		}
//		setPosition(position.translate(0, offsetY, 0));
//	}

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

}
