package be.kuleuven.cs.robijn.autopilot.image;

import be.kuleuven.cs.robijn.common.math.Vector3f;
import java.util.ArrayList;


/**
 * A class that can create and work with images.
 * @author Raf Hermans, Wout Mees
 * @version 1.0
 */
public class ImageRecognizer {
	
	public ImageRecognizer(){
	}
	
	/**
	 * Read and create an image structure.
	 * @param image					The given image (given as a byte array)
	 * @param nbRows				The given number of rows
	 * @param nbColumns				The given number of columns
	 * @param horizontalAngleOfView	The given horizontal angle of view
	 * @param verticalAngleOfView	The given vertical angle of view
	 * @return	An instance of the Image class containing the given values
	 * @throws Exception	One of the parameters is invalid or the image can't be read
	 */
	public Image createImage(byte[] image, int nbRows, int nbColumns, float horizontalAngleOfView, float verticalAngleOfView) throws IllegalStateException{
		Image im = new Image(image, nbRows, nbColumns, horizontalAngleOfView, verticalAngleOfView);
		this.UpdateImageRecognizerCubeList(im);
		return im;
	}
	
	public ArrayList<ImageRecognizerCube> ImageRecognizerCubeList = new ArrayList<ImageRecognizerCube>();
	
	/**
	 * Returns the average coordinates of the pixels of the cube with given hue and saturation in the given image.
	 * @param image	The given image
	 * @return	A list with the x-coordinate and y-coordinate of the center of all pixels with given hue and saturation
	 * @throws Exception	Something goes wrong while calculating the pixels with given hue and saturation.
	 */
	public float[] getCubeAveragePixel(Image image, float hue, float sat) throws Exception{
		return image.getCubeCenterPixel(hue, sat);
	}
	
	/**
	 * 
	 * @param image	The given image
	 * @return	The rotation necessary for the drone to turn towards the center of the cube with given hue and saturation
	 * 			(an x-value and a y-value given in degrees)
	 * @throws Exception Something goes wrong while calculating the average coordinates of the pixels with given hue and saturation
	 */
	public float[] getNecessaryRotation(Image image, float hue, float sat) throws IllegalStateException{
		return image.getRotationToCube(hue, sat);
	}
	

	/**
	 * Returns the distance to cube with given hue and saturation in the given image.
	 * @param image	The given image
	 * @return	The distance to the cube
	 * @throws Exception	There are no sides of a cube with given hue and saturation visible in this Image
	 */
	public float getDistanceToCube(Image image, float hue, float sat) throws Exception{
		return image.getTotalDistance(hue, sat);
	}
	
	/**
	 * Returns the vector from the camera to the center of the cube with given hue and saturation.
	 * @param image	The given image
	 * @return	The vector from the camera to the center of the cube
	 * @throws Exception There are no sides of a cube with given hue and saturation visible in this Image
	 */
	public Vector3f getVectorToCube(Image image, float hue, float sat) throws Exception{
		return image.getXYZDistance(hue, sat);
	}
	
	public ImageRecognizerCube getClosestCubeInWorld(){
		float[] curPos = {0.0f, 0.0f, 0.0f}; //replace with drone's current position
		ImageRecognizerCube closest = null;
		float minimum = 1000f;
		boolean first = true;
		for (ImageRecognizerCube c : this.ImageRecognizerCubeList){
			if (first){
				closest = c;
				first = false;
			} else {
				float distance = (float) Math.sqrt(Math.pow(curPos[0] - c.getX(), 2) + Math.pow(curPos[1] - c.getY(), 2) + Math.pow(curPos[2] - c.getZ(), 2));
				if (distance < minimum){
					minimum = distance;
					closest = c;
				}
			}
		}
		return closest;
	}
	
	public ArrayList<float[]> getAllHueSatCombinations(){
		ArrayList<float[]> combos = new ArrayList<float[]>();
		for (ImageRecognizerCube c : this.ImageRecognizerCubeList){
			float[] combo = {c.getHue(), c.getSaturation()};
			combos.add(combo);
		}
		return combos;
	}
	
	public ImageRecognizerCube getImageRecognizerCube(Image image, float hue, float sat) throws IllegalStateException{
		for (ImageRecognizerCube cu : ImageRecognizerCubeList){
			if (floatFuzzyEquals(hue, cu.getHue(), 0.01f) && floatFuzzyEquals(sat, cu.getSaturation(), 0.01f)){
				Vector3f vector = image.getXYZDistance(hue, sat);
				ImageRecognizerCube cube = new ImageRecognizerCube(vector.getX(), vector.getY(), vector.getZ(), hue, sat);
				return cube;
			}
		}
		return null;
	}
	
	public void UpdateImageRecognizerCubeList(Image image) throws IllegalStateException{
		for (ImageCube cu : image.getImageCubes()){
			float hue = cu.getHue();
			float sat = cu.getSaturation();
			Vector3f vector = image.getXYZDistance(hue, sat);
			ImageRecognizerCube cube = getImageRecognizerCube(image, hue, sat);
			if (cube == null){
				ImageRecognizerCubeList.add(new ImageRecognizerCube(vector.getX(), vector.getY(), vector.getZ(), hue, sat));
			}
		}
	}
	
	public boolean floatFuzzyEquals(float a, float b, float delta){
		return Math.abs(a - b) <= delta;
	}
	
}
