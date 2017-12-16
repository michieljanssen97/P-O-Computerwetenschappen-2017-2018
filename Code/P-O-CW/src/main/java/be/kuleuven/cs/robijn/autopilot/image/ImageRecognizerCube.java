package be.kuleuven.cs.robijn.autopilot.image;

/**
 * A class that represents a cube with its position (x, y and z coordinate) and its hue and saturation.
 * @author Raf Hermans, Wout Mees
 * @version 1.0
 */
public class ImageRecognizerCube {
	
	/**
	 * A variable containing the x coordinate of this ImageRecognizerCube.
	 */
	private float x;
	
	/**
	 * A variable containing the y coordinate of this ImageRecognizerCube.
	 */
	private float y;
	
	/**
	 * A variable containing the z coordinate of this ImageRecgonizerCube.
	 */
	private float z;
	
	/**
	 * A variable containing the hue of this ImageRecognizerCube.
	 */
	private float hue;
	
	/**
	 * A variable containing the saturation of this ImageRecognizerCube.
	 */
	private float saturation;
	
	/**
	 * A variable containing the factor of this ImageRecognizerCube.
	 */
	private float factor = 0.01f;
	
	/**
	 * A variable indicating whether or not this ImageRecognizerCube is destroyed.
	 */
	private boolean destroyed = false;
	
	private boolean isTarget = false;
	
	/**
	 * Initialize this ImageRecognizerCube with given x, y and z coordinate and given hue and saturation.
	 * @param x				The given x coordinate
	 * @param y				The given y coordinate
	 * @param z				The given z coordinate
	 * @param hue			The given hue
	 * @param saturation	The given saturation
	 * @throws IllegalArgumentException		The given position or color for the cube is invalid.
	 */
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
	
	/**
	 * Returns the factor of this ImageRecognizerCube.
	 */
	public float getFactor(){
		return this.factor;
	}
	
	/**
	 * Check whether a given value is a valid factor for this ImageRecognizerCube.
	 * @param value		The given value
	 * @return		True if the value is between 0 and 5.
	 */
	public boolean isValidFactor(float value){
		return (value >= 0 && Float.isFinite(value));
	}
	
	/**
	 * Set the factor of this ImageRecognizerCube to the given value.
	 * @param value		The given value
	 * @throws IllegalArgumentException		The given value is not a valid factor for this ImageRecognizerCube.
	 */
	public void setFactor(float value) throws IllegalArgumentException{
		if (!isValidFactor(value))
			throw new IllegalArgumentException("This is not a valid factor for this cube.");
		this.factor = value;
	}
	
	/**
	 * Check whether the given value is a valid coordinate for this ImageRecognizerCube.
	 * @param value		The given value
	 * @return		True if the given value is finite.
	 */
	public boolean isValidCoordinate(float value){
		return (Double.isFinite(value));
	}
	
	/**
	 * Return the x coordinate of this ImageRecognizerCube.
	 */
	public float getX(){
		return this.x;
	}
	
	/**
	 * Return the y coordinate of this ImageRecognizerCube.
	 */
	public float getY(){
		return this.y;
	}
	
	/**
	 * Return the z coordinate of this ImageRecognizerCube.
	 */
	public float getZ(){
		return this.z;
	}
	
	/**
	 * Return the position of this ImageRecognizerCube.
	 * @return	An array containing the x, y and z coordinate of this ImageRecognizerCube.
	 */
	public float[] getPosition(){
		float[] position = {this.x, this.y, this.z};
		return position;
	}
	
	/**
	 * Return the hue of this ImageRecognizerCube.
	 */
	public float getHue(){
		return this.hue;
	}
	
	/**
	 * Return the saturation of this ImageRecognizerCube.
	 */
	public float getSaturation(){
		return this.saturation;
	}
	
	/**
	 * Set the position of this ImageRecognizerCube to the given position.
	 * @param x		The given x coordinate
	 * @param y		The given y coordinate
	 * @param z		The given z coordinate
	 * @throws IllegalArgumentException		The given position is not valid for this ImageRecognizerCube.
	 */
	public void setPosition(float x, float y, float z) throws IllegalArgumentException{
		if (!isValidCoordinate(x) || !isValidCoordinate(y) || !isValidCoordinate(z))
			throw new IllegalArgumentException("This is not a valid position for this cube.");
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void destroy(){
		this.destroyed = true;
		this.isTarget = false;
	}
	
	public boolean isNotDestroyed(){
		return !this.destroyed;
	}
	
	public void makeTarget() {
		this.isTarget = true;
	}
	
	public boolean isTarget() {
		return this.isTarget;
	}
	
}
