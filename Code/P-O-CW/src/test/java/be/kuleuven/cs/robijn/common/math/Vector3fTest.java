package be.kuleuven.cs.robijn.common.math;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;



public class Vector3fTest {
	
	private static float EPSILON = 0.0001f;
	private Vector3f vector;
	
	@Before
	public void setUp() {
		vector = new Vector3f();
	}
	
	@Test
	public void testGetXYZ(){
		assertEquals(vector.getX(),0f,EPSILON);
		assertEquals(vector.getY(),0f,EPSILON);
		assertEquals(vector.getZ(),0f,EPSILON);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testInfinityXArgument() throws Exception{
		vector = new Vector3f(Float.POSITIVE_INFINITY, 1.2f, 1.2f);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInfinityYArgument() throws Exception{
		vector = new Vector3f(1.2f,Float.POSITIVE_INFINITY, 1.2f);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testInfinityZArgument() throws Exception{
		vector = new Vector3f(1.2f, 1.2f,Float.POSITIVE_INFINITY);
	}
	@Test(expected =IllegalArgumentException.class)
	public void testXArgumentIsNaN() throws Exception{
		vector = new Vector3f(Float.NaN, 1.2f, 1.2f);
	}
	@Test(expected =IllegalArgumentException.class)
	public void testYArgumentIsNaN() throws Exception{
		vector = new Vector3f(1.2f,Float.NaN,1.2f);
	}
	@Test(expected =IllegalArgumentException.class)
	public void testZArgumentIsNaN() throws Exception{
		vector = new Vector3f(1.2f, 1.2f,Float.NaN);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testSetX() throws Exception{
		vector.setX(Float.NEGATIVE_INFINITY);
	}
	@Test 
	public void testSetY(){
		Vector3f newvec = vector.setY(10f);
		assertEquals(newvec.getX(), 0,EPSILON);
		assertEquals(newvec.getY(), 10,EPSILON);
		assertEquals(newvec.getZ(), 0,EPSILON);
	}
	@Test 
	public void testSetZ(){
		Vector3f newvec = vector.setZ(120f);
		assertEquals(newvec.getX(), 0,EPSILON);
		assertEquals(newvec.getY(), 0,EPSILON);
		assertEquals(newvec.getZ(), 120,EPSILON);
	}
	
	@Test 
	public void testSubtractVector(){
		Vector3f vec = new Vector3f(3,5,4); 
		Vector3f res = vector.subtract(vec);
		assertEquals(res.getX(), -3 ,EPSILON);
		assertEquals(res.getY(), -5 ,EPSILON);
		assertEquals(res.getZ(), -4 ,EPSILON);
	}
	@Test 
	public void testSumVector(){
		Vector3f vec = new Vector3f(10.2f,5.0f,4.1f); 
		Vector3f res = vector.sum(vec);
		assertEquals(res.getX(), 10.2 ,EPSILON);
		assertEquals(res.getY(), 5.0,EPSILON);
		assertEquals(res.getZ(), 4.1 ,EPSILON);
	}
	@Test
	public void testLengthVector() {
		Vector3f vec = new Vector3f(10f,4f,5f);
		assertEquals(vec.length(),11.87434 ,EPSILON);
	}
	@Test
	public void testUnitVector(){
		Vector3f vec = new Vector3f(3f,4f,5f);
		Vector3f res = vec.unit();
		assertEquals(res.getX(),0.42426,EPSILON);
		assertEquals(res.getY(),0.56569,EPSILON);
		assertEquals(res.getZ(),0.70711,EPSILON);
	}
	@Test
	public void testScaleVector(){
		Vector3f vec = new Vector3f(3f,4f,5f);
		Vector3f res = vec.scale(3f);
		assertEquals(res.getX(),9f,EPSILON);
		assertEquals(res.getY(),12f,EPSILON);
		assertEquals(res.getZ(),15f,EPSILON);
	}
	@Test
	public void testDotVector(){
		Vector3f vec = new Vector3f(3f,4f,5f);
		Vector3f vec2 = new Vector3f(1f,2f,6f);
		float res = vec.dot(vec2);
		assertEquals(res, 41,EPSILON);		
	}
	@Test
	public void testPositiveTranslateVector(){
		Vector3f res = vector.translate(-1,-1320, 145);
		assertEquals(res.getX(),-1,EPSILON);
		assertEquals(res.getY(),-1320,EPSILON);
		assertEquals(res.getZ(),145,EPSILON);
	}
	@Test
	public void testFuzzyEqualsVector(){
		Vector3f vec = new Vector3f(1,1,1);
		Vector3f vec2 = new Vector3f(1,1,1);
		assertTrue(vec.fuzzyEquals(vec2, EPSILON));
	}
	@Test
	public void testCrossProductVector(){
		Vector3f vec = new Vector3f(3f,4f,5f);
		Vector3f vec2 = new Vector3f(1f,2f,6f);
		Vector3f res = vec.crossProduct(vec2);
		assertEquals(res.getX(), 14, EPSILON);
		assertEquals(res.getY(), -13, EPSILON);
		assertEquals(res.getZ(), 2, EPSILON);
	}

}

