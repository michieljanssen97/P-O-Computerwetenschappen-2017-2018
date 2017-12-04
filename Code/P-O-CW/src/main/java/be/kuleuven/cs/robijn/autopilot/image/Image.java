package be.kuleuven.cs.robijn.autopilot.image;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.IOException;
import java.util.ArrayList;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * A class that represents images.
 * @author Raf Hermans, Wout Mees
 * @version 1.0
 */
public class Image {
	
	/**
	 * A variable containing the image of this instance.
	 */
	private BufferedImage image;
	
	/**
	 * A variable containing the number of rows of this Image.
	 */
	private int nbRows;
	
	/**
	 * A variable containing the number of columns of this Image.
	 */
	private int nbColumns;
	
	/**
	 * A variable containing the horizontal angle of view of this Image. 
	 */
	private float horizontalAngleOfView;
	
	/**
	 * A variable containing the vertical angle of view of this Image.
	 */
	private float verticalAngleOfView;
	
	/**
	 * A variable containing the list of ImageCubes that are visible in this image.
	 */
	private ArrayList<ImageCube> cubes;
	
	/**
	 * Initiate this Image with a given byte array (represents the image), number of rows, number of columns and horizontal and vertical angle of view.
	 * @param image 				The given byte array that represents the image.
	 * @param nbRows				The amount of rows.
	 * @param nbColumns				The amount of columns.
	 * @param horizontalAngleOfView	The horizontal angle of view.
	 * @param verticalAngleOfView	The vertical angle of view.
	 * @throws Exception 
	 * @throws IOException	One of the parameters is invalid or the image cannot be read.
	 */
	public Image(byte[] image, int nbRows, int nbColumns, float horizontalAngleOfView, float verticalAngleOfView) {
		if ( (!isValidImage(image, nbRows, nbColumns)) || (!isValidAngle(horizontalAngleOfView)) || (!isValidAngle(verticalAngleOfView)) )
			throw new IllegalArgumentException("The given byte array is invalid or does not have the right dimensions.");
		this.nbRows = nbRows;
		this.nbColumns = nbColumns;
		this.horizontalAngleOfView = horizontalAngleOfView;
		this.verticalAngleOfView = verticalAngleOfView;
		for(int i = 0; i < nbColumns * nbRows; i++){
			byte b = image[(i*3)+0];
			image[(i*3)+0] = image[(i*3)+2];
			image[(i*3)+2] = b;
		}
		this.image = new BufferedImage(nbColumns, nbRows, BufferedImage.TYPE_3BYTE_BGR);
		byte[] imageBackingBuffer = ((DataBufferByte) this.image.getRaster().getDataBuffer()).getData();
		System.arraycopy(image, 0, imageBackingBuffer, 0, image.length);
		this.cubes = scanImageForCubes();
	}
	
	/**
	 * Check whether the given image, with the given number of rows and columns is valid.
	 * @param image		The given image
	 * @param nbRows	The given number of rows
	 * @param nbColumns	The given number of columns
	 * @return	True if the number of rows and columns and the length of the image are strictly positive
	 */
	private boolean isValidImage(byte[] image, int nbRows, int nbColumns){
		return (nbRows > 0) && (nbColumns > 0) && (image.length == ((nbRows*nbColumns)*3));
	}
	
	/**
	 * Check whether the given angle is a valid angle.
	 * @param angle	The given angle (given in degrees)
	 * @return	True if the given angle is a value between 0 and 360
	 */
	private boolean isValidAngle(float angle){
		return (angle >= 0) && (angle <= 360);
	}
	
	/**
	 * Return the image of this instance.
	 */
	public BufferedImage getImage(){
		return this.image;
	}
	
	/**
	 * Return the number of rows of this Image.
	 */
	public int getnbRows(){
		return this.nbRows;
	}
	
	/**
	 * Return the number of columns of this Image.
	 */
	public int getnbColumns(){
		return this.nbColumns;
	}
	
	/**
	 * Return the horizontal angle of view of this Image.
	 */
	public float getHorizontalAngle(){
		return this.horizontalAngleOfView;
	}
	
	/**
	 * Return the vertical angle of view of this Image.
	 */
	public float getVerticalAngle(){
		return this.verticalAngleOfView;
	}
	
	/**
	 * Return the cubes on this image.
	 */
	public ArrayList<ImageCube> getImageCubes(){
		return this.cubes;
	}
	
	/**
	 * Calculates the HSV-value of the given pixel of this image.
	 * @param x	The x coordinate
	 * @param y	The y coordinate
	 * @return	An array with 3 floats indicating the hue, saturation and value of the given pixel
	 * @throws Exception	The given coordinates are invalid
	 */
	public float[] getPixelHSV(int x, int y){
		if ((!isValidXCoordinate(x)) || (!isValidYCoordinate(y))) 
			throw new IllegalArgumentException();
		int rgb = getImage().getRGB(x, y);
		Color col = new Color(rgb);
		float[] hsv = new float[3];
		Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), hsv);
		return hsv;
	}
	
	/**
	 * Check whether the given x coordinate is valid.
	 * @param x	The given x coordinate
	 * @return	True if the x coordinate is positive and lower than or equal to the number of columns of this image minus 1
	 */
	private boolean isValidXCoordinate(int x){
		return (x >= 0) && (x <= getnbColumns()-1);
	}
	
	/**
	 * Check whether the given y coordinate is valid.
	 * @param y	The given y coordinate
	 * @return	True if the given y coordinate is positive and lower than or equal to the number of rows minus 1
	 */
	private boolean isValidYCoordinate(int y){
		return (y >= 0) && (y <= getnbRows()-1);
	}
	
	/**
	 * Check whether the given HSV-values are valid.
	 * @param hsv	The given HSV-values
	 * @return	True if all the values are between 0 and 1
	 */
	private static boolean isValidHSV(float[] hsv){
		float h = hsv[0];
		float s = hsv[1];
		float v = hsv[2];
		return ( (h >= 0.0) && (h <= 1.0) && (s >= 0.0) && (s <= 1.0) && (v >= 0.0) && (v <= 1.0) );
	}
	
	/**
	 * Calculates the coordinates of the center pixel of the image.
	 */
	public float[] getCenterPixel(){
		float[] center = {getnbColumns()/2, getnbRows()/2};
		return center;
	}
	
	/**
	 * Return the distance (measured in pixels) that a given position is from the center pixel of the image.
	 * @param img	The given image
	 * @param x		The x coordinate of the given position
	 * @param y		The y coordinate of the given position
	 * @return	An array which contains the x-value and the y-value of the distance to the center of the image
	 */
	public float[] getPixelsFromCenter(float x, float y){
		float[] center = getCenterPixel();
		float x_value = x - center[0];
		float y_value = y - center[1];
		return new float[] {x_value, y_value};
	}
	
	/** Return the horizontal and vertical rotation necessary to fly towards a given point,
	 *  of which the distance to the center on the image is given in terms of x and y coordinate.
	 * @param img				The given image
	 * @param fov_horizontal	The horizontal angle of view
	 * @param fov_vertical		The vertical angle of view
	 * @param x_to_center		The horizontal distance to the center of the image (given in pixels)
	 * @param y_to_center		The vertical distance to the centre of the image (given in pixels)
	 * @return	An array containing the horizontal the vertical rotation
	 */
	public float[] getNecessaryRotation(float fov_horizontal, float fov_vertical, float x_to_center, float y_to_center){
		int width = getnbColumns();
		int height = getnbRows();
		float y_rotation = -x_to_center * fov_horizontal / width;
		float x_rotation = -y_to_center * fov_vertical / height;
		return new float[] {y_rotation, x_rotation};
	}
	
	/**
	 * Return the minimum distance from the red pixels that are on the edge of the cube, to its center.
	 * @param hue	The given hue
	 * @param sat	The given saturation
	 * @throws Exception Something goes wrong while calculating the red pixels
	 */
	public float getMinimumDistanceSpherePixels(float hue, float sat){
		float minimum = (float) Math.sqrt( Math.pow(getnbRows(), 2) + Math.pow(getnbColumns(), 2) );
		float[] centerPixel = getCubeCenterPixel(hue, sat);
		for(Pixel p : getCubeEdgePixels(hue, sat)){
			float distance = (float) Math.sqrt(Math.pow(p.getX()-centerPixel[0], 2) + Math.pow(p.getY()-centerPixel[1], 2)) + 0.5f;
			if (distance < minimum)
				minimum = distance;
		}
		return minimum;
	}

	/**
	 * Return the maximum distance from the red pixels that are on the edge of the cube, to its center.
	 * @param hue	The given hue
	 * @param sat	The given saturation
	 * @throws Exception	Something goes wrong while calculating the red pixels
	 */
	public float getMaximumDistanceSpherePixels(float hue, float sat){
		float maximum = 0;
		float[] centerPixel = getCubeCenterPixel(hue, sat);
		for(Pixel p : getCubeEdgePixels(hue, sat)){
			float distance = (float) Math.sqrt(Math.pow(p.getX()-centerPixel[0], 2) + Math.pow(p.getY()-centerPixel[1], 2));
			if (distance > maximum)
				maximum = distance;
		}
		return maximum + 0.5f;
	}
	
	/**
	 * Return the amount of sides of the cube with given hue and saturation that are visible in the Image.
	 * @param hue	The given hue
	 * @param sat	The given saturation
	 * @throws Exception	Something goes wrong while calculating the pixels of the cube.
	 */
	public int getAmountSidesVisible(float hue, float sat) throws IllegalStateException{
		boolean checkX = true;
		boolean checkY = true;
		boolean checkZ = true;
		int visible = 0;
		for (Pixel p : getCubePixels(hue, sat)){
			if (checkX && isXPixel(p)){
				checkX = false;
				visible++;
			}
			if (checkY && isYPixel(p)){
				checkY = false;
				visible++;
			}
			if (checkZ && isZPixel(p)){
				checkZ = false;
				visible++;
			}
		}
		return visible;
	}
	
	/**
	 * Check whether the given pixel is in a surface perpendicular to the x axis of the world coordinate system.
	 * @param p	The given pixel
	 * @return	True if the value of the pixel is either between 0.8 and 0.9 or between 0.25 and 0.35
	 */
	private boolean isXPixel(Pixel p){
		float v = p.getValue();
		return ( (v >= 0.8 && v <= 0.9) || (v >= 0.25 && v <= 0.35) );
	}
	
	/**
	 * Check whether the given pixel is in a surface perpendicular to the y axis of the world coordinate system.
	 * @param p	The given pixel
	 * @return	True if the value of the pixel is either between 0.1 and 0.2 or larger than 0.95
	 */
	private boolean isYPixel(Pixel p){
		float v = p.getValue();
		return ( (v >= 0.95) || (v >= 0.1 && v <= 0.2) );
	}
	
	/**
	 * Check whether the given pixel is in a surface perpendicular to the z axis of the world coordinate system.
	 * @param p	The given pixel
	 * @return	True if the value of the pixel is either between 0.4 and 0.5 or between 0.65 and 0.75
	 */
	private boolean isZPixel(Pixel p){
		float v = p.getValue();
		return ( (v >= 0.65 && v <= 0.75) || (v >= 0.4 && v <= 0.5) );
	}
	
	/**
	 * Return the distance from the cube with given hue and saturation to the camera along the (negative) z axis of the drone coordinate system.
	 * @param hue	The given hue
	 * @param sat	The given saturation
	 * @throws Exception	There are no sides of a cube with given hue and saturation visible in this Image
	 */
	public float getTotalDistance(float hue, float sat){
		int sides = getAmountSidesVisible(hue, sat);
		if (sides == 3){
			float[] percentageXYZPixels = getPercentageXYZPixels(hue, sat);
			if (percentageXYZPixels[0] < 0.1 || percentageXYZPixels[1] < 0.1 || percentageXYZPixels[2] < 0.1){
				float pixels = getMinimumDistanceSpherePixels(hue, sat);
				float angle = (pixels * getHorizontalAngle()) / getnbColumns();
				float ratio = 1;
				if (percentageXYZPixels[0] < 0.1){
					ratio = percentageXYZPixels[1] / percentageXYZPixels[2];
				}
				if (percentageXYZPixels[1] < 0.1){
					ratio = percentageXYZPixels[0] / percentageXYZPixels[2];
				}
				if (percentageXYZPixels[2] < 0.1){
					ratio = percentageXYZPixels[0] / percentageXYZPixels[1];
				}
				if (ratio < 1){
					ratio = 1 / ratio;
				}
				float angleCos = (float) Math.sqrt(1 / (1 + Math.pow(ratio, 2)));
				float planeAngle = (float) Math.acos(angleCos);
				return (float) (0.5/Math.tan(degreesToRadians(angle)) + 0.5 / Math.sin(planeAngle));
			}
			else{
				float pixels = getMaximumDistanceSpherePixels(hue, sat);
				float angle = (pixels * getHorizontalAngle()) / getnbColumns();
				return (float) (Math.sqrt(0.75) / Math.tan((degreesToRadians(angle))));
			}
		} else if (sides == 2) {
			float pixels = getMinimumDistanceSpherePixels(hue, sat);
			float angle = (pixels * getHorizontalAngle()) / getnbColumns();
			float ratio = getRatioPixelsIfTwoPlanesVisible(hue, sat);
			float angleCos = (float) Math.sqrt(1 / (1 + Math.pow(ratio, 2)));
			float planeAngle = (float) Math.acos(angleCos);
			return (float) (0.5/Math.tan(degreesToRadians(angle)) + 0.5 / Math.sin(planeAngle));
		} else if (sides == 1){
			float pixels = getMinimumDistanceSpherePixels(hue, sat);
			float angle = (pixels * getHorizontalAngle()) / getnbColumns();
			return (float) (0.5 / Math.tan(degreesToRadians(angle)) + 0.5);
		} else
			throw new IllegalStateException("The cube does not have the right values to be visible or no cube is present.");
	}
	
	
	/**
	 * Return a vector containing the x, y and z distance from the camera to the cube with given hue and saturation.
	 * @throws Exception	Something goes wrong while calculating the pixels with given hue and saturation
	 */
	public RealVector getXYZDistance(float hue, float sat){
		float[] cubeCenter = getCubeCenterPixel(hue, sat);
		float[] imageCenter = getCenterPixel();
		float angleX = (cubeCenter[0] - imageCenter[0]) * getHorizontalAngle() / getnbColumns();
		float angleY = (imageCenter[1] - cubeCenter[1]) * getVerticalAngle() / getnbRows();
		float distanceX =  (float) (getTotalDistance(hue, sat)*Math.sin(degreesToRadians(angleX)));
		float distanceY =  (float) (getTotalDistance(hue, sat)*Math.sin(degreesToRadians(angleY)));
		float distanceZ = (float) - Math.sqrt(Math.pow(getTotalDistance(hue, sat), 2) - Math.pow(distanceX, 2) - Math.pow(distanceY, 2));
		double[] vectorDouble = {distanceX, distanceY, distanceZ};
		RealVector realVector = new ArrayRealVector(vectorDouble);
		return realVector;
	}
	
	
	/**
	 * Convert a given amount of degrees to radians.
	 * @param degrees	The given amount of degrees
	 * @return	| result = degrees * Math.PI / 180
	 */
	public float degreesToRadians(float degrees){
		return (degrees * ((float) Math.PI / 180.0f));
	}
	
	/**
	 * Return the percentages of the pixels with given hue and saturation that are in planes perpendicular to the x-axis, the y-axis and the z-axis.
	 * @param hue	The given hue
	 * @param sat	The given saturation
	 * @throws IllegalStateException There is no cube with given hue and saturation on the image.
	 */
	public float[] getPercentageXYZPixels(float hue, float sat) throws IllegalStateException{
		ArrayList<Pixel> redPixels = getCubePixels(hue, sat);
		float TotalRedPixels = getCubePixels(hue, sat).size();
		float redXPixels = 0;
		float redYPixels = 0;
		float redZPixels = 0;
		if (redPixels.size() == 0)
			throw new IllegalStateException("there is no red cube on the camera image");
		for (Pixel p : redPixels){
			if (isXPixel(p)){
				redXPixels += 1;
			} else if (isYPixel(p)){
				redYPixels += 1;
			} else if (isZPixel(p)){
				redZPixels += 1;
			}
		}
		float percentageXPixels = redXPixels / TotalRedPixels;
		float percentageYPixels = redYPixels / TotalRedPixels;
		float percentageZPixels = redZPixels / TotalRedPixels;
		float[] percentageXYZPixels = new float[] {percentageXPixels, percentageYPixels, percentageZPixels};
		return percentageXYZPixels;
	}
	
	/**
	 * Return the ratio of the amount of pixels of the two planes that are visible.
	 * @param hue	The given hue
	 * @param sat	The given saturation
	 * @throws Exception				Something goes wrong while calculating the red pixels. 		
	 * @throws IllegalStateException	The amount of visible sides is not equal to 2.
	 */
	public float getRatioPixelsIfTwoPlanesVisible(float hue, float sat) throws IllegalStateException{
		if (getAmountSidesVisible(hue, sat) != 2){
			throw new IllegalStateException("The amount of visible sides is not equal to 2");
		}
		float ratio = 0;
		float[] percentageXYZPixels = getPercentageXYZPixels(hue, sat);
		if (percentageXYZPixels[0] == 0){
			ratio = percentageXYZPixels[1] / percentageXYZPixels[2];
		} else if (percentageXYZPixels[1] == 0){
			ratio = percentageXYZPixels[0] / percentageXYZPixels[2];
		} else if (percentageXYZPixels[2] == 0){
			ratio = percentageXYZPixels[0] / percentageXYZPixels[1];
		}
		if (0 < ratio && ratio < 1){
			return 1 / ratio;
		}
		return ratio;
	}
	
	/**
	 * Check whether the given hue, saturation and value represent the white color.
	 * @param hsv	The given hue, saturation and value.
	 * @return		True if the saturation is 0 and the value is 1.
	 */
	public static boolean isWhiteHSV(float[] hsv){
		if (!isValidHSV(hsv)) {throw new IllegalArgumentException();}
		return (hsv[1] == 0.0f) && (hsv[2] == 1.0f);
	}
	
	/**
	 * Return a list containing all the cubes that are visible on this image.
	 * @throws Exception 
	 */
	public ArrayList<ImageCube> scanImageForCubes() {
		ArrayList<ImageCube> cubeCollection = new ArrayList<ImageCube>();
		boolean cubeExists = false;
		for (int y = 0; y < getnbRows(); y++){
			for (int x = 0; x < getnbColumns(); x++){
				float[] hsv = getPixelHSV(x, y);
				if (!isWhiteHSV(hsv)){
					for (ImageCube c : cubeCollection){
						if ((hsv[0] == c.getHue()) && (hsv[1] == c.getSaturation())){
							c.addPixel(new Pixel(x, y, hsv));
							cubeExists = true;
							break;
						}
					}
					if (!cubeExists){
						ArrayList<Pixel> p = new ArrayList<Pixel>();
						p.add(new Pixel(x, y, hsv));
						ImageCube cu = new ImageCube(p, hsv[0], hsv[1]);
						cubeCollection.add(cu);
					}
					cubeExists = false;
				}
			}
		}
		return cubeCollection;
	}
	
	/**
	 * Return a list containing all the pixels that have the given hue and saturation.
	 * @param hue	The given hue
	 * @param sat	The given saturation
	 */
	public ArrayList<Pixel> getCubePixels(float hue, float sat){
		ImageCube cube = null;
		for (ImageCube c : getImageCubes()){
			if ( (hue == c.getHue()) && (sat == c.getSaturation()) )
				cube = c;
		}
		if (cube == null)
			throw new IllegalArgumentException("No cube with the given hue and saturation exists in this image.");
		else
			return cube.getPixels();
	}
	
	/**
	 * Return the center of all the pixels with given hue and saturation.
	 * @param hue 	The given hue
	 * @param sat	The given saturation
	 */
	public float[] getCubeCenterPixel(float hue, float sat){
		ArrayList<Pixel> cubePixels = getCubePixels(hue, sat);
		if (cubePixels.size() == 0)
			throw new IllegalStateException("There is no cube on the camera image with the given hue and saturation.");
		float totalX = 0.0f;
		float totalY = 0.0f;
		for (Pixel p : cubePixels){
			totalX += p.getX();
			totalY += p.getY();
		}
		float[] avg = {totalX / cubePixels.size(), totalY / cubePixels.size()};
		return avg;
	}
	
	/**
	 * Return the rotation that is necessary to fly towards the cube with given hue and saturation.
	 * @param hue	The given hue
	 * @param sat	The given saturation
	 */
	public float[] getRotationToCube(float hue, float sat) {
		float[] average = getCubeCenterPixel(hue, sat);
		float[] pixelsToCube = getPixelsFromCenter(average[0], average[1]);
		return getNecessaryRotation(getHorizontalAngle(), getVerticalAngle(), pixelsToCube[0], pixelsToCube[1]);
	}
	
	/**
	 * Return the pixels that are on the edge of the cube with given hue and saturation.
	 * @param hue	The given hue
	 * @param sat	The given saturation
	 * @throws Exception 
	 */
	public ArrayList<Pixel> getCubeEdgePixels(float hue, float sat) {
		ArrayList<Pixel> edge = new ArrayList<Pixel>();
		boolean isEdge = false;
		for (Pixel p : getCubePixels(hue, sat)){
			int x = p.getX();
			int y = p.getY();
			if (x < getnbColumns()-1 && !isEqualHSCombination(p.getHSV(), getPixelHSV(x+1, y)))
				isEdge = true;
			if (x > 0 && !isEqualHSCombination(p.getHSV(), getPixelHSV(x-1, y)))
				isEdge = true;
			if (y < getnbRows()-1 && !isEqualHSCombination(p.getHSV(), getPixelHSV(x, y+1)))
				isEdge = true;
			if (y > 0 && !isEqualHSCombination(p.getHSV(), getPixelHSV(x, y-1)))
				isEdge = true;
			if (isEdge){
				edge.add(p);
				isEdge = false;
			}
		}
		return edge;
	}
	
	/**
	 * Check whether two given hsv-arrays have equal hue and saturation.
	 * @param hsv1	The first given hsv-array
	 * @param hsv2	The second given hsv-array
	 * @return	True if the first and the second values of the arrays are equal.
	 */
	public boolean isEqualHSCombination(float[] hsv1, float[] hsv2){
		if ( (!isValidHSV(hsv1)) || (!isValidHSV(hsv2)) )
			throw new IllegalArgumentException();
		return ( (hsv1[0] == hsv2[0]) && (hsv1[1] == hsv2[1]) );
	}
	
	/**
	 * Return the cube that is closest to the drone in this image.
	 * @throws Exception	There are no sides of a cube visible in this image.
	 */
	public ImageCube getClosestCube() {
		float minimum = 100;
		ImageCube cube = null;
		for (ImageCube cu : this.cubes){
			float hue = cu.getHue();
			float saturation = cu.getSaturation();
			float distance = getTotalDistance(hue, saturation);
			if (distance < minimum){
				cube = cu;
				minimum = distance;
			}
		}
		return cube;
	}
	
	/**
	 * Get the weight factor that is necessary for the cube with given hue and saturation.
	 * @param hue	The given hue
	 * @param sat	The given saturation
	 */
	public float getNecessaryCubeFactor(float hue, float sat) {
		ArrayList<Pixel> cubePixels = getCubePixels(hue, sat);
		for (Pixel p : cubePixels){
			if (p.getX() == 0 || p.getX() == getnbColumns() -1 || p.getY() == 0 || p.getY() == getnbRows() -1)
				return 0;
		}
		float maxDistanceToCenter = (float) Math.sqrt(Math.pow(getnbColumns()/2, 2) + Math.pow(getnbRows()/2, 2));
		float[] cubeCenterPixel = getCubeCenterPixel(hue, sat);
		float[] PixelsToCenter = getPixelsFromCenter(cubeCenterPixel[0], cubeCenterPixel[1]);
		float distanceToCenter = (float) Math.sqrt(Math.pow(PixelsToCenter[0], 2) + Math.pow(PixelsToCenter[1], 2));
		float factorValue = 5 * (1 - distanceToCenter / maxDistanceToCenter) * (1 - 0.01f * getTotalDistance(hue, sat));
		return factorValue;
	}
}

