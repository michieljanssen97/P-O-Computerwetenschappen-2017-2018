package be.kuleuven.cs.robijn.common.math;

import junit.framework.*;

import org.junit.Test;
public class Vector3fTest extends TestCase {
	
	private static double EPSILON = 0.0001;
	private Vector3f vector;
	
	public void setUp() {
		vector = new Vector3f();
	}
	
	public void testGetXYZ(){
		assertEquals(vector.getX(),0.0,EPSILON);
		assertEquals(vector.getY(),0.0,EPSILON);
		assertEquals(vector.getZ(),0.0,EPSILON);
	}
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidArgument() throws Exception{
		vector = new Vector3f(Float.POSITIVE_INFINITY, 1.2f, 1.2f);
	}
}
