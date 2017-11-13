package be.kuleuven.cs.robijn.autopilot.image;

public class ImageRecognizerCube {
	
	private float x;
	
	private float y;
	
	private float z;
	
	private float hue;
	
	private float saturation;
	
	public ImageRecognizerCube(float x, float y, float z, float hue, float saturation) throws IllegalArgumentException{
		if (!isValidCoordinate(x) || !isValidCoordinate(y) || !isValidCoordinate(z))
			throw new IllegalArgumentException("The given position for the cube is invalid.");
		this.x = x;
		this.y = y;
		this.z = z;
		if (0 > hue || 0 > saturation || hue > 1 || saturation > 1)
			throw new IllegalArgumentException("The given color for the cube is invalid.");
		this.hue = hue;
		this.saturation = saturation;
	}
	
	public boolean isValidCoordinate(float value){
		return (Double.isFinite(value));
	}
	
	public float getX(){
		return this.x;
	}
	
	public float getY(){
		return this.y;
	}
	
	public float getZ(){
		return this.z;
	}
	
	public float[] getPosition(){
		float[] position = {this.x, this.y, this.z};
		return position;
	}
	
	public float getHue(){
		return this.hue;
	}
	
	public float getSaturation(){
		return this.saturation;
	}
	
	public void setPosition(float x, float y, float z) throws IllegalArgumentException{
		if (!isValidCoordinate(x) || !isValidCoordinate(y) || !isValidCoordinate(z))
			throw new IllegalArgumentException("This is not a valid position for this cube.");
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
