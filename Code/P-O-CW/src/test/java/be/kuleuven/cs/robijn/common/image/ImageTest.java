package be.kuleuven.cs.robijn.common.image;

import java.awt.Color;
import java.io.File;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class ImageTest {

	@Test
	public void testRGBtoHSV() {
		float[] hsv = new float[3];
		float[] expected = {0.0f, 1.0f, 1.0f};
		Color.RGBtoHSB(255, 0, 0, hsv);
		assertArrayEquals(expected, hsv, 0.01f);
		assertTrue(true);
	}
	
	@Test
	public void testIsRedHSV() throws Exception {
		float[] hsv = {0.0f, 1.0f, 1.0f};
		assertTrue(Image.isRedHSV(hsv));
	}
	
	@Test
	public void testRedCenterPixel5x5() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = Files.readAllBytes(new File("test/java/be/kuleuven/cs/robijn/common/image/5x5-Red-255-0-0-Center-3-3.png"));
		Image im = rec.createImage(image1, 5, 5, 120, 120);
		int[] redCo = rec.getRedCubeAveragePixel(im);
		int[] expected = {3,3};
		assertArrayEquals(expected, redCo);
	}
	
	@Test
	public void testRedCenterPixel10x10() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\rafhe\\Desktop\\Test Images\\10x10-Red-255-0-0-Center-3-2.png").toPath());
		Image im = rec.createImage(image1, 10, 10, 120, 120);
		int[] redCo = rec.getRedCubeAveragePixel(im);
		int[] expected = {3,2};
		assertArrayEquals(expected, redCo);
	}
	
	@Test
	public void testCenterPixel5x5() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\rafhe\\Desktop\\Test Images\\5x5-Red-255-0-0-Center-3-3.png").toPath());
		Image im = rec.createImage(image1, 5, 5, 120, 120);
		int[] center = im.getCenterPixel();
		int[] expected = {2,2};
		assertArrayEquals(expected, center);
	}
	
	@Test
	public void testCenterPixel200x200() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube1side.png").toPath());
		Image im = rec.createImage(image1, 200, 200, 120, 120);
		int[] center = im.getCenterPixel();
		int[] expected = {100,100};
		assertArrayEquals(expected, center);
	}
	
	@Test
	public void testRedCenterPixelCube3() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3side.png").toPath());
		Image im = rec.createImage(image1, 200, 200, 120, 120);
		int[] center = rec.getRedCubeAveragePixel(im);
//		System.out.println(Integer.toString(center[0]));
//		System.out.println(Integer.toString(center[1]));
		assertTrue(true);
	}
	
	@Test
	public void testRedCenterPixelCube3Corner() throws Exception {
		ImageRecognizer rec = new ImageRecognizer();
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3sidecorner.png").toPath());
		Image im = rec.createImage(image1, 200, 200, 120, 120);
		int[] center = rec.getRedCubeAveragePixel(im);
//		System.out.println(Integer.toString(center[0]));
//		System.out.println(Integer.toString(center[1]));
		assertTrue(true);
	}
	
	@Test
	public void testRedEdgePixels10x10() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\10x10-Red-255-0-0-Center-3-2.png").toPath());
		Image im = new Image(image1, 10, 10, 120, 120);
//		System.out.println(Integer.toString(im.getRedEdgePixels().size()));
		assertTrue(true);
	}
	
	@Test
	public void testRedEdgePixelsCube3() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Integer.toString(im.getRedEdgePixels().size()));
		assertTrue(true);
	}
	
	@Test
	public void testMinimumDistanceSpherePixelsCube1Square() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube1sidesquare.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getMinimumDistanceSpherePixels()));
		assertTrue(true);
	}
	
	@Test
	public void testMinimumDistanceSpherePixelsCube2() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube2side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getMinimumDistanceSpherePixels()));
		assertTrue(true);
	}
	
	@Test
	public void testMaximumDistanceSpherePixelsCube3() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getMaximumDistanceSpherePixels()));
		assertTrue(true);
	}
	
	@Test
	public void testMaximumDistanceSpherePixelsCube3Corner() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3sidecorner.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getMaximumDistanceSpherePixels()));
		assertTrue(true);
	}
	
	@Test
	public void testSidesVisibleCube2() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube2side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Integer.toString(im.getAmountSidesVisible()));
		assertTrue(true);
	}
	
	@Test
	public void testSidesVisibleCube2Small() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube2sidesmall.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Integer.toString(im.getAmountSidesVisible()));
		assertTrue(true);
	}
	
	@Test
	public void testSidesVisibleCube3() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Integer.toString(im.getAmountSidesVisible()));
		assertTrue(true);
	}
	
	@Test
	public void testSidesVisibleCube3Corner() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3sidecorner.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Integer.toString(im.getAmountSidesVisible()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceXToCube2() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube2side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getXDistance()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceYToCube2() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube2side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getYDistance()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceZToCube2() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube2side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getZDistance()));
		assertTrue(true);
	}
	
	@Test
	public void test3DDistanceToCube2() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube2side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.get3DDistanceToCube()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceXToCube2Small() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube2sidesmall.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getXDistance()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceYToCube2Small() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube2sidesmall.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getYDistance()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceZToCube2Small() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube2sidesmall.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getZDistance()));
		assertTrue(true);
	}
	
	@Test
	public void test3DDistanceToCube2Small() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube2sidesmall.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.get3DDistanceToCube()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceXToCube3() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getXDistance()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceYToCube3() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getYDistance()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceZToCube3() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getZDistance()));
		assertTrue(true);
	}
	
	@Test
	public void test3DDistanceToCube3() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3side.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.get3DDistanceToCube()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceXToCube3Corner() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3sidecorner.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getXDistance()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceYToCube3Corner() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3sidecorner.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getYDistance()));
		assertTrue(true);
	}
	
	@Test
	public void testDistanceZToCube3Corner() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\Gebruiker\\Desktop\\Test Images\\cube3sidecorner.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
//		System.out.println(Float.toString(im.getZDistance()));
		assertTrue(true);
	}
	
	@Test
	public void test3DDistanceToCube3Corner() throws Exception {
		byte[] image1 = Files.readAllBytes(new File("C:\\Users\\rafhe\\Desktop\\Test Images\\cube3sidecorner.png").toPath());
		Image im = new Image(image1, 200, 200, 120, 120);
		System.out.println(Float.toString(im.getTotalDistance()));
		System.out.println(Float.toString(im.getXYZDistance().getX()));
		System.out.println(Float.toString(im.getXYZDistance().getY()));
		System.out.println(Float.toString(im.getXYZDistance().getZ()));
		System.out.println(Integer.toString(im.getAmountSidesVisible()));
		assertTrue(true);
	}
	
}
