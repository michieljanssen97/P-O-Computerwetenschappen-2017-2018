package be.kuleuven.cs.robijn.autopilot.image;

import be.kuleuven.cs.robijn.common.math.Vector3f;

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
	public Image createImage(byte[] image, int nbRows, int nbColumns, float horizontalAngleOfView, float verticalAngleOfView) {
		return new Image(image, nbRows, nbColumns, horizontalAngleOfView, verticalAngleOfView);
	}
	
	/**
	 * Returns the average coordinates of the pixels of the cube with given hue and saturation in the given image.
	 * @param image	The given image
	 * @return	A list with the x-coordinate and y-coordinate of the center of all pixels with given hue and saturation
	 * @throws Exception	Something goes wrong while calculating the pixels with given hue and saturation.
	 */
	public int[] getCubeAveragePixel(Image image, float hue, float sat) throws Exception{
		return image.getCubeCenterPixel(hue, sat);
	}
	
	/**
	 * 
	 * @param image	The given image
	 * @return	The rotation necessary for the drone to turn towards the center of the cube with given hue and saturation
	 * 			(an x-value and a y-value given in degrees)
	 * @throws Exception Something goes wrong while calculating the average coordinates of the pixels with given hue and saturation
	 */
	public float[] getNecessaryRotation(Image image, float hue, float sat) throws Exception{
		return image.getRotationToCube(hue, sat);
	}
	

	/**
	 * Returns the distance to cube with given hue and saturation in the given image.
	 * @param image	The given image
	 * @return	The distance to the cube
	 * @throws Exception	There are no sides of a cube with given hue and saturation visible in this Image
	 */
<<<<<<< HEAD
	public float getDistanceToRedCube(Image image, float hue, float sat) throws Exception{
=======
	public float getDistanceToCube(Image image, float hue, float sat) throws Exception{
>>>>>>> e4b07f9bfdab3e945124d3866afeff2786c86626
		return image.getTotalDistance(hue, sat);
	}
	
	/**
	 * Returns the vector from the camera to the center of the cube with given hue and saturation.
	 * @param image	The given image
	 * @return	The vector from the camera to the center of the cube
	 * @throws Exception There are no sides of a cube with given hue and saturation visible in this Image
	 */
<<<<<<< HEAD
	public Vector3f getVectorToRedCube(Image image, float hue, float sat) throws Exception{
=======
	public Vector3f getVectorToCube(Image image, float hue, float sat) throws Exception{
>>>>>>> e4b07f9bfdab3e945124d3866afeff2786c86626
		return image.getXYZDistance(hue, sat);
	}
	
}
