package image;

import math.Vector3f;

public class ImageRecognizer {
	
	public ImageRecognizer(){
	}
	
	/**
	 * Read and create an image structure.
	 * @param image
	 * @param nbRows
	 * @param nbColumns
	 * @param horizontalAngleOfView
	 * @param verticalAngleOfView
	 * @return
	 * @throws Exception
	 */
	public Image createImage(byte[] image, int nbRows, int nbColumns, float horizontalAngleOfView, float verticalAngleOfView) throws Exception{
		return new Image(image, nbRows, nbColumns, horizontalAngleOfView, verticalAngleOfView);
	}
	
	/**
	 * Returns the coordinates of the average (center) pixel of the red cube in the given image.
	 * @param image
	 * The given image.
	 * @return
	 * A list with the x-coordinate and y-coordinate of the center red pixel.
	 * @throws Exception
	 */
	public int[] getRedCubeAveragePixel(Image image) throws Exception{
		return image.getAverageRedPixel();
	}
	
	/**
	 * 
	 * @param image
	 * The given image.
	 * @return
	 * The rotation necessary for the drone to turn towards the center of the red cube.
	 * (An x-value and a y-value given in degrees.)
	 * @throws Exception
	 */
	public float[] getNecessaryRotation(Image image) throws Exception{
		return image.getRotationToRedCube();
	}
	
	/**
	 * Returns the distances on the x,y and z-axis between the camera and the red cube.
	 * @param image
	 * The given image.
	 * @return
	 * An array with respecticely the distances on the x,y and z-axis.
	 * @throws Exception
	 */
	public float[] getAxisDistancesToRedCube(Image image) throws Exception{
		return new float[] {image.getXDistance(), image.getYDistance(), image.getZDistance()};
	}

	/**
	 * Returns the distance to red cube in the given image.
	 * @param image
	 * The given image.
	 * @return
	 * The distance to the red cube.
	 * @throws Exception 
	 */
	public float getDistanceToRedCube(Image image) throws Exception{
		return image.get3DDistanceToCube();
	}
	
	/**
	 * Returns the vector from the camera to the center of the red cube.
	 * @param image
	 * The given image.
	 * @return
	 * The vector from the camera to the center of the red cube.
	 * @throws Exception
	 */
	public Vector3f getVectorToRedCube(Image image) throws Exception{
		return image.getVectorToRedCube();
	}
	
}
