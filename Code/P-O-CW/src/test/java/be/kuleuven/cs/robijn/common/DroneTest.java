package be.kuleuven.cs.robijn.common;

import org.apache.commons.math3.linear.*;
import be.kuleuven.cs.robijn.common.math.*;
import be.kuleuven.cs.robijn.worldObjects.Drone;

import org.junit.jupiter.api.Test;
import interfaces.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A class with tests for the class Drone.
 * 
 * @author Pieter Vandensande en Roy De Prins
 */
public class DroneTest {
	
	private static final double EPSILON = 0.0001;
	
	private static float wingX = 4.2f;
	private static float tailSize = 4.2f;
	private static float engineMass = 180;
	private static float wingMass = 100;
	private static float tailMass = 100;
	private static float maxThrust = 2000;
	private static float maxAOA = ((float) (Math.PI/12));
	private static float wingLiftSlope = 10;
	private static float horStabLiftSlope = 5;
	private static float verStabLiftSlope = 5;
	private static RealVector velocity = new ArrayRealVector(new double[] {20,20,20},false);
	private static float horAngleOfView = (float) (Math.PI/3);
	private static float verAngleOfView = (float) (Math.PI/3);
	private static int nbColumns = 120;
	private static int nbRows = 120;
	private static String droneID = "drone";
	private static float wheelY = -1.22f;
	private static float rearWheelX = 1.4f;
	private static float rearWheelZ = 1f;
	private static float frontWheelZ = -2f;
	private static float tyreSlope = 40875f;
	private static float dampSlope = 470f;
	private static float RMax = 4316f;
	private static float fcMax = 0.7f;
	private static float tyreRadius = 0.22f;
	
	private static AutopilotConfig config = new AutopilotConfig() {
		@Override
		public String getDroneID() {
			return droneID;
		}

		public float getGravity() { return (float) 9.81; }
        public float getWingX() { return wingX; }
        public float getTailSize() { return tailSize; }

		@Override
		public float getWheelY() {
			return wheelY;
		}

		@Override
		public float getFrontWheelZ() {
			return frontWheelZ;
		}

		@Override
		public float getRearWheelZ() {
			return rearWheelZ;
		}

		@Override
		public float getRearWheelX() {
			return rearWheelX;
		}

		@Override
		public float getTyreSlope() {
			return tyreSlope;
		}

		@Override
		public float getDampSlope() {
			return dampSlope;
		}

		@Override
		public float getTyreRadius() {
			return tyreRadius;
		}

		@Override
		public float getRMax() {
			return RMax;
		}

		@Override
		public float getFcMax() {
			return fcMax;
		}

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

	@Test
	public final void testExtendedConstructor_LegalCase() throws Exception {
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
			@Override
			public String getDroneID() {
				return droneID;
			}

			public float getGravity() { return (float) 9.81; }
	        public float getWingX() { return wingX; }
	        public float getTailSize() { return tailSize; }

			@Override
			public float getWheelY() {
				return wheelY;
			}

			@Override
			public float getFrontWheelZ() {
				return frontWheelZ;
			}

			@Override
			public float getRearWheelZ() {
				return rearWheelZ;
			}

			@Override
			public float getRearWheelX() {
				return rearWheelX;
			}

			@Override
			public float getTyreSlope() {
				return tyreSlope;
			}

			@Override
			public float getDampSlope() {
				return dampSlope;
			}

			@Override
			public float getTyreRadius() {
				return tyreRadius;
			}

			@Override
			public float getRMax() {
				return RMax;
			}

			@Override
			public float getFcMax() {
				return fcMax;
			}

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

	@Test
	public final void testSetHeading_LegalCase_IllegalCase() {
		float heading = (float) Math.PI; //Valid value
		drone.setHeading(heading);
		assertEquals(heading, drone.getHeading(), EPSILON);
		
		float heading2 = (float) (3*Math.PI); //Invalid value
		assertThrows(IllegalArgumentException.class, ()->{
			drone.setHeading(heading2);
		});
	}

	@Test
	public final void testSetPitch_LegalCase_IllegalCase() {
		float pitch = (float) Math.PI; //Valid value
		drone.setPitch(pitch);
		assertEquals(pitch, drone.getPitch(), EPSILON);
		
		float pitch2 = (float) (3*Math.PI); //Invalid value
		assertThrows(IllegalArgumentException.class, ()->{
			drone.setPitch(pitch2);
		});
	}

	@Test
	public final void testSetRoll_LegalCase_IllegalCase() {
		float roll = (float) Math.PI; //Valid value
		drone.setRoll(roll);
		assertEquals(roll, drone.getRoll(), 0.00001);
		
		float roll2 = (float) (3*Math.PI); //Invalid value
		assertThrows(IllegalArgumentException.class, ()->{
			drone.setRoll(roll2);
		});
	}

	@Test
	public final void testSetPosition_LegalCase_IllegalCase() {
		RealVector pos = new ArrayRealVector(new double[] {4,7,5},false); //Valid value
		drone.setRelativePosition(pos);
		assertTrue(VectorMath.fuzzyEquals(pos, drone.getWorldPosition()));
		
		RealVector pos2 = null; //Invalid value
		assertThrows(NullPointerException.class, ()->{
			drone.setRelativePosition(pos2);
		});
	}

	@Test
	public final void testSetVelocity_LegalCase_IllegalCase() {
		RealVector velocity =  new ArrayRealVector(new double[] {5,8,6}, false); //Valid value
		drone.setVelocity(velocity);
		assertTrue(VectorMath.fuzzyEquals(velocity, drone.getVelocity()));
		
		RealVector velocity2 = null; //Invalid value
		assertThrows(IllegalArgumentException.class, ()->{
			drone.setVelocity(velocity2);
		});
	}

	@Test
	public final void testTransformationToWorldCoordinates_SingleCase() {
		drone.setRoll(0);
		drone.setHeading(0);
		drone.setPitch(0);
		RealVector positionDroneCoordinates = new ArrayRealVector(new double[] {-2.6337,-0.47158,-1.2795}, false);
		assertTrue(VectorMath.fuzzyEquals(new ArrayRealVector(new double[] {-2.6337,-0.47158,-1.2795}, false),
				drone.transformationToWorldCoordinates(positionDroneCoordinates)));
	}

	@Test
    public final void testTransformationToDroneCoordinates_SingleCase() {
		drone.setRoll((float) Math.PI/3);
		drone.setHeading((float) Math.PI/4);
		drone.setPitch((float) Math.PI/5);
		RealVector positionWorldCoordinates = new ArrayRealVector(new double[] {0,2,3}, false);
		assertTrue(VectorMath.fuzzyEquals(new ArrayRealVector(new double[] {1.4204288832, 3.269574698, 0.5406136163}, false),
				drone.transformationToDroneCoordinates(positionWorldCoordinates)));
	}
}
