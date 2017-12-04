package be.kuleuven.cs.robijn.autopilot.image;

import java.util.ArrayList;

public class ImageCube {
	
	/**
	 * Initialize this ImageCube with a given list of pixels, a given hue and a given saturation.
	 * @param pixels		The given list of pixels
	 * @param hue			The given hue
	 * @param saturation	The given saturation
	 */
	public ImageCube(ArrayList<Pixel> pixels, float hue, float saturation){
		this.pixels = pixels;
		this.hue = hue;
		this.saturation = saturation;
	}
	
	/**
	 * A variable containing the list of pixels of this ImageCube.
	 */
	private ArrayList<Pixel> pixels;
	
	/**
	 * A variable containing the hue of this ImageCube.
	 */
	private float hue;
	
	/**
	 * A variable containing the saturation of this ImageCube.
	 */
	private float saturation;
	
	/**
	 * Return the list of pixels of this ImageCube.
	 * @return
	 */
	public ArrayList<Pixel> getPixels(){
		return this.pixels;
	}
	
	/**
	 * Add a pixel to the list of pixels of this ImageCube.
	 * @param pixel		The given pixel
	 */
	public void addPixel(Pixel pixel){
		this.pixels.add(pixel);
	}
	
	/**
	 * Return the hue of this ImageCube.
	 */
	public float getHue(){
		return this.hue;
	}
	
	/**
	 * Return the saturation of this ImageCube.
	 */
	public float getSaturation(){
		return this.saturation;
	}
	
}
