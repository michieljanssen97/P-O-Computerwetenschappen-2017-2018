package be.kuleuven.cs.robijn.autopilot.image;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import javax.imageio.ImageIO;

public class ImageTest {
	
	private byte[] loadImageRGBBytes(String resourceName) throws IOException {
		BufferedImage image = ImageIO.read(getClass().getClassLoader().getResource(resourceName));
		byte[] rgbBytes = new byte[image.getWidth() * image.getHeight()*3];
		for(int y = 0; y < image.getHeight(); y++){
			for(int x = 0; x < image.getWidth(); x++){
				int argb = image.getRGB(x, y);
				byte r = (byte)((argb >> 16) & 0xFF);
				byte g = (byte)((argb >>  8) & 0xFF);
				byte b = (byte)((argb >>  0) & 0xFF);
				int offset = ((y*image.getWidth()) + x) * 3;
				rgbBytes[offset] = r;
				rgbBytes[offset+1] = g;
				rgbBytes[offset+2] = b;
			}
		}
		return rgbBytes;
	}

	@Test
	public void testRGBtoHSV() {
		float[] hsv = new float[3];
		float[] expected = {0.0f, 1.0f, 1.0f};
		Color.RGBtoHSB(255, 0, 0, hsv);
		assertArrayEquals(expected, hsv, 0.01f);
	}
	
	@Test
	public void testIsWhiteHSV() throws Exception {
		float[] hsv = {0.5f, 0.0f, 1.0f};
		assertTrue(Image.isWhiteHSV(hsv));
	}
	
	@Test
	public void testRedCenterPixel5x5() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("5x5-Red-255-0-0-Center-3-3.png");
		Image im = new Image(image1, 5, 5, 120, 120);
		float[] redCo = im.getCubeCenterPixel(0.0f, 1.0f);
		float[] expected = {3f,3f};
		assertArrayEquals(expected, redCo, 0.01f);
	}
	
	@Test
	public void testRedCenterPixel10x10() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = this.loadImageRGBBytes("10x10-Red-255-0-0-Center-3-2.png");
		RealVector dronePos = new ArrayRealVector(new double[] {0,0,0});
		Image im = rec.createImage(image1, 10, 10, 120, 120, dronePos, 0.0f, 0.0f, 0.0f);
		float[] redCo = rec.getCubeAveragePixel(im, 0.0f, 1.0f);
		float[] expected = {3,2};
		assertArrayEquals(expected, redCo, 0.01f);
	}
	
	@Test
	public void testCenterPixel5x5() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("5x5-Red-255-0-0-Center-3-3.png");
		Image im = new Image(image1, 5, 5, 120, 120);
		float[] center = im.getCenterPixel();
		float[] expected = {2,2};
		assertArrayEquals(expected, center, 0.01f);
	}
	
	@Test
	public void testCenterPixel200x200() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = this.loadImageRGBBytes("cube1side.png");
		RealVector dronePos = new ArrayRealVector(new double[] {0,0,0});
		Image im = rec.createImage(image1, 200, 200, 120, 120, dronePos, 0.0f, 0.0f, 0.0f);
		float[] center = im.getCenterPixel();
		float[] expected = {100,100};
		assertArrayEquals(expected, center, 0.01f);
	}
	
	@Test
	public void testRedCenterPixelCube3() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		RealVector dronePos = new ArrayRealVector(new double[] {0,0,0});
		Image im = rec.createImage(image1, 200, 200, 120, 120, dronePos, 0.0f, 0.0f, 0.0f);
		float[] center = rec.getCubeAveragePixel(im, 0.0f, 1.0f);
		float[] expected = {104.77f, 97.275f};
		assertArrayEquals(expected, center, 0.01f);
	}
	
	@Test
	public void testRedCenterPixelCube3Corner() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = this.loadImageRGBBytes("cube3sidecorner.png");
		RealVector dronePos = new ArrayRealVector(new double[] {0,0,0});
		Image im = rec.createImage(image1, 200, 200, 120, 120, dronePos, 0.0f, 0.0f, 0.0f);
		float[] center = rec.getCubeAveragePixel(im, 0.0f, 1.0f);
		float[] expected = {130.542f, 62.668f};
		assertArrayEquals(expected, center, 0.01f);
	}
	
	@Test
	public void testRedEdgePixels10x10() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("10x10-Red-255-0-0-Center-3-2.png");
		Image im = new Image(image1, 10, 10, 120, 120);
		int result = im.getCubeEdgePixels(0.0f, 1.0f).size();
		int expected = 8;
		assertEquals(expected, result);
	}
	
	@Test
	public void testRedEdgePixelsCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		int result = im.getCubeEdgePixels(0.0f, 1.0f).size();
		int expected = 349;
		assertEquals(expected, result);
	}
	
	@Test
	public void testMinimumDistanceSpherePixelsCube1() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube1side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getMinimumDistanceSpherePixels(0.0f, 1.0f);
		float expected = 40.15f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testMinimumDistanceSpherePixelsCube1Square() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube1sidesquare.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getMinimumDistanceSpherePixels(0.0f, 1.0f);
		float expected = 40.5f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testMinimumDistanceSpherePixelsCube2() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getMinimumDistanceSpherePixels(0.0f, 1.0f);
		float expected = 40.34f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testMaximumDistanceSpherePixelsCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getMaximumDistanceSpherePixels(0.0f, 1.0f);
		float expected = 66.73f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testMaximumDistanceSpherePixelsCube3Corner() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3sidecorner.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getMaximumDistanceSpherePixels(0.0f, 1.0f);
		float expected = 66.98f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testSidesVisibleCube2() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		int result = im.getAmountSidesVisible(0.0f, 1.0f);
		int expected = 2;
		assertEquals(result, expected);
	}
	
	@Test
	public void testSidesVisibleCube2Small() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2sidesmall.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		int result = im.getAmountSidesVisible(0.0f, 1.0f);
		int expected = 2;
		assertEquals(expected, result);
	}
	
	@Test
	public void testSidesVisibleCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		int result = im.getAmountSidesVisible(0.0f, 1.0f);
		int expected = 3;
		assertEquals(expected, result);
	}
	
	@Test
	public void testSidesVisibleCube3Corner() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3sidecorner.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		int result = im.getAmountSidesVisible(0.0f, 1.0f);
		int expected = 3;
		assertEquals(expected, result);
	}
	
	@Test
	public void testDistanceXToCube2() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(0);
		float expected = 0.0926f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceYToCube2() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(1);
		float expected = 0.0f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceZToCube2() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(2);
		float expected = -1.81f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testTotalDistanceToCube2() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getTotalDistance(0.0f, 1.0f);
		float expected = 1.81f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceXToCube2Small() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2sidesmall.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(0);
		float expected = 2.57f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceYToCube2Small() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2sidesmall.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(1);
		float expected = 2.48f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceZToCube2Small() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2sidesmall.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(2);
		float expected = -3.24f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testTotalDistanceToCube2Small() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2sidesmall.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getTotalDistance(0.0f, 1.0f);
		float expected = 4.83f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceXToCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(0);
		float expected = 0.052f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceYToCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(1);
		float expected = 0.03f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceZToCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(2);
		float expected = -1.028f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testTotalDistanceToCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getTotalDistance(0.0f, 1.0f);
		float expected = 1.04f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceXToCube3Corner() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3sidecorner.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(0);
		float expected = 0.32f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceYToCube3Corner() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3sidecorner.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(1);
		float expected = 0.40f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceZToCube3Corner() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3sidecorner.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = (float) im.getXYZDistance(0.0f, 1.0f).getEntry(2);
		float expected = -0.90f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void test3DDistanceToCube3Corner() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3sidecorner.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getTotalDistance(0.0f, 1.0f);
		float expected = 1.03f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testPercentageXYZCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float[] result = im.getPercentageXYZPixels(0.0f, 1.0f);
		float[] expected = {0.36f, 0.31f, 0.32f};
		assertArrayEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testGetDistance1Pixel() throws Exception{
		byte[] image = this.loadImageRGBBytes("1pixel.png");
		Image im = new Image(image, 200, 200, 120, 120);
		ArrayList<ImageCube> cubeList = im.getImageCubes();
		float hue = cubeList.get(0).getHue();
		float sat = cubeList.get(0).getSaturation();
		//System.out.println(Float.toString(im.getTotalDistance(hue, sat)));
	}
	
	@Test
	public void testRatioPixelsCube2() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getRatioPixelsIfTwoPlanesVisible(0.0f, 1.0f);
		float expected = 1.01f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testRatioPixelsCube2BiggerRatio() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side2.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getRatioPixelsIfTwoPlanesVisible(0.0f, 1.0f);
		float expected = 4.65f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testAmountCubesCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		int result = im.getImageCubes().size();
		int expected = 1;
		assertEquals(expected, result);
	}
	
	@Test
	public void testAmountCubesMulti3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("multi3cubes.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		int result = im.getImageCubes().size();
		int expected = 3;
		assertEquals(expected, result);
	}
	
	@Test
	public void testHueSatCube3() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		RealVector dronePos = new ArrayRealVector(new double[] {0,0,0});
		Image im = rec.createImage(image1, 200, 200, 120, 120, dronePos, 0.0f, 0.0f, 0.0f);
		ArrayList<float[]> result = rec.getAllHueSatCombinations();
		for (float[] com : result){
			//System.out.println("[" + Float.toString(com[0]) + ", " + Float.toString(com[1]) + "]");
		}
	}
	
	@Test
	public void testHueSatMulti3() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = this.loadImageRGBBytes("multi3cubes.png");
		RealVector dronePos = new ArrayRealVector(new double[] {0,0,0});
		Image im = rec.createImage(image1, 200, 200, 120, 120, dronePos, 0.0f, 0.0f, 0.0f);
		ArrayList<float[]> result = rec.getAllHueSatCombinations();
		for (float[] com : result){
			//System.out.println("[" + Float.toString(com[0]) + ", " + Float.toString(com[1]) + "]");
		}
	}
	
	//___________________________________________________________________________________________________________________________
	
	@Test
	public void testHueSat2Cubes1Side() throws Exception{
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image = this.loadImageRGBBytes("2Cubes1Side.png");
		RealVector dronePos = new ArrayRealVector(new double[] {0,0,0});
		Image im = rec.createImage(image, 200, 200, 120, 120, dronePos, 0.0f, 0.0f, 0.0f);
		assertEquals(rec.getImageRecognizerCubes().size(), 2);
		for (ImageRecognizerCube cube : rec.getImageRecognizerCubes()){
			//System.out.println(Float.toString(cube.getHue()));
			//System.out.println(Float.toString(cube.getSaturation()));
			//System.out.println(Float.toString(cube.getX()));
			//System.out.println(Float.toString(cube.getY()));
			//System.out.println(Float.toString(cube.getZ()));
			//System.out.println(Float.toString(cube.getFactor()));
			//System.out.println(" ");
		}
	}
	
	@Test
	public void testAngleEdgeCube() throws Exception{
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image = this.loadImageRGBBytes("Cube1SideLeft.png");
		RealVector dronePos = new ArrayRealVector(new double[] {0,0,0});
		Image im = rec.createImage(image, 200, 200, 120, 120, dronePos, 0.0f, 0.0f, 0.0f);
		for (ImageCube cube : im.getImageCubes()){
			//System.out.println(Float.toString( (float) im.getXYZDistance(cube.getHue(), cube.getSaturation()).getEntry(0)));
			//System.out.println(Float.toString( (float) im.getXYZDistance(cube.getHue(), cube.getSaturation()).getEntry(1)));
			//System.out.println(Float.toString( (float) im.getXYZDistance(cube.getHue(), cube.getSaturation()).getEntry(2)));
			}
	}
	
	@Test
	public void testImageRecognizerCubePosition() throws Exception{
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image = this.loadImageRGBBytes("CubeInCenter.png");
		RealVector dronePos = new ArrayRealVector(new double[] {10,0,0});
		Image im = rec.createImage(image, 200, 200, 120, 120, dronePos, (float) Math.PI/2, 0.0f, 0.0f);
		for (ImageRecognizerCube cube : rec.getImageRecognizerCubes()){
//			System.out.println(Float.toString(cube.getFactor()));
//			System.out.println(Float.toString(im.getTotalDistance(cube.getHue(), cube.getSaturation())));
//			System.out.println(Float.toString(cube.getPosition()[0]));
//			System.out.println(Float.toString(cube.getPosition()[1]));
//			System.out.println(Float.toString(cube.getPosition()[2]));
		}
	}
	
	@Test
	public void testImageRecognizerCubeFactor() throws Exception{
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image = this.loadImageRGBBytes("4RedCubes.png");
		RealVector dronePos = new ArrayRealVector(new double[] {10,0,0});
		Image im = rec.createImage(image, 200, 200, 120, 120, dronePos, (float) Math.PI/2, 0.0f, 0.0f);
		for (ImageRecognizerCube cube : rec.getImageRecognizerCubes()){
//			System.out.println(Float.toString(cube.getFactor()));
//			System.out.println(Float.toString(im.getTotalDistance(cube.getHue(), cube.getSaturation())));
//			System.out.println(Float.toString(im.getCubeCenterPixel(cube.getHue(), cube.getSaturation())[0]));
//			System.out.println(Float.toString(im.getCubeCenterPixel(cube.getHue(), cube.getSaturation())[1]));
//			System.out.println(Float.toString(im.getMinimumDistanceSpherePixels(cube.getHue(), cube.getSaturation())));
		}
	}
	
	@Test
	public void testImageRecognizerCubeFactorRip() throws Exception{
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image = this.loadImageRGBBytes("4Cubes1Side.png");
		RealVector dronePos = new ArrayRealVector(new double[] {10,0,0});
		Image im = rec.createImage(image, 200, 200, 120, 120, dronePos, (float) Math.PI/2, 0.0f, 0.0f);
		for (ImageRecognizerCube cube : rec.getImageRecognizerCubes()){
			System.out.println(Float.toString(cube.getFactor()));
//			System.out.println(Float.toString(im.getTotalDistance(cube.getHue(), cube.getSaturation())));
//			System.out.println(Float.toString(im.getCubeCenterPixel(cube.getHue(), cube.getSaturation())[0]));
//			System.out.println(Float.toString(im.getCubeCenterPixel(cube.getHue(), cube.getSaturation())[1]));
//			System.out.println(Float.toString(im.getMinimumDistanceSpherePixels(cube.getHue(), cube.getSaturation())));
		}
	}
	
}
