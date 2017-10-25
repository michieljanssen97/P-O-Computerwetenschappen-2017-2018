package image;

/**
 * A class that represents the pixels of an image.
 * @author Raf Hermans, Wout Mees
 * @version 1.0
 */
public class Pixel {
	
	/**
	 * A variable containing the x coordinate of this Pixel.
	 */
	private int x;
	
	/**
	 * A variable containing the y coordinate of this Pixel.
	 */
	private int y;
	
	/**
	 * A variable containing the HSV-values of this Pixel.
	 */
	private float[] hsv;
	
	/**
	 * Initiate this Pixel with the given x and y coordinates and HSV-values.
	 * @param x		The x coordinate of the pixel.
	 * @param y		The y coordinate of the pixel.
	 * @param hsv	The HSV-values of the pixel (in an array of three values).
	 */
	public Pixel(int x, int y, float[] hsv){
		this.x = x;
		this.y = y;
		this.hsv = hsv;
	}
	
	/**
	 * Return the x coordinate of this Pixel.
	 */
	public int getX(){
		return this.x;
	}
	
	/**
	 * Return the y coordinate of this Pixel.
	 */
	public int getY(){
		return this.y;
	}
	
	/**
	 * Return the hue of this Pixel.
	 */
	public float getHue(){
		return this.hsv[0];
	}
	
	/**
	 * Return the saturation of this Pixel.
	 */
	public float getSaturation(){
		return this.hsv[1];
	}
	
	/**
	 * Return the value of this Pixel.
	 */
	public float getValue(){
		return this.hsv[2];
	}
	
}