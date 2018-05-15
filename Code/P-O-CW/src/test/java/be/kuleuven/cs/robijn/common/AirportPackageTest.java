package be.kuleuven.cs.robijn.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.jupiter.api.Test;

import be.kuleuven.cs.robijn.autopilot.AutopilotModule;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.AirportPackage;
import be.kuleuven.cs.robijn.common.airports.AirportPackage.State;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.common.airports.Runway;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import interfaces.AutopilotConfig;

public class AirportPackageTest {
	
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
	private static RealVector velocity = new ArrayRealVector(new double[] {0,0,0},false);
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
    

    @Test
	public void testNewPackageNoAvailableDrone() {
		WorldObject world = new WorldObject();
		Airport airport1 = new Airport(0, 1000, 500, new Vector2D(0,0));
		airport1.setRelativePosition(new ArrayRealVector(new double[] {0,0,0}, false));
		Airport airport2 = new Airport(1, 1000, 500, new Vector2D(0, 0));
		airport2.setRelativePosition(new ArrayRealVector(new double[] {-10000,0,60000}, false));
		Drone drone = new Drone(config, velocity);
		world.addChild(airport1);
		world.addChild(airport2);
		world.addChild(drone);
		
		Airport fromAirport = airport2;
		Airport toAirport = airport1;
		Gate fromGate = fromAirport.getGates()[0];
		Gate toGate = toAirport.getGates()[0];
		
		//Set the position of the Drone at no airport
		RealVector gatePos = fromGate.getWorldPosition();
        RealVector dronePos = gatePos.add(
                new ArrayRealVector(new double[]{
                        -5000000,
                        -drone.getConfig().getWheelY() + drone.getConfig().getTyreRadius(),
                        -5000000
                }, false)
        );
        drone.setRelativePosition(dronePos);
        drone.setToAirport();
        
        Airport firstAirport = world.getFirstChildOfType(Airport.class);
        
        assertEquals(firstAirport.getAllAirports().size(), 2);
//		assertEquals(AirportPackage.getAllPackagesToAssign().size(),0);
		AutopilotModule module = new AutopilotModule(world);		
		module.deliverPackage(fromAirport, fromGate, toAirport, toGate);
        AirportPackage airPackage = world.getFirstChildOfType(AirportPackage.class);
		airPackage.assignPackages();
		
		
		for(Airport air : firstAirport.getAllAirports()) {
			assertEquals(0, air.getCurrentDrones().size());
		}
		assertEquals(airPackage.getAllPackagesToAssign().size(),1);
		AirportPackage p = airPackage.getAllPackagesToAssign().get(0);
		assertEquals(p.getState(), State.AT_GATE);
		
		
		removeAllChildren(world);
	}
	
	@Test
	public void testNewPackageOnAirportDrone() {
	    WorldObject world = new WorldObject();
		Airport airport1 = new Airport(0, 1000, 500, new Vector2D(0,0));
		airport1.setRelativePosition(new ArrayRealVector(new double[] {55000,0,-34256}, false));
		Airport airport2 = new Airport(1, 1000, 500, new Vector2D(0,0));
		airport2.setRelativePosition(new ArrayRealVector(new double[] {-10000,0,60000}, false));
		Drone drone = new Drone(config, velocity);
		world.addChild(drone);
		world.addChild(airport1);
		world.addChild(airport2);
		
		Airport fromAirport = airport1;
		Airport toAirport = airport2;
		Gate fromGate = fromAirport.getGates()[0];
		Gate toGate = toAirport.getGates()[0];
		
		//Set position of drone at airport1
		RealVector gatePos = fromGate.getWorldPosition();
        RealVector dronePos = gatePos.add(
                new ArrayRealVector(new double[]{
                		0,
                        -drone.getConfig().getWheelY() + drone.getConfig().getTyreRadius(),
                        0
                }, false)
        );
        drone.setRelativePosition(dronePos);
        drone.setToAirport();
        
        Airport firstAirport = world.getFirstChildOfType(Airport.class);
		
        assertEquals(firstAirport.getAllAirports().size(), 2);
//		assertEquals(AirportPackage.getAllPackagesToAssign().size(),0);
		AutopilotModule module = new AutopilotModule(world);
		module.deliverPackage(fromAirport, fromGate, toAirport, toGate);
        AirportPackage airPackage = world.getFirstChildOfType(AirportPackage.class);
        airPackage.assignPackages();
		assertEquals(airPackage.getAllPackagesToAssign().size(),0);
		assertTrue(drone.hasPackage());
		AirportPackage airportPackage = drone.getPackage();
		assertEquals(airportPackage.getState(), State.IN_TRANSIT);
		assertEquals(airportPackage.getCurrentGate(), null);
		assertEquals(airportPackage.getOrigin(), fromGate);
		assertEquals(airportPackage.getCurrentTransporter(), drone);
		assertEquals(airportPackage.getDestination(), toGate);	
		
		removeAllChildren(world);
	}
	
	@Test
	public void testNewPackageNotOnAirportDrone() {
		WorldObject world = new WorldObject();
		Airport airport1 = new Airport(0, 1000, 500, new Vector2D(0,0));
		airport1.setRelativePosition(new ArrayRealVector(new double[] {0,0,0}, false));
		Airport airport2 = new Airport(1, 1000, 500, new Vector2D(0, 0));
		airport2.setRelativePosition(new ArrayRealVector(new double[] {-10000,0,60000}, false));
		Drone drone = new Drone(config, velocity);
		world.addChild(drone);
		world.addChild(airport1);
		world.addChild(airport2);
		
		Airport fromAirport = airport2;
		Airport toAirport = airport1;
		Gate fromGate = fromAirport.getGates()[0];
		Gate toGate = toAirport.getGates()[0];
		
		//Set position of drone at airport1
        RealVector gatePos = toGate.getWorldPosition();
        RealVector dronePos = gatePos.add(
                new ArrayRealVector(new double[]{
                        0,
                        -drone.getConfig().getWheelY() + drone.getConfig().getTyreRadius(),
                        0
                }, false)
        );
        drone.setRelativePosition(dronePos);
        drone.setToAirport();
		
        
        Airport firstAirport = world.getFirstChildOfType(Airport.class);
        
        assertEquals(firstAirport.getAllAirports().size(), 2);
//		assertEquals(AirportPackage.getAllPackagesToAssign().size(),0);
		AutopilotModule module = new AutopilotModule(world);
		module.deliverPackage(fromAirport, fromGate, toAirport, toGate);
        AirportPackage airPackage = world.getFirstChildOfType(AirportPackage.class);
		airPackage.assignPackages();
		assertEquals(airPackage.getAllPackagesToAssign().size(),1);
		assertTrue(!drone.hasPackage());
		
		assertEquals(1, airport1.getCurrentDrones().size());
		assertEquals(0, airport2.getCurrentDrones().size());
		
		removeAllChildren(world);
	}	
	
	@Test
	public void testNewInvalidPackage() {
		WorldObject world = new WorldObject();
		Airport airport1 = new Airport(0, 1000, 500, new Vector2D(0,0));
		airport1.setRelativePosition(new ArrayRealVector(new double[] {0,0,0}, false));
		Airport airport2 = new Airport(1, 1000, 500, new Vector2D(0, 0));
		airport2.setRelativePosition(new ArrayRealVector(new double[] {-10000,0,60000}, false));
		Drone drone = new Drone(config, velocity);
		world.addChild(drone);
		world.addChild(airport1);
		world.addChild(airport2);
		
		Airport fromAirport = airport2;
		Airport toAirport = airport1;
		Gate fromGate = fromAirport.getGates()[0];
		Gate toGate = toAirport.getGates()[0];
		
		//Set position of drone at airport1
        RealVector gatePos = toGate.getWorldPosition();
        RealVector dronePos = gatePos.add(
                new ArrayRealVector(new double[]{
                        0,
                        -drone.getConfig().getWheelY() + drone.getConfig().getTyreRadius(),
                        0
                }, false)
        );
        drone.setRelativePosition(dronePos);
        drone.setToAirport();
        
        Airport firstAirport = world.getFirstChildOfType(Airport.class);
        
        assertEquals(firstAirport.getAllAirports().size(), 2);
//		assertEquals(AirportPackage.getAllPackagesToAssign().size(),0);
		AutopilotModule module = new AutopilotModule(world);
		module.deliverPackage(fromAirport, fromGate, toAirport, toGate);
        AirportPackage airPackage = world.getFirstChildOfType(AirportPackage.class);
		assertEquals(airPackage.getAllPackagesToAssign().size(),1);
		module.deliverPackage(fromAirport, fromGate, toAirport, toGate);
		assertEquals(airPackage.getAllPackagesToAssign().size(),1);
		
		removeAllChildren(world);
	}
	
	/**
	 * Spawn 1 drone op een airport. Voeg 2 pakketten toe, eerst aan airport waar drone niet staat, dan een waar drone wel staat.
	 * Het 2de pakket mag worden toegekend, het eerste niet.
	 */
	@Test
	public void testMoreThanOneDroneAvailableAtOtherAirport() {
		WorldObject world = new WorldObject();
		Airport airport1 = new Airport(0, 1000, 500, new Vector2D(0,0));
		airport1.setRelativePosition(new ArrayRealVector(new double[] {0,0,0}, false));
		Airport airport2 = new Airport(1, 1000, 500, new Vector2D(0, 0));
		airport2.setRelativePosition(new ArrayRealVector(new double[] {-10000,0,60000}, false));
		Drone drone = new Drone(config, velocity);
		world.addChild(drone);
		world.addChild(airport1);
		world.addChild(airport2);
		
		Airport fromAirport1 = airport1;
		Airport toAirport1 = airport2;
		Gate fromGate1 = fromAirport1.getGates()[0];
		Gate toGate1 = toAirport1.getGates()[0];
		
		Airport fromAirport2 = airport2;
		Airport toAirport2 = airport1;
		Gate fromGate2 = fromAirport2.getGates()[0];
		Gate toGate2 = toAirport2.getGates()[0];
		
		//Set position of drone1 at airport1
        RealVector gatePos = fromGate1.getWorldPosition();
        RealVector dronePos1 = gatePos.add(
                new ArrayRealVector(new double[]{
                        0,
                        -drone.getConfig().getWheelY() + drone.getConfig().getTyreRadius(),
                        0
                }, false)
        );
        drone.setRelativePosition(dronePos1);
        drone.setToAirport();
        
        Airport firstAirport = world.getFirstChildOfType(Airport.class);
        
        assertEquals(firstAirport.getAllAirports().size(), 2);
//		assertEquals(AirportPackage.getAllPackagesToAssign().size(),0);
		assertEquals(firstAirport.getChildrenOfType(Drone.class).size(), 1);
		
		//Packet1
		AutopilotModule module = new AutopilotModule(world);
		module.deliverPackage(fromAirport2, fromGate2, toAirport2, toGate2);
        AirportPackage airPackage = world.getFirstChildOfType(AirportPackage.class);
		assertEquals(airPackage.getAllPackagesToAssign().size(),1);
		
		//Packet2
		module.deliverPackage(fromAirport1, fromGate1, toAirport1, toGate1);
		airPackage.assignPackages();
		assertEquals(airPackage.getAllPackagesToAssign().size(),1);
		
		assertEquals(drone.getPackage().getOrigin(), fromGate1);	
		assertTrue(fromGate2.hasPackage());
		
		removeAllChildren(world);
        
	}
	
	public static void removeAllChildren(WorldObject world) {
		world.removeAllChildrenOfType(Drone.class);
		world.removeAllChildrenOfType(Gate.class);
		world.removeAllChildrenOfType(Airport.class);
		world.removeAllChildrenOfType(Runway.class);
		world.removeAllChildrenOfType(WorldObject.class);
	}

}
