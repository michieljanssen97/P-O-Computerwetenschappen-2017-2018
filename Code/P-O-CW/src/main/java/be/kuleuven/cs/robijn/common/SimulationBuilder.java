package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.common.airports.Runway;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import be.kuleuven.cs.robijn.worldObjects.GroundPlane;
import be.kuleuven.cs.robijn.worldObjects.WorldObject;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.HashMap;

public class SimulationBuilder {
    /**
     * Creates airports and drones as specified in the 'settings' parameter and adds them to 'root'.
     * @param settings the simulation settings
     * @param root the worldobject to add the objects to.
     */
    public static void buildSimulation(SimulationSettings settings, WorldObject root){
    	//Add groundplane to calculate grass and tarmac
    	GroundPlane groundPlane = new GroundPlane();
    	root.addChild(groundPlane);
    	
        //Add airports
        HashMap<SimulationSettings.AirportDefinition, Airport> airports = new HashMap<>();
        SimulationSettings.AirportDefinition[] airportDefs = settings.getAirports();
        for (int i = 0; i < airportDefs.length; i++) {
            SimulationSettings.AirportDefinition airportDef = airportDefs[i];
            Airport newAirport = new Airport(
                    i,
                    settings.getRunwayLength(),
                    settings.getGateLength(),
                    new Vector2D(
                            airportDef.getCenterToRunway0X(),
                            airportDef.getCenterToRunway0Z()
                    )
            );
            newAirport.setRelativePosition(new ArrayRealVector(new double[]{airportDef.getCenterX(), 0, airportDef.getCenterZ()}));
            root.addChild(newAirport);
            airports.put(airportDef, newAirport);
            groundPlane.addAirport(newAirport);
        }

        //Setup drones
        for(SimulationSettings.DroneDefinition droneDef : settings.getDrones()){
            //Add new drone
            RealVector initialVelocity = new ArrayRealVector(new double[] {0, 0, 0}, false);
            Drone newDrone = new Drone(droneDef.getConfig(), initialVelocity);
            root.addChild(newDrone);

            //Place drone at gate
            Airport droneAirport = airports.get(droneDef.getAirport());
            Runway droneRunway = droneAirport.getRunways()[droneDef.getRunwayToFace()];
            Gate droneGate = droneAirport.getGates()[droneDef.getGate()];
            RealVector gatePos = droneGate.getWorldPosition();
            RealVector dronePos = gatePos.add(
                    new ArrayRealVector(new double[]{
                            0,
                            -newDrone.getConfig().getWheelY() + newDrone.getConfig().getTyreRadius(),
                            0
                    }, false)
            );
            newDrone.setRelativePosition(dronePos);

            //Rotate drone towards correct runway
            RealVector gateToRunwayVector = droneRunway.getWorldPosition().subtract(droneGate.getWorldPosition());
            Rotation rotation = new Rotation(
                    new Vector3D(0, 0, -1),
                    new Vector3D(
                            gateToRunwayVector.getEntry(0),
                            gateToRunwayVector.getEntry(1),
                            gateToRunwayVector.getEntry(2)
                    )
            );
            
//            double angle = droneAirport.getAngle();
//            if (droneDef.getRunwayToFace() == 1)
//            	angle += Math.PI;
//            Rotation rotation = new Rotation( Vector3D.PLUS_J, angle );
//            rotation.applyTo(new Vector3D(0, 0, -1));
            
            newDrone.setRelativeRotation(rotation); //TODO: make sure this isn't broken by Drone
            
        }
        
    }
}
