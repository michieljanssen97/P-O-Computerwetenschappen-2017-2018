package be.kuleuven.cs.robijn.common;


import org.apache.commons.math3.linear.*;

import be.kuleuven.cs.robijn.common.Drone;
import junit.framework.TestCase;
import p_en_o_cw_2017.AutopilotConfig;

public class DroneTest extends TestCase {
	
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
	RealVector velocity = new ArrayRealVector(new double[] {20,20,20},false);
	float horAngleOfView = (float) (Math.PI/3);
	float verAngleOfView = (float) (Math.PI/3);
	int nbColumns = 120;
	int nbRows = 120;
	
	
	public AutopilotConfig createAutopilotConfig() {
	    return new AutopilotConfig() {
	        public float getGravity() { return (float) 9.81; }
	        public float getWingX() { return wingX; }
	        public float getTailSize() { return tailSize; }
	        public float getEngineMass() { return engineMass; }
	        public float getWingMass() { return wingMass; }
	        public float getTailMass() { return tailMass; }
	        public float getMaxThrust() { return maxThrust; }
	        public float getMaxAOA() { return maxAOA; }
	        public float getWingLiftSlope() { return wingLiftSlope; }
	        public float getHorStabLiftSlope() { return horStabLiftSlope; }
	        public float getVerStabLiftSlope() { return verStabLiftSlope; }
	        public float getHorizontalAngleOfView() { return horAngleOfView; }
	        public float getVerticalAngleOfView() { return verAngleOfView; }
	        public int getNbColumns() { return nbColumns; }
	        public int getNbRows() { return nbRows; }
	    };
	}
	
	Drone drone = new Drone(createAutopilotConfig(), velocity);
	
	/**
	 * Test the getters of the drone's attributes
	 */
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
	
	
	
	public void testHeading() {
		float heading = (float) Math.PI; //Valid value
		drone.setHeading(heading);
		assertEquals(heading, drone.getHeading(), 0.00001);
		
		float heading2 = (float) (3*Math.PI); //Invalid value
		boolean thrown = false;
		try{
			drone.setHeading(heading2);
		}
		catch (IllegalArgumentException e) {
			thrown=true;
		}
		assertTrue(thrown);
	}
	
	
	public void testPitch() {
		float pitch = (float) Math.PI; //Valid value
		drone.setPitch(pitch);
		assertEquals(pitch, drone.getPitch(), 0.00001);
		
		float pitch2 = (float) (3*Math.PI); //Invalid value
		boolean thrown = false;
		try{
			drone.setPitch(pitch2);
		}
		catch (IllegalArgumentException e) {
			thrown=true;
		}
		assertTrue(thrown);
	}
	
	
	public void testRoll() {
		float roll = (float) Math.PI; //Valid value
		drone.setRoll(roll);
		assertEquals(roll, drone.getRoll(), 0.00001);
		
		float roll2 = (float) (3*Math.PI); //Invalid value
		boolean thrown = false;
		try{
			drone.setRoll(roll2);
		}
		catch (IllegalArgumentException e) {
			thrown=true;
		}
		assertTrue(thrown);
	}
	
	
	public void testPosition() {
		RealVector pos = new ArrayRealVector(new double[] {4,7,5},false); //Valid value
		drone.setPosition(pos);
		assertTrue(pos.equals(drone.getPosition()));
		
		RealVector pos2 = null; //Invalid value
		boolean thrown = false;
		try {
			drone.setPosition(pos2);
		}
		catch(IllegalArgumentException e) {
			thrown=true;
		}
		assertTrue(thrown);
	}
	
	
	public void testVelocity() {
		RealVector velocity =  new ArrayRealVector(new double[] {5,8,6}, false); //Valid value
		drone.setVelocity(velocity);
		assertTrue(velocity.equals(drone.getVelocity()));
		
		RealVector velocity2 = null; //Invalid value
		boolean thrown = false;
		try {
			drone.setVelocity(velocity2);
		}
		catch(IllegalArgumentException e) {
			thrown=true;
		}
		assertTrue(thrown);
	}
	
	public void testRollTransformation() {
		RealVector position = new ArrayRealVector(new double[] {-2.6337,-0.47158,-1.2795});
		assertTrue(position.equals(drone.transformationToWorldCoordinates(drone.transformationToDroneCoordinates(position))));
	}
	
    public boolean RealVectorEquals(RealVector v1, RealVector v2){ 
        if(v1.getDimension() == 0 || v2.getDimension()== 0){
            return false;
        }
        double epsilon = 0.000000001;
        return Math.abs(v1.getEntry(0)-v2.getEntry(0))<epsilon && Math.abs(v1.getEntry(1)-v2.getEntry(1))<epsilon && Math.abs(v1.getEntry(2)-v2.getEntry(2))<epsilon;
    }
	
    /*
     * test the transformation matrices
     */	
    
    public void testTransformationMatrices() {
		drone.setRoll((float) Math.PI/3);
		drone.setHeading((float) Math.PI/4);
		drone.setPitch((float) Math.PI/5);

		
		RealVector position = new ArrayRealVector(new double[] {0,2,3});
		//System.out.print(drone2.transformationToDroneCoordinates(position)+"\n"); -> klopte met andere groep
		//System.out.print(drone2.transformationToWorldCoordinates(position));
		assertTrue(RealVectorEquals(position,drone.transformationToWorldCoordinates(drone.transformationToDroneCoordinates(position))));
	}
}
