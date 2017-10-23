package image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import math.Vector3f;


import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Image {
	
	private BufferedImage image;
	private int nbRows;
	private int nbColumns;
	private float horizontalAngleOfView;
	private float verticalAngleOfView;
	
	/**
	 * 
	 * @param image
	 * 			The given byte array that represents the image.
	 * @param nbRows
	 * 			The amount of rows.
	 * @param nbColumns
	 * 			The amount of columns.
	 * @param horizontalAngleOfView
	 * 			The horizontal angle of view.
	 * @param verticalAngleOfView
	 * 			The vertical angle of view.
	 * @throws IOException
	 * 			One of the parameters is invalid or the image cannot be read.
	 */
	public Image(byte[] image, int nbRows, int nbColumns, float horizontalAngleOfView, float verticalAngleOfView) throws Exception{
		if ( (!isValidImage(image, nbRows, nbColumns)) || (!isValidAngle(horizontalAngleOfView)) || (!isValidAngle(verticalAngleOfView)) )
			throw new Exception("The given byte array is invalid or does not have the right dimensions.");
		this.nbRows = nbRows;
		this.nbColumns = nbColumns;
		this.horizontalAngleOfView = horizontalAngleOfView;
		this.verticalAngleOfView = verticalAngleOfView;
		this.image = ImageIO.read(new ByteArrayInputStream(image));
	}
	
	private boolean isValidImage(byte[] image, int nbRows, int nbColumns){
		return (nbRows > 0) && (nbColumns > 0) && (image.length > 0);
	}
	
	private boolean isValidAngle(float angle){
		return (angle >= 0) && (angle <= 360);
	}
	
	public BufferedImage getImage(){
		return this.image;
	}
	
	public int getnbRows(){
		return this.nbRows;
	}
	
	public int getnbColumns(){
		return this.nbColumns;
	}
	
	public float getHorizontalAngle(){
		return this.horizontalAngleOfView;
	}
	
	public float getVerticalAngle(){
		return this.verticalAngleOfView;
	}
	
	/**
	 * Calculates the HSV-value of the given pixel of this image.
	 * @param x
	 * The x coordinate.
	 * @param y
	 * The y coordinate.
	 * @return
	 * An array with 3 floats indicating the hue, saturation and value of the given pixel.
	 * @throws Exception 
	 * The given coordinates are invalid.
	 */
	public float[] getPixelHSV(int x, int y) throws Exception{
		if ((!isValidXCoordinate(x)) || (!isValidYCoordinate(y))) 
			throw new Exception();
		int rgb = getImage().getRGB(x, y);
		Color col = new Color(rgb);
		float[] hsv = new float[3];
		Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), hsv);
		return hsv;
	}
	
	private boolean isValidXCoordinate(int x){
		return (x >= 0) && (x <= getnbColumns()-1);
	}
	
	private boolean isValidYCoordinate(int y){
		return (y >= 0) && (y <= getnbRows()-1);
	}
	
	/**
	 * Returns whether or not the given color is part of the red spectrum.
	 * @param hsv
	 * The hue, saturation and value of the given color.
	 * @return
	 * Whether or not the given color is part of the red spectrum.
	 * @throws Exception 
	 * The given HSV is invalid.
	 */
	public static boolean isRedHSV(float[] hsv) throws Exception{
		if (!isValidHSV(hsv)) {throw new Exception();}
		float hue = hsv[0];
		float saturation = hsv[1];
		return (((hue >= (1.0 - (5.0/360.0))) || (hue <= (10.0/360.0))) && (saturation >= 0.25));
	}
	
	private static boolean isValidHSV(float[] hsv){
		float h = hsv[0];
		float s = hsv[1];
		float v = hsv[2];
		return ( (h >= 0.0) && (h <= 1.0) && (s >= 0.0) && (s <= 1.0) && (v >= 0.0) && (v <= 1.0) );
	}
	
	/**
	 * Returns a list of all red pixels in the image.
	 * @throws Exception
	 * The coordinates of the scanned pixels are invalid.
	 */
	private ArrayList<Pixel> getRedPixels() throws Exception{
		ArrayList<Pixel> redPixels = new ArrayList<Pixel>();
		for (int y = 0; y < getnbRows(); y++){
			for (int x = 0; x < getnbColumns(); x++){
				float[] hsv = getPixelHSV(x, y);
				if (isRedHSV(hsv)){
					redPixels.add(new Pixel(x, y, hsv));
				}
			}
		}
		return redPixels;
	}
	
	/**
	 * Calculates the coordinates of the center pixel of the image.
	 */
	public int[] getCenterPixel(){
		int[] center = {getnbColumns()/2, getnbRows()/2};
		return center;
	}
	
	/**
	 * Calculates the coordinates of the average red pixel of the image.
	 * @throws Exception
	 * Something goes wrong while calculating the red pixels.
	 */
	public int[] getAverageRedPixel() throws Exception{
		ArrayList<Pixel> redPixels = getRedPixels();
		int totalX = 0;
		int totalY = 0;
		for (Pixel p : redPixels){
			totalX += p.getX();
			totalY += p.getY();
		}
		int[] avg = {totalX / redPixels.size(), totalY / redPixels.size()};
		return avg;
	}
	
	/**
	 * Return the distance (measured in pixels) that a given position is from the center pixel of the image.
	 * @param img	The given image.
	 * @param x		The x-coordinate of the given position.
	 * @param y		The y-coordinate of the given position.
	 * @return	An array which contains the x-value and the y-value of the distance to the centre of the image.
	 */
	public int[] getPixelsFromCenter(int x, int y){
		int[] center = getCenterPixel();
		int x_value = x - center[0];
		int y_value = y - center[1];
		return new int[] {x_value, y_value};
	}
	
	/** Return the horizontal and vertical rotation necessary to fly towards a given point,
	 *  of which the distance to the center is given in terms of x- and y-coordinate.
	 * 
	 * @param img				The given image.
	 * @param fov_horizontal	The horizontal angle of view.
	 * @param fov_vertical		The vertical angle of view.
	 * @param x_to_center		The horizontal distance to the center of the image (given in pixels).
	 * @param y_to_center		The vertical distance to the centre of the image (given in pixels).
	 * @return	An array containing the horizontal the vertical rotation.
	 */
	public float[] getNecessaryRotation(float fov_horizontal, float fov_vertical, float x_to_center, float y_to_center){
		int width = getnbColumns();
		int height = getnbRows();
		float y_rotation = -x_to_center * fov_horizontal / width;
		float x_rotation = -y_to_center * fov_vertical / height;
		return new float[] {y_rotation, x_rotation};
	}
	
	public float[] getRotationToRedCube() throws Exception{
		int[] averageRed = getAverageRedPixel();
		int[] pixelsToRedCube = getPixelsFromCenter(averageRed[0], averageRed[1]);
		return getNecessaryRotation(getHorizontalAngle(), getVerticalAngle(), pixelsToRedCube[0], pixelsToRedCube[1]);
	}
	
//	public float getPercentageOfRedPixels() throws Exception{
//		float redAmount = getRedPixels().size();
//		float totalAmount = getnbColumns() * getnbRows();
//		return (redAmount / totalAmount) * 100;
//	}
	
	public ArrayList<Pixel> getRedEdgePixels() throws Exception{
		ArrayList<Pixel> edge = new ArrayList<Pixel>();
		boolean isEdge = false;
		for (Pixel p : getRedPixels()){
			int x = p.getX();
			int y = p.getY();
			if (x < getnbColumns()-1 && !isRedHSV(getPixelHSV(x+1, y)))
				isEdge = true;
			if (x > 0 && !isRedHSV(getPixelHSV(x-1, y)))
				isEdge = true;
			if (y < getnbRows()-1 && !isRedHSV(getPixelHSV(x, y+1)))
				isEdge = true;
			if (y > 0 && !isRedHSV(getPixelHSV(x, y-1)))
				isEdge = true;
			if (isEdge){
				edge.add(p);
				isEdge = false;
			}
		}
		return edge;
	}
	
	public float getMinimumDistanceSpherePixels() throws Exception{
		float minimum = (float) Math.sqrt( Math.pow(getnbRows(), 2) + Math.pow(getnbColumns(), 2) );
		int[] centerPixel = getAverageRedPixel();
		for(Pixel p : getRedEdgePixels()){
			float distance = (float) Math.sqrt(Math.pow(p.getX()-centerPixel[0], 2) + Math.pow(p.getY()-centerPixel[1], 2));
			if (distance < minimum)
				minimum = distance;
		}
		return minimum;
	}
	
	public float getMaximumDistanceSpherePixels() throws Exception{
		float maximum = 0;
		int[] centerPixel = getAverageRedPixel();
		for(Pixel p : getRedEdgePixels()){
			float distance = (float) Math.sqrt(Math.pow(p.getX()-centerPixel[0], 2) + Math.pow(p.getY()-centerPixel[1], 2));
			if (distance > maximum)
				maximum = distance;
		}
		return maximum;
	}
	
	public int getAmountSidesVisible() throws Exception{
		boolean checkX = true;
		boolean checkY = true;
		boolean checkZ = true;
		int visible = 0;
		for (Pixel p : getRedPixels()){
			if (checkX && isRedXPixel(p)){
				checkX = false;
				visible++;
			}
			if (checkY && isRedYPixel(p)){
				checkY = false;
				visible++;
			}
			if (checkZ && isRedZPixel(p)){
				checkZ = false;
				visible++;
			}
		}
		return visible;
	}
	
	private boolean isRedXPixel(Pixel p){
		float v = p.getValue();
		return ( (v >= 0.8 && v <= 0.9) || (v >= 0.25 && v <= 0.35) );
	}
	
	private boolean isRedYPixel(Pixel p){
		float v = p.getValue();
		return ( (v >= 0.95) || (v >= 0.1 && v <= 0.2) );
	}
	
	private boolean isRedZPixel(Pixel p){
		float v = p.getValue();
		return ( (v >= 0.65 && v <= 0.75) || (v >= 0.4 && v <= 0.5) );
	}
	
	public float getZDistance() throws Exception{
		int sides = getAmountSidesVisible();
		if (sides == 3){
			float pixels = getMaximumDistanceSpherePixels();
			float angle = (pixels * getHorizontalAngle()) / getnbColumns();
			return (float) (Math.sqrt(0.75) / Math.tan((degreesToRadians(angle))));
		} else if ((sides == 1) || (sides == 2)) {
			float pixels = getMinimumDistanceSpherePixels();
			float angle = (pixels * getHorizontalAngle()) / getnbColumns();
			return (float) (0.5 / Math.tan(degreesToRadians(angle)));
		} else
			throw new Exception("The cube does not have the right values to be visible or no cube is present.");
	}
	
	public float getXDistance() throws Exception{
		int[] averageRed = getAverageRedPixel();
		int[] center = getCenterPixel();
		float distanceZ = getZDistance();
		float angleX = ( Math.abs(center[0] - averageRed[0]) * getHorizontalAngle() ) / getnbColumns();
		return (float) (distanceZ * Math.tan(degreesToRadians(angleX)));
		
	}
	
	public float getYDistance() throws Exception{
		int[] averageRed = getAverageRedPixel();
		int[] center = getCenterPixel();
		float distanceZ = getZDistance();
		float angleY = ( Math.abs(center[1] - averageRed[1]) * getVerticalAngle() ) / getnbRows();
		return (float) (distanceZ * Math.tan(degreesToRadians(angleY)));
		
	}
	
	public float get3DDistanceToCube() throws Exception{
		return (float) Math.sqrt( Math.pow(getXDistance(), 2) + Math.pow(getYDistance(), 2) + Math.pow(getZDistance(), 2) );
	}
	
	public float degreesToRadians(float degrees){
		return (degrees * ((float) Math.PI / 180.0f));
	}
	
	public Vector3f getVectorToRedCube() throws Exception{
		float x = getXDistance();
		float y = getYDistance();
		float z = getZDistance();
		int[] averageRed = getAverageRedPixel();
		int[] center = getCenterPixel();
		if (averageRed[0] < center[0])
			x = -x;
		if (averageRed[1] > center[1])
			y = -y;
		z = -z;	
		return new Vector3f(x,y,z);
	}
	
}

