package be.kuleuven.cs.robijn.common.image;

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
	 * Returns the average coordinates of the pixels of the red cube in the given image.
	 * @param image	The given image
	 * @return	A list with the x-coordinate and y-coordinate of the center red pixel
	 * @throws Exception	Something goes wrong while calculating the red pixels
	 */
	public int[] getRedCubeAveragePixel(Image image) throws Exception{
		return image.getAverageRedPixel();
	}
	
	/**
	 * 
	 * @param image	The given image
	 * @return	The rotation necessary for the drone to turn towards the center of the red cube
	 * 			(an x-value and a y-value given in degrees)
	 * @throws Exception Something goes wrong while calculating the average coordinates of the red pixels
	 */
	public float[] getNecessaryRotation(Image image) throws Exception{
		return image.getRotationToRedCube();
	}
	

	/**
	 * Returns the distance to red cube in the given image.
	 * @param image	The given image
	 * @return	The distance to the red cube
	 * @throws Exception	There are no red cube sides visible in this Image
	 */
	public float getDistanceToRedCube(Image image) throws Exception{
		return image.getTotalDistance();
	}
	
	/**
	 * Returns the vector from the camera to the center of the red cube.
	 * @param image	The given image
	 * @return	The vector from the camera to the center of the red cube
	 * @throws Exception There are no red cube sides visible in this Image
	 */
	public Vector3f getVectorToRedCube(Image image) throws Exception{
		return image.getXYZDistance();
	}
	
}
