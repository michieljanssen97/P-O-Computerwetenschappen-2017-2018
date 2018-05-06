//package be.kuleuven.cs.robijn.common;
//
//import static org.junit.Assert.*;
//
//import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
//import org.apache.commons.math3.linear.ArrayRealVector;
//import org.apache.commons.math3.linear.RealVector;
//import org.junit.Test;
//
//import be.kuleuven.cs.robijn.autopilot.AutopilotModule;
//import be.kuleuven.cs.robijn.common.airports.Airport;
//import be.kuleuven.cs.robijn.common.airports.AirportPackage;
//import be.kuleuven.cs.robijn.common.airports.AirportPackage.State;
//import be.kuleuven.cs.robijn.common.airports.Gate;
//import be.kuleuven.cs.robijn.worldObjects.Drone;
//import be.kuleuven.cs.robijn.worldObjects.WorldObject;
//import interfaces.AutopilotConfig;
//
//public class AirportPackageTest {
//	
//	private static float wingX = 4.2f;
//	private static float tailSize = 4.2f;
//	private static float engineMass = 180;
//	private static float wingMass = 100;
//	private static float tailMass = 100;
//	private static float maxThrust = 2000;
//	private static float maxAOA = ((float) (Math.PI/12));
//	private static float wingLiftSlope = 10;
//	private static float horStabLiftSlope = 5;
//	private static float verStabLiftSlope = 5;
//	private static RealVector velocity = new ArrayRealVector(new double[] {0,0,0},false);
//	private static float horAngleOfView = (float) (Math.PI/3);
//	private static float verAngleOfView = (float) (Math.PI/3);
//	private static int nbColumns = 120;
//	private static int nbRows = 120;
//	private static String droneID = "drone";
//	private static float wheelY = -1.22f;
//	private static float rearWheelX = 1.4f;
//	private static float rearWheelZ = 1f;
//	private static float frontWheelZ = -2f;
//	private static float tyreSlope = 40875f;
//	private static float dampSlope = 470f;
//	private static float RMax = 4316f;
//	private static float fcMax = 0.7f;
//	private static float tyreRadius = 0.22f;
//	
//	private static AutopilotConfig config = new AutopilotConfig() {
//		@Override
//		public String getDroneID() {
//			return droneID;
//		}
//
//		public float getGravity() { return (float) 9.81; }
//        public float getWingX() { return wingX; }
//        public float getTailSize() { return tailSize; }
//
//		@Override
//		public float getWheelY() {
//			return wheelY;
//		}
//
//		@Override
//		public float getFrontWheelZ() {
//			return frontWheelZ;
//		}
//
//		@Override
//		public float getRearWheelZ() {
//			return rearWheelZ;
//		}
//
//		@Override
//		public float getRearWheelX() {
//			return rearWheelX;
//		}
//
//		@Override
//		public float getTyreSlope() {
//			return tyreSlope;
//		}
//
//		@Override
//		public float getDampSlope() {
//			return dampSlope;
//		}
//
//		@Override
//		public float getTyreRadius() {
//			return tyreRadius;
//		}
//
//		@Override
//		public float getRMax() {
//			return RMax;
//		}
//
//		@Override
//		public float getFcMax() {
//			return fcMax;
//		}
//
//		public float getEngineMass() { return engineMass; }
//        public float getWingMass() { return wingMass; }
//        public float getTailMass() { return tailMass; }
//        public float getMaxThrust() { return maxThrust; }
//        public float getMaxAOA() { return maxAOA; }
//        public float getWingLiftSlope() { return wingLiftSlope; }
//        public float getHorStabLiftSlope() { return horStabLiftSlope; }
//        public float getVerStabLiftSlope() { return verStabLiftSlope; }
//        public float getHorizontalAngleOfView() { return horAngleOfView; }
//        public float getVerticalAngleOfView() { return verAngleOfView; }
//        public int getNbColumns() { return nbColumns; }
//        public int getNbRows() { return nbRows; }
//    };
//    
//    private WorldObject world = new WorldObject();
//	private Airport airport1 = new Airport(0, 1000, 1000, new Vector2D(0,0));
//	private Airport airport2 = new Airport(1, 1000, 1000, new Vector2D(0, -5000));
//	
//	@Test
//	public void testNewPackageOnAirportDrone() {
//		Drone drone1 = new Drone(config, velocity); //TODO zorg dat hij op airport1 staat
//		Airport fromAirport = airport1;
//		Airport toAirport = airport2;
//		Gate fromGate = fromAirport.getGates()[0];
//		Gate toGate = toAirport.getGates()[0];
//		assertEquals(AirportPackage.getAllPackagesToAssign().size(),0);
//		AutopilotModule module = new AutopilotModule(world);
//		module.deliverPackage(fromAirport, fromGate, toAirport, toGate);
//		assertEquals(AirportPackage.getAllPackagesToAssign().size(),0);
//		assertTrue(drone1.hasPackage());
//		AirportPackage airportPackage = drone1.getPackage();
//		assertEquals(airportPackage.getState(), State.IN_TRANSIT);
//		assertEquals(airportPackage.getCurrentGate(), fromGate);
//		assertEquals(airportPackage.getOrigin(), fromGate);
//		assertEquals(airportPackage.getCurrentTransporter(), drone1);
//		assertEquals(airportPackage.getDestination(), toGate);		
//	}
//	
//	@Test
//	public void testNewPackageNotOnAirportDrone() {
//		Drone drone1 = new Drone(config, velocity); //TODO zorg dat hij op airport1 staat
//		Airport fromAirport = airport2;
//		Airport toAirport = airport1;
//		Gate fromGate = fromAirport.getGates()[0];
//		Gate toGate = toAirport.getGates()[0];
//		assertEquals(AirportPackage.getAllPackagesToAssign().size(),0);
//		AutopilotModule module = new AutopilotModule(world);
//		module.deliverPackage(fromAirport, fromGate, toAirport, toGate);
//		assertEquals(AirportPackage.getAllPackagesToAssign().size(),0);
//		assertTrue(drone1.hasPackageWaiting());
//		assertFalse(drone1.hasPackage());
//	}
//	
//	@Test
//	public void TestNewPackageNoDrone() {
//		Airport fromAirport = airport2;
//		Airport toAirport = airport1;
//		Gate fromGate = fromAirport.getGates()[0];
//		Gate toGate = toAirport.getGates()[0];
//		assertEquals(AirportPackage.getAllPackagesToAssign().size(),0);
//		AutopilotModule module = new AutopilotModule(world);
//		module.deliverPackage(fromAirport, fromGate, toAirport, toGate);
//		assertEquals(AirportPackage.getAllPackagesToAssign().size(),1);
//		AirportPackage airportPackage = AirportPackage.getAllPackagesToAssign().get(0);
//		assertEquals(airportPackage.getState(), State.AT_GATE);
//	}
//
//}
