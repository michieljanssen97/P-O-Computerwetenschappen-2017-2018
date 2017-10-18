package be.kuleuven.cs.robijn.common;

import static org.junit.Assert.*;

import org.apache.commons.math3.linear.*;
import org.junit.Test;

import be.kuleuven.cs.robijn.common.Drone;
import be.kuleuven.cs.robijn.testbed.VirtualTestbed;

public class DroneTest {
	
	float wingX = 4;
	float tailSize = 6;
	float engineMass = 20;
	float wingMass = 40;
	float tailMass = 30;
	float maxThrust = 60000;
	float maxAOA = ((float) (Math.PI));
	float wingLiftSlope = 15;
	float horStabLiftSlope = 10;
	float verStabLiftSlope = 10;
	RealVector velocity = new ArrayRealVector(new double[] {20,20,20},false);
	
	Drone drone = new Drone(wingX, tailSize, engineMass, wingMass, tailMass, maxThrust, maxAOA, wingLiftSlope
			, horStabLiftSlope, verStabLiftSlope, velocity);
	
	/**
	 * Test the getters of the drone's attributes
	 */
	@Test
	public void testDroneConfiguration() {
		assertEquals(wingX, drone.getWingX(), 0.00001);
		assertEquals(tailSize, drone.getTailSize(), 0.00001);
		assertEquals(engineMass, drone.getEngineMass(), 0.00001);
		assertEquals(wingMass, drone.getWingMass(), 0.00001);
		assertEquals(tailMass, drone.getTailMass(), 0.00001);
		assertEquals(maxThrust, drone.getMaxThrust(), 0.00001);
		assertEquals(maxAOA, drone.getMaxAOA(), 0.00001);
		assertEquals(wingLiftSlope, drone.getWingLiftSlope(), 0.00001);
		assertEquals(horStabLiftSlope, drone.getHorStabLiftSlope(), 0.00001);
		assertEquals(verStabLiftSlope, drone.getVerStabLiftSlope(), 0.00001);
		assertTrue(velocity.equals(drone.getVelocity()));
	}
	
	
	@Test
	public void testHeading() {
		float heading = (float) Math.PI; //Valid value
		drone.setHeading(heading);
		assertEquals(heading, drone.getHeading(), 0.00001);
		
		float heading2 = (float) (3*Math.PI); //Invalid value
		try{
			drone.setHeading(heading2);
		}
		catch (IllegalArgumentException e) {
			//Is ok
		}
	}
	
	@Test
	public void testPitch() {
		float pitch = (float) Math.PI; //Valid value
		drone.setPitch(pitch);
		assertEquals(pitch, drone.getPitch(), 0.00001);
		
		float pitch2 = (float) (3*Math.PI); //Invalid value
		try{
			drone.setPitch(pitch2);
		}
		catch (IllegalArgumentException e) {
			//Is ok
		}
	}
	
	@Test
	public void testRoll() {
		float roll = (float) Math.PI; //Valid value
		drone.setRoll(roll);
		assertEquals(roll, drone.getRoll(), 0.00001);
		
		float roll2 = (float) (3*Math.PI); //Invalid value
		try{
			drone.setRoll(roll2);
		}
		catch (IllegalArgumentException e) {
			//Is ok
		}
	}
	
	@Test
	public void testPosition() {
		RealVector pos = new ArrayRealVector(new double[] {4,7,5},false); //Valid value
		drone.setPosition(pos);
		assertTrue(pos.equals(drone.getPosition()));
		
		RealVector pos2 = null; //Invalid value
		try {
			drone.setPosition(pos2);
		}
		catch(IllegalArgumentException e) {
			//Is ok
		}
	}
	
	@Test
	public void testVelocity() {
		RealVector velocity =  new ArrayRealVector(new double[] {5,8,6}, false); //Valid value
		drone.setVelocity(velocity);
		assertTrue(velocity.equals(drone.getVelocity()));
		
		RealVector velocity2 = null; //Invalid value
		try {
			drone.setVelocity(velocity2);
		}
		catch(IllegalArgumentException e) {
			//Is ok
		}
	}
	
	@Test
	public void testRollTransformation() {
		RealVector position = new ArrayRealVector(new double[] {-2.6337,-0.47158,-1.2795});
		assertTrue(position.equals(drone.transformationToWorldCoordinates(drone.transformationToDroneCoordinates(position))));
	}
	
	public static void main(String args[]) {
		float wingX = 4;
		float tailSize = 6;
		float engineMass = 20;
		float wingMass = 40;
		float tailMass = 30;
		float maxThrust = 60000;
		float maxAOA = ((float) (Math.PI/3));
		float wingLiftSlope = 15;
		float horStabLiftSlope = 10;
		float verStabLiftSlope = 10;
		VirtualTestbed vtestbed = new VirtualTestbed();
		
		RealVector velocity = new ArrayRealVector(new double[] {20,20,20},false);
		Drone drone2 = new Drone(wingX, tailSize, engineMass, wingMass, tailMass, maxThrust, maxAOA, wingLiftSlope
				, horStabLiftSlope, verStabLiftSlope, velocity, vtestbed);
		
		drone2.setHeading((float) Math.PI/6);
		drone2.setPitch((float) Math.PI/6);
		drone2.setRoll((float) Math.PI/6);
		RealVector position = new ArrayRealVector(new double[] {1,1,1});
		System.out.print(position.equals(drone2.transformationToWorldCoordinates(drone2.transformationToDroneCoordinates(position)))+ "\n");
		System.out.print(position+ "\n");
		System.out.print(drone2.transformationToWorldCoordinates(drone2.transformationToDroneCoordinates(position)));
	}
}
