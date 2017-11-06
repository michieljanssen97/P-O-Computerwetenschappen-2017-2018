package be.kuleuven.cs.robijn.autopilot.image;

import java.util.ArrayList;

public class ImageCube {
	
	public ImageCube(ArrayList<Pixel> pixels, float hue, float saturation){
		this.pixels = pixels;
		this.hue = hue;
		this.saturation = saturation;
	}
	
	private ArrayList<Pixel> pixels;
	
	private float hue;
	
	private float saturation;
	
	public ArrayList<Pixel> getPixels(){
		return this.pixels;
	}
	
	public void addPixel(Pixel pixel){
		this.pixels.add(pixel);
	}
	
	public float getHue(){
		return this.hue;
	}
	
	public float getSaturation(){
		return this.saturation;
	}
	
}
