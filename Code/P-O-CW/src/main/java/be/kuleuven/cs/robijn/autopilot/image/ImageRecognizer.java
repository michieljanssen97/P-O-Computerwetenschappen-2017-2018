package be.kuleuven.cs.robijn.autopilot.image;

import java.util.ArrayList;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;


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
	public Image createImage(byte[] image, int nbRows, int nbColumns, float horizontalAngleOfView, float verticalAngleOfView) throws IllegalStateException, Exception{
		Image im = new Image(image, nbRows, nbColumns, horizontalAngleOfView, verticalAngleOfView);
		this.UpdateImageRecognizerCubeList(im);
		return im;
	}
	
	/**
	 * A variable that consists of an ArrayList of all ImageRecognizerCubes that are visible in an image.
	 */
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
	public RealVector getVectorToCube(Image image, float hue, float sat) throws Exception{
		return image.getXYZDistance(hue, sat);
	}
	

	/**
	 * Return the cube in the ImageRecognizerCubeList that is closest to the current position of the drone.
	 */
	public ImageRecognizerCube getClosestCubeInWorld(){
		
		 //Replace with drone's current position (x, y, z) in world coordinates.
		float[] curPos = {0.0f, 0.0f, 0.0f};
		
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
	
	/**
	 * Return an ArrayList containing all combinations of hue and saturation that are visible in the image.
	 */
	public ArrayList<float[]> getAllHueSatCombinations(){
		ArrayList<float[]> combos = new ArrayList<float[]>();
		for (ImageRecognizerCube c : this.ImageRecognizerCubeList){
			float[] combo = {c.getHue(), c.getSaturation()};
			combos.add(combo);
		}
		return combos;
	}
	
	/**
	 * Return the ImageRecognizerCube in the list that has the given hue and saturation as its color.
	 * @param image		The given image
	 * @param hue		The given hue
	 * @param sat		The given saturation
	 * @return	The ImageRecognizerCube that corresponds to the given hue and saturation, or null if there is none such.
	 * @throws Exception
	 */
	public ImageRecognizerCube getImageRecognizerCube(Image image, float hue, float sat) throws Exception{
		for (ImageRecognizerCube cu : ImageRecognizerCubeList){
			if (floatFuzzyEquals(hue, cu.getHue(), 0.01f) && floatFuzzyEquals(sat, cu.getSaturation(), 0.01f)){
				RealVector vector = image.getXYZDistance(hue, sat);
				ImageRecognizerCube cube = new ImageRecognizerCube((float) vector.getEntry(0), (float) vector.getEntry(1), (float) vector.getEntry(2), hue, sat);
				return cube;
			}
		}
		return null;
	}
	
	
	/**
	 * Update the list containing the ImageRecognizerCubes. For each cube, the new position is calculated
	 * as the weighted average of the previously calculated position and the new position. The weight of
	 * the previously calculated position gets larger the more times the position has been calculated.
	 * @param image		The given image
	 * @throws Exception
	 */
	public void UpdateImageRecognizerCubeList(Image image) throws Exception{
		for (ImageCube cu : image.getImageCubes()){
			float hue = cu.getHue();
			float sat = cu.getSaturation();
			RealVector vector = image.getXYZDistance(hue, sat);
			
			//Replace 0,0,0 with the roll, pitch and heading.
			RealVector vectorWorld = transformationToWorldCoordinates(vector, 0, 0, 0);
			
			//Replace 0,0,0 with the position of the drone (x, y, z) in world coordinates.
			double[] droneCoordinates = {0,0,0};
			
			RealVector dronePosition = new ArrayRealVector(droneCoordinates);
			
			double[] cubeCoordinates = {dronePosition.getEntry(0) + vectorWorld.getEntry(0), dronePosition.getEntry(1) + vectorWorld.getEntry(1), dronePosition.getEntry(2) + vectorWorld.getEntry(2)};
			
			RealVector cubePosition = new ArrayRealVector(cubeCoordinates);
			
			ImageRecognizerCube cube = getImageRecognizerCube(image, hue, sat);
			if (image.getTotalDistance(hue, sat) <= 4)
				ImageRecognizerCubeList.remove(cube);
			if (cube == null){
				float value = image.getNecessaryCubeFactor(hue, sat);
				ImageRecognizerCube cube1 = new ImageRecognizerCube((float) cubePosition.getEntry(0), (float) cubePosition.getEntry(1), (float) cubePosition.getEntry(2), hue, sat);
				cube1.setFactor(value);
				ImageRecognizerCubeList.add(cube1);
				break;
			} else {
				float previous_factor = cube.getFactor();
				float new_factor = image.getNecessaryCubeFactor(hue, sat);
				float total_factor = previous_factor + new_factor;
				float newX = (previous_factor * cube.getX() + new_factor * (float) cubePosition.getEntry(0)) / total_factor;
				float newY = (previous_factor * cube.getY() + new_factor * (float) cubePosition.getEntry(1)) / total_factor;
				float newZ = (previous_factor * cube.getZ() + new_factor * (float) cubePosition.getEntry(2)) / total_factor;
				cube.setPosition(newX, newY, newZ);
				cube.setFactor(total_factor);
			}
			
		}
	}

	
	
	
	
	
	
	
	
	
	
 //  -----------------      //
 //                            //
 //  TRANSFORMATION MATRICES   //				DRONE TO WORLD COORDINATES
 //                            //
 //     -----------------      //
	/**
	 * Transform the given vector from Drone coordinates to Heading-Pitch coordinates
	 * @param realVector
	 * 			The vector to transform from Drone to Heading-Pitch coordinates
	 * @return The given vector in Heading-Pitch coordinates
	 */
	public RealVector inverseRollTransformation(RealVector realVector, float roll){
		float rollAngle = roll;
		RealMatrix inverseRollTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for inverse roll
			{Math.cos(rollAngle),      -Math.sin(rollAngle),       0},
			{Math.sin(rollAngle),       Math.cos(rollAngle),       0}, 
			{0,                         0,                         1}
			}, false);
		return inverseRollTransformation.operate(realVector);	
	}
	
	/**
	 * Transform the given vector from Heading-Pitch coordinates to Heading coordinates
	 * @param realVector
	 * 			The vector to transform from Heading-Pitch to Heading coordinates
	 * @return The given vector in Heading coordinates
	 */
	public RealVector inversePitchTransformation(RealVector realVector, float pitch){
		float PitchAngle = pitch;
		RealMatrix inversePitchTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for inverse pitch
			{1,       0,                        0},
			{0,       Math.cos(PitchAngle),    -Math.sin(PitchAngle)},
			{0,       Math.sin(PitchAngle),     Math.cos(PitchAngle)}
			}, false);
		return inversePitchTransformation.operate(realVector);	
	}
	
	/**
	 * Transform the given vector from Heading coordinates to World coordinates
	 * @param realVector
	 * 			The vector to transform from Heading to World coordinates
	 * @return The given vector in World coordinates
	 */
	public RealVector inverseHeadingTransformation(RealVector realVector, float heading){
		float headingAngle = heading;
		RealMatrix inverseHeadingTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for inverse heading
			{Math.cos(headingAngle),     0,       Math.sin(headingAngle)}, 
			{0,                          1,       0}, 
			{-Math.sin(headingAngle),    0,       Math.cos(headingAngle)}
			}, false);
		return inverseHeadingTransformation.operate(realVector);	
	}
	
	/**
	 * Transform the given vector from Drone coordinates to World Coordinates
	 * @param realVector
	 * 			The vector to transform from Drone to World coordinates
	 * @return The given vector in World Coordinates
	 */
	public RealVector transformationToWorldCoordinates(RealVector realVector, float roll, float pitch, float heading) {
		return this.inverseHeadingTransformation(this.inversePitchTransformation(this.inverseRollTransformation(realVector, roll), pitch), heading);
	}
	
	public boolean floatFuzzyEquals(float a, float b, float delta){
		return Math.abs(a - b) <= delta;
	}
	
}
