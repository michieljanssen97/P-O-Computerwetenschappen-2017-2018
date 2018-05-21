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
	public Image createImage(byte[] image, int nbRows, int nbColumns, float horizontalAngleOfView, float verticalAngleOfView, RealVector dronePos, float heading, float pitch, float roll) {
		Image im = new Image(image, nbRows, nbColumns, horizontalAngleOfView, verticalAngleOfView);
		this.dronePosition = dronePos;
		this.heading = heading;
		this.pitch = pitch;
		this.roll = roll;
		if (!isFollowingPathCoordinates())
			this.UpdateImageRecognizerCubeList(im);
//		if (!this.hasTarget())
//			getClosestCubeInWorld(im).makeTarget();
		return im;
	}
	
	/**
	 * A variable that consists of an ArrayList of all ImageRecognizerCubes that are visible in an image.
	 */
	private ArrayList<ImageRecognizerCube> ImageRecognizerCubeList = new ArrayList<ImageRecognizerCube>();
	
	private RealVector dronePosition = new ArrayRealVector(new double[]{0, 0, 0}, false);
	
	private float heading = 0.0f;
	
	private float pitch = 0.0f;
	
	private float roll = 0.0f;
	
	private CubePath path = null;
	
	/**
	 * Returns the average coordinates of the pixels of the cube with given hue and saturation in the given image.
	 * @param image	The given image
	 * @return	A list with the x-coordinate and y-coordinate of the center of all pixels with given hue and saturation
	 * @throws Exception	Something goes wrong while calculating the pixels with given hue and saturation.
	 */
	public float[] getCubeAveragePixel(Image image, float hue, float sat) {
		return image.getCubeCenterPixel(hue, sat);
	}
	
	/**
	 * Returns the rotations in x and y for the drone to take to the cube with given hue and saturation in the given image.
	 * @param image	The given image
	 * @return	The rotation necessary for the drone to turn towards the center of the cube with given hue and saturation
	 * 			(an x-value and a y-value given in degrees)
	 * @throws Exception Something goes wrong while calculating the average coordinates of the pixels with given hue and saturation
	 */
	public float[] getNecessaryRotation(Image image, float hue, float sat) throws IllegalStateException{
		try {
			return image.getRotationToCube(hue, sat);
		} catch (IllegalArgumentException exc) {
			//default rotation
			RealVector vec = getWorldVectorToCube(getEquivalentImageRecognizerCube(hue, sat));
			double[] dronePos = getDronePositionCoordinates();
			float x,y;
			if (vec.getEntry(0) > (float)dronePos[0])
				x = -image.getHorizontalAngle()/6f;
			else
				x = image.getHorizontalAngle()/6f;
			if (vec.getEntry(1) > (float)dronePos[1])
				y = -image.getVerticalAngle()/6f;
			else
				y = image.getVerticalAngle()/6f;
			return new float[] {x,y};
		}
	}
	
	/**
	 * Returns the distance to cube with given hue and saturation in the given image.
	 * @param image	The given image
	 * @return	The distance to the cube
	 * @throws Exception	There are no sides of a cube with given hue and saturation visible in this Image
	 */
	public float getDistanceToCube(Image image, float hue, float sat){
		return image.getTotalDistance(hue, sat);
	}
	
	/**
	 * Returns the vector from the camera to the center of the cube with given hue and saturation.
	 * @param image	The given image
	 * @return	The vector from the camera to the center of the cube
	 * @throws Exception There are no sides of a cube with given hue and saturation visible in this Image
	 */
	public RealVector getVectorToCube(Image image, float hue, float sat) {
		return image.getXYZDistance(hue, sat);
	}
	
	/**
	 * Return a list with the stored coordinates of the drone accompanying this image recognizer.
	 * @return
	 */
	public double[] getDronePositionCoordinates(){
		RealVector pos = this.dronePosition;
		return new double[] {pos.getEntry(0), pos.getEntry(1), pos.getEntry(2)};
	}
	
	/**
	 * Return a list with the roll, pitch and heading of the drone accompanying this image recognizer.
	 * @return
	 */
	public float[] getRollPitchHeading(){
		return new float[] {this.roll, this.pitch, this.heading};
	}
	
	/**
	 * Return the ImageRecognizerCubeList of this ImageRecognizer.
	 * @return
	 */
	public ArrayList<ImageRecognizerCube> getImageRecognizerCubes(){
		return this.ImageRecognizerCubeList;
	}
	
	/**
	 * Return a list of all ImageRecognizerCubes that are not destroyed.
	 * @return
	 */
	public ArrayList<ImageRecognizerCube> getNotDestroyedImageRecognizerCubes() {
		ArrayList<ImageRecognizerCube> cubes = new ArrayList<ImageRecognizerCube>();
		for (ImageRecognizerCube c : getImageRecognizerCubes()){
			if (c.isNotDestroyed())
				cubes.add(c);
		}
		return cubes;
	}
	
	public ArrayList<ImageRecognizerCube> getImageRecognizerCubesFromImage(Image im){
		ArrayList<ImageRecognizerCube> cubes = new ArrayList<ImageRecognizerCube>();
		for (ImageCube c : im.getImageCubes()) {
			float hue = c.getHue();
			float sat = c.getSaturation();
			cubes.add(getEquivalentImageRecognizerCube(hue, sat));
		}
		return cubes;
	}

	/**
	 * Return the cube in the ImageRecognizerCubeList that is closest to the current position of the drone.
	 */
	public ImageRecognizerCube getClosestCubeInWorld(Image im){
		float[] curPos = {(float)dronePosition.getEntry(0), (float)dronePosition.getEntry(1), (float)dronePosition.getEntry(2)};
		ImageRecognizerCube closest = null;
		float minimum = 1000f;
//		for (ImageRecognizerCube c : getNotDestroyedImageRecognizerCubes()){
		for (ImageRecognizerCube c : getImageRecognizerCubesFromImage(im)) {
			float distance = (float) Math.sqrt(Math.pow(curPos[0] - c.getX(), 2) + Math.pow(curPos[1] - c.getY(), 2) + Math.pow(curPos[2] - c.getZ(), 2));
			if (distance < minimum){
				minimum = distance;
				closest = c;
			}
		}
		return closest;
	}
	
	/**
	 * Get the total distance from the drone to the given cube in the world.
	 * @param cube		The given cube.
	 * @return			The total distance.
	 */
	public float getWorldDistanceToCube(ImageRecognizerCube cube){
		float[] cubePos = cube.getPosition();
		double[] dronePos = getDronePositionCoordinates();
		return (float) Math.sqrt( Math.pow(cubePos[0] - (float)dronePos[0], 2) + Math.pow(cubePos[1] - (float)dronePos[1], 2) + Math.pow(cubePos[2] - (float)dronePos[2], 2) );
	}
	
	/**
	 * Get the vector from the drone to the given cube.
	 * @param cube		The given cube.
	 * @return			The vector from drone to cube.
	 */
	public RealVector getWorldVectorToCube(ImageRecognizerCube cube){
		float[] cubePos = cube.getPosition();
		double[] dronePos = getDronePositionCoordinates();
		return new ArrayRealVector(new double[] {(double)cubePos[0]-dronePos[0] , (double)cubePos[1]-dronePos[1], (double)cubePos[2]-dronePos[2]});
	}
	
	/**
	 * Return an ArrayList containing all combinations of hue and saturation that are visible in the image.
	 */
	public ArrayList<float[]> getAllHueSatCombinations(){
		ArrayList<float[]> combos = new ArrayList<float[]>();
		for (ImageRecognizerCube c : getImageRecognizerCubes()){
			float[] combo = {c.getHue(), c.getSaturation()};
			combos.add(combo);
		}
		return combos;
	}
	
	public ImageRecognizerCube getTargetCube() {
		for (ImageRecognizerCube cu : getImageRecognizerCubes()) {
			if (cu.isTarget())
				return cu;
		}
		return null;
	}
	
	public boolean hasTarget() {
		for (ImageRecognizerCube cu : getImageRecognizerCubes()) {
			if (cu.isTarget())
				return true;
		}
		return false;
	}
	
	/**
	 * Return the ImageRecognizerCube in the list that has the given hue and saturation as its color.
	 * @param hue		The given hue
	 * @param sat		The given saturation
	 * @return	The ImageRecognizerCube that corresponds to the given hue and saturation, or null if there is none such.
	 * @throws Exception
	 */
	private ImageRecognizerCube getEquivalentImageRecognizerCube(float hue, float sat) {
		for (ImageRecognizerCube cu : getImageRecognizerCubes()){
			if (floatFuzzyEquals(hue, cu.getHue(), 0.01f) && floatFuzzyEquals(sat, cu.getSaturation(), 0.01f)){
				return cu;
			}
		}
		//no equivalent ImageRecognizerCube exists => create new ImageRecognizerCube
		return null;
	}
	
	/*
	 * Returns whether or not the two given float numbers are equal within a given maximum error (delta). 
	 */
	private boolean floatFuzzyEquals(float a, float b, float delta){
		return Math.abs(a - b) <= delta;
	}
	
	/**
	 * Update the list containing the ImageRecognizerCubes. For each cube, the new position is calculated
	 * as the weighted average of the previously calculated position and the new position. The weight of
	 * the previously calculated position gets larger the more times the position has been calculated.
	 * @param image		The given image
	 * @throws Exception
	 */
	private void UpdateImageRecognizerCubeList(Image image) {
		for (ImageCube cu : image.getImageCubes()){
			float hue = cu.getHue();
			float sat = cu.getSaturation();
			RealVector vector = image.getXYZDistance(hue, sat);
			if (Double.isNaN(vector.getEntry(0)) || (Double.isNaN(vector.getEntry(1))) || (Double.isNaN(vector.getEntry(2))))
				vector = new ArrayRealVector(new double[] {0, 0, -100}, false);
			
			float[] droneRotation = getRollPitchHeading();
			RealVector vectorWorld = transformationToWorldCoordinates(vector, droneRotation[0], droneRotation[1], droneRotation[2]);
			
			double[] droneCoordinates = getDronePositionCoordinates();
			RealVector dronePosition = new ArrayRealVector(droneCoordinates);
			
			double[] cubeCoordinates = {dronePosition.getEntry(0) + vectorWorld.getEntry(0), dronePosition.getEntry(1) + vectorWorld.getEntry(1), dronePosition.getEntry(2) + vectorWorld.getEntry(2)};
			RealVector cubePosition = new ArrayRealVector(cubeCoordinates);
			
			ImageRecognizerCube cube = getEquivalentImageRecognizerCube(hue, sat);
			if (cube == null){
				float value = image.getNecessaryCubeFactor(hue, sat);
				ImageRecognizerCube cube1 = new ImageRecognizerCube((float) cubePosition.getEntry(0), (float) cubePosition.getEntry(1), (float) cubePosition.getEntry(2), hue, sat);
				cube1.setFactor(value);
				this.ImageRecognizerCubeList.add(cube1);
			} else {
				if (getWorldDistanceToCube(cube) > 60) {
					cube.setPosition((float) cubePosition.getEntry(0), (float) cubePosition.getEntry(1), (float) cubePosition.getEntry(2));
				}
				else if (image.getTotalDistance(hue, sat) > 60 && getWorldDistanceToCube(cube) <= 60) {
					cube.setPosition(cube.getPosition()[0], cube.getPosition()[1], cube.getPosition()[2]);
				}
//				if (image.getTotalDistance(hue, sat) <= 4)
//					cube.destroy();
				else {
					float previous_factor = cube.getFactor();
					float new_factor = image.getNecessaryCubeFactor(hue, sat);
					float total_factor = previous_factor + new_factor;
					float newX = (previous_factor * cube.getX() + new_factor * (float) cubePosition.getEntry(0)) / total_factor;
					float newY = (previous_factor * cube.getY() + new_factor * (float) cubePosition.getEntry(1)) / total_factor;
					float newZ = (previous_factor * cube.getZ() + new_factor * (float) cubePosition.getEntry(2)) / total_factor;
					cube.setPosition(newX, newY, newZ);
					cube.setFactor(total_factor);
				}
				if (getWorldDistanceToCube(cube) <= 4)
					cube.destroy();
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
	private RealVector inverseRollTransformation(RealVector realVector, float roll){
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
	private RealVector inversePitchTransformation(RealVector realVector, float pitch){
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
	private RealVector inverseHeadingTransformation(RealVector realVector, float heading){
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
	private RealVector transformationToWorldCoordinates(RealVector realVector, float roll, float pitch, float heading) {
		return this.inverseHeadingTransformation(this.inversePitchTransformation(this.inverseRollTransformation(realVector, roll), pitch), heading);
	}
	
	/**
	 * Initialize the path (containing approximate cube coordinates). 
	 * @param path
	 * 		The path to initialize.
	 */
	public void setPath(CubePath path) {
		this.path = path;
		setPathTarget(getClosestPathXYZ());
	}
	
	/**
	 * Return the path of this image recognizer.
	 */
	public CubePath getPath() {
		return this.path;
	}
	
	/**
	 * Returns the path coordinates of the cube closest to the drone.
	 */
	public RealVector getClosestPathXYZ() {
		return this.path.getClosestXYZTo(getDronePositionCoordinates());
	}
	
	/**
	 * The path coordinate that the drone is currently travelling to.
	 */
	private RealVector currentPathTarget;
	
	/**
	 * Indicates the travelling pattern of the drone. True means it is travelling towards an 
	 * approximate path coordinate. False means it is travelling towards an exact position of 
	 * a cube, using image recognition to determine that position.
	 */
	private boolean followingPath = true;
	
	private float currentCubeHue;
	
	private float currentCubeSat;
	
	private boolean currentCubeColorCalculated = false;
	
	/**
	 * Returns the path coordinates the drone is currently travelling to.
	 * If the drone gets within a certain distance of the path coordinates, it will switch to 
	 * "exact" mode, meaning it will use image recognition to determine the exact coordinate of the cube.
	 */
	public RealVector getCurrentPathTarget() {
//		System.out.println("PATH");
		RealVector path = this.currentPathTarget;
		double[] drone = getDronePositionCoordinates();
		float distanceToPath = (float) Math.sqrt(Math.pow(path.getEntry(0) - drone[0], 2) + Math.pow(path.getEntry(1) - drone[1], 2) + Math.pow(path.getEntry(2) - drone[2], 2));
		if (distanceToPath <= 150)
			followExactCoordinates();
//			this.followNewPathCoordinates();
		
		return path;
	}
	
	public void setPathTarget(RealVector target) {
		this.currentPathTarget = target;
	}
	
	private void setCurrentCubeHue(float hue) {
		this.currentCubeHue = hue;
	}
	
	private void setCurrentCubeSat(float sat) {
		this.currentCubeHue = sat;
	}
	
	public boolean isFollowingPathCoordinates() {
		return this.followingPath;
	}
	
	/**
	 * Switch the drone's travelling pattern to "exact" mode. The drone will now try to 
	 * determine the cube's exact position with image recognition.
	 */
	public void followExactCoordinates() {
		this.followingPath = false;
	}
	
	/**
	 * Switch the drone to "path" mode. The closest remaining path coordinate is determined and set as 
	 * the drone's current destination, while the previous path coordinate is removed.
	 */
	public void followNewPathCoordinates() {
		this.followingPath = true;
		this.currentCubeColorCalculated = false;
		this.path.removeCoordinate(this.currentPathTarget);
		RealVector nextPath = getClosestPathXYZ();
		if (nextPath != null)
			setPathTarget(getClosestPathXYZ());
		else {
			//default value, drone is supposed to start landing.
			double[] dronePos = getDronePositionCoordinates();
			dronePos[2] = dronePos[2]-1000;
			nextPath = new ArrayRealVector(dronePos);
			setPathTarget(nextPath);
		}
	}
	
	/**
	 * Returns the coordinates of the cube close to the coordinates of the current path (5 meter).
	 * @param im
	 * 		The current image.
	 */
	public RealVector searchForCubeInPathArea(Image im) {
//		System.out.println("EXACT");
		ImageRecognizerCube toFollow;
		float toFollowDistance;
		
		float[] curPos = {(float)dronePosition.getEntry(0), (float)dronePosition.getEntry(1), (float)dronePosition.getEntry(2)};
		float minimum = Float.POSITIVE_INFINITY;
		ImageRecognizerCube closest = null;
		if (currentCubeColorCalculated && getEquivalentImageRecognizerCube(this.currentCubeHue, this.currentCubeSat) == null) {
			followNewPathCoordinates();
			return getCurrentPathTarget();
		}
		for (ImageRecognizerCube c : getImageRecognizerCubesFromImage(im)) {
			float distance = (float) Math.sqrt(Math.pow(curPos[0] - c.getX(), 2) + Math.pow(curPos[1] - c.getY(), 2) + Math.pow(curPos[2] - c.getZ(), 2));
			if (distance < minimum){
				minimum = distance;
				closest = c;
			}
		}
		if (minimum <= 50) {
			setCurrentCubeHue(closest.getHue());
			setCurrentCubeSat(closest.getSaturation());
			this.currentCubeColorCalculated = true;
		}
		toFollow = closest;
		toFollowDistance = minimum;
		
		float[] co = new float[] {toFollow.getX(), toFollow.getY(), toFollow.getZ()};
//		RealVector pathCo = this.currentPathTarget;
//		float pathDistance = (float) Math.sqrt(Math.pow(co[0] - pathCo.getEntry(0), 2) + Math.pow(co[1] - pathCo.getEntry(1), 2) + Math.pow(co[2] - pathCo.getEntry(2), 2));
		
		if (toFollowDistance <= 5) {
			//cube is touched
			toFollow.destroy();
			followNewPathCoordinates();
		}
		
		double[] coD = new double[3];
		coD[0] = co[0];
		coD[1] = co[1];
		coD[2] = co[2];
		return new ArrayRealVector(coD, false);
		
	}
	
}
