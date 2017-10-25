package be.kuleuven.cs.robijn.common;

import org.apache.commons.math3.linear.*;
import be.kuleuven.cs.robijn.common.math.*;
import junit.framework.*;
import p_en_o_cw_2017.*;

public class DroneTest extends TestCase {
	
	private static float wingX = 4;
	private static float tailSize = 6;
	private static float engineMass = 20;
	private static float wingMass = 40;
	private static float tailMass = 30;
	private static float maxThrust = 60000;
	private static float maxAOA = ((float) (Math.PI/3));
	private static float wingLiftSlope = 15;
	private static float horStabLiftSlope = 10;
	private static float verStabLiftSlope = 10;
	private static RealVector velocity = new ArrayRealVector(new double[] {20,20,20},false);
	private static float horAngleOfView = (float) (Math.PI/3);
	private static float verAngleOfView = (float) (Math.PI/3);
	private static int nbColumns = 120;
	private static int nbRows = 120;
	private static AutopilotConfig config = new AutopilotConfig() {
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
	private static Drone drone = new Drone(config, velocity);
	
	public final void extendedConstructor_LegalCase() throws Exception {
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
		AutopilotConfig config = new AutopilotConfig() {
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
		Drone drone1 = new Drone(config, velocity);
		assertNotNull(drone1);
		assertEquals(wingX, drone1.getWingX(), 0.00001);
		assertEquals(tailSize, drone1.getTailSize(), 0.00001);
		assertEquals(engineMass, drone1.getEngineMass(), 0.00001);
		assertEquals(wingMass, drone1.getWingMass(), 0.00001);
		assertEquals(tailMass, drone1.getTailMass(), 0.00001);
		assertEquals(maxThrust, drone1.getMaxThrust(), 0.00001);
		assertEquals(maxAOA, drone1.getMaxAOA(), 0.00001);
		assertEquals(wingLiftSlope, drone1.getWingLiftSlope(), 0.00001);
		assertEquals(horStabLiftSlope, drone1.getHorStabLiftSlope(), 0.00001);
		assertEquals(verStabLiftSlope, drone1.getVerStabLiftSlope(), 0.00001);
		assertTrue(VectorMath.fuzzyEquals(velocity, drone1.getVelocity()));
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
		drone.setRelativePosition(pos);
		assertTrue(pos.equals(drone.getWorldPosition()));
		
		RealVector pos2 = null; //Invalid value
		boolean thrown = false;
		try {
			drone.setRelativePosition(pos2);
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
	
    /*
     * test the transformation matrices
     */
    public void testTransformationMatrices() {
		drone.setRoll((float) Math.PI/3);
		drone.setHeading((float) Math.PI/4);
		drone.setPitch((float) Math.PI/5);

		
		RealVector position = new ArrayRealVector(new double[] {0,2,3});
		assertTrue(VectorMath.fuzzyEquals(position,drone.transformationToWorldCoordinates(drone.transformationToDroneCoordinates(position))));
	}
}
