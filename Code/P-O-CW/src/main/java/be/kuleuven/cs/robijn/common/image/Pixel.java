package image;


public class Pixel {
	
	private int x;
	private int y;
	private float[] hsv;
	
	/**
	 * Constructor.
	 * @param x
	 * The x coordinate of the pixel.
	 * @param y
	 * The y coordinate of the pixel.
	 * @param hsv
	 * The HSV-values of the pixel (in an array of three values).
	 */
	public Pixel(int x, int y, float[] hsv){
		this.x = x;
		this.y = y;
		this.hsv = hsv;
	}
	
	public int getX(){
		return this.x;
	}
	
	public int getY(){
		return this.y;
	}
	
	public float getHue(){
		return this.hsv[0];
	}
	
	public float getSaturation(){
		return this.hsv[1];
	}
	
	public float getValue(){
		return this.hsv[2];
	}
	
}