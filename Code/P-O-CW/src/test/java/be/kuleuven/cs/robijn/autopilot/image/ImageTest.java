package be.kuleuven.cs.robijn.autopilot.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.Test;

import javax.imageio.ImageIO;

import static org.junit.Assert.*;


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
		Image im = rec.createImage(image1, 10, 10, 120, 120);
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
		Image im = rec.createImage(image1, 200, 200, 120, 120);
		float[] center = im.getCenterPixel();
		float[] expected = {100,100};
		assertArrayEquals(expected, center, 0.01f);
	}
	
	@Test
	public void testRedCenterPixelCube3() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = rec.createImage(image1, 200, 200, 120, 120);
		float[] center = rec.getCubeAveragePixel(im, 0.0f, 1.0f);
		float[] expected = {104.77f, 97.275f};
		assertArrayEquals(expected, center, 0.01f);
	}
	
	@Test
	public void testRedCenterPixelCube3Corner() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = this.loadImageRGBBytes("cube3sidecorner.png");
		Image im = rec.createImage(image1, 200, 200, 120, 120);
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
		float expected = 39.647f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testMinimumDistanceSpherePixelsCube1Square() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube1sidesquare.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getMinimumDistanceSpherePixels(0.0f, 1.0f);
		float expected = 40.0f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testMinimumDistanceSpherePixelsCube2() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getMinimumDistanceSpherePixels(0.0f, 1.0f);
		float expected = 39.837f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testMaximumDistanceSpherePixelsCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getMaximumDistanceSpherePixels(0.0f, 1.0f);
		float expected = 66.2375f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testMaximumDistanceSpherePixelsCube3Corner() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3sidecorner.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getMaximumDistanceSpherePixels(0.0f, 1.0f);
		float expected = 66.478f;
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
		float result = im.getXYZDistance(0.0f, 1.0f).getX();
		float expected = 0.0926f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceYToCube2() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getXYZDistance(0.0f, 1.0f).getY();
		float expected = 0.0000797f;
		assertEquals(expected, result, 0.000001f);
	}
	
	@Test
	public void testDistanceZToCube2() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getXYZDistance(0.0f, 1.0f).getZ();
		float expected = -1.82666f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testTotalDistanceToCube2() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getTotalDistance(0.0f, 1.0f);
		float expected = 1.82f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceXToCube2Small() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2sidesmall.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getXYZDistance(0.0f, 1.0f).getX();
		float expected = 2.68f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceYToCube2Small() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2sidesmall.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getXYZDistance(0.0f, 1.0f).getY();
		float expected = 2.586f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceZToCube2Small() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2sidesmall.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getXYZDistance(0.0f, 1.0f).getZ();
		float expected = -3.389f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testTotalDistanceToCube2Small() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube2sidesmall.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getTotalDistance(0.0f, 1.0f);
		float expected = 5.036f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceXToCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getXYZDistance(0.0f, 1.0f).getX();
		float expected = 0.052f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceYToCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getXYZDistance(0.0f, 1.0f).getY();
		float expected = 0.03f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceZToCube3() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3side.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getXYZDistance(0.0f, 1.0f).getZ();
		float expected = -1.039f;
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
		float result = im.getXYZDistance(0.0f, 1.0f).getX();
		float expected = 0.32f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceYToCube3Corner() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3sidecorner.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getXYZDistance(0.0f, 1.0f).getY();
		float expected = 0.40f;
		assertEquals(expected, result, 0.01f);
	}
	
	@Test
	public void testDistanceZToCube3Corner() throws Exception {
		byte[] image1 = this.loadImageRGBBytes("cube3sidecorner.png");
		Image im = new Image(image1, 200, 200, 120, 120);
		float result = im.getXYZDistance(0.0f, 1.0f).getZ();
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
	
}
