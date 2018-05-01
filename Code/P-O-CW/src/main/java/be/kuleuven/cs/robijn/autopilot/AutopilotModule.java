package be.kuleuven.cs.robijn.autopilot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.common.airports.Runway;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

public class AutopilotModule {
    private final WorldObject world; //Must not be modified by Autopilot or race conditions might occur!
    private final HashMap<Drone, Autopilot> autopilots = new HashMap<>();
    private final HashMap<Drone, Future<AutopilotOutputs>> autopilotTasks = new HashMap<>();
    private final ExecutorService threadPool;

    public AutopilotModule(WorldObject world){
        this.world = world;

        // Create a new autopilot for each drone in the world.
        ArrayList<Drone> drones = world.getChildrenOfType(Drone.class);
        for (Drone drone : drones) {
        	Autopilot autopilot = new Autopilot();
        	autopilot.initialise(drone.getConfig(), drone);
        	autopilots.put(drone, autopilot);
        }

        // Create a threadpool for the autopilots to run on in parallel.
        threadPool = Executors.newFixedThreadPool(drones.size(), runnable -> {
            Thread thread = new Thread(runnable, "Autopilot Thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void deliverPackage(Airport fromAirport, Gate fromGate, Airport toAirport, Gate toGate) {
        //TODO
    }

    public void startTimeHasPassed(Drone drone, AutopilotInputs inputs) {
        // Get the autopilot that controls this drone
    	Autopilot autopilot = autopilots.get(drone);
    	// Run the autopilot update on a separate thread in the threadpool
    	Future<AutopilotOutputs> task = threadPool.submit(() -> autopilot.timePassed(inputs));
    	// Store the task.
        autopilotTasks.put(drone, task);
    }

    public AutopilotOutputs completeTimeHasPassed(Drone drone) {
        try {
            // Wait until the autopilot update is done and return the result.
            return autopilotTasks.get(drone).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void simulationEnded() throws IllegalArgumentException {
        threadPool.shutdown();
    }
    
    public void flyRoute(Drone drone, Gate fromGate, Gate toGate, float hight) {
    	Airport fromAirport = fromGate.getAirport();
    	Runway fromRunway1 = fromAirport.getRunways()[0];
    	RealVector vector1 = fromRunway1.getWorldPosition();
    	Runway fromRunway2 = fromAirport.getRunways()[1];
    	RealVector vector2 = fromRunway2.getWorldPosition();
    	
    	Runway fromRunway;
    	RealVector droneHeading = drone.transformationToWorldCoordinates(new ArrayRealVector(new double[] {0, 0, -50}, false))
    			.add(drone.getWorldPosition());
    	if (vector1.getDistance(droneHeading) < vector2.getDistance(droneHeading))
    		fromRunway = fromRunway1;
    	else {
    		fromRunway = fromRunway2;
    	}
    	
    	RealVector fromOrientation = fromRunway.getWorldPosition().subtract(fromAirport.getWorldPosition());
    	fromOrientation = fromOrientation.add(
    			fromGate.getWorldPosition().subtract(fromAirport.getWorldPosition()));
    	if (fromOrientation.getNorm() != 0)
    		fromOrientation = fromOrientation.mapMultiply(1/fromOrientation.getNorm());
    			
    	RealVector firstTarget = fromOrientation.mapMultiply(
    			(fromAirport.getSize().getX()/2) + (hight/Math.tan(Math.toRadians(5)))
    			).add(drone.getWorldPosition());
    		
    	Airport toAirport = toGate.getAirport();
    	Runway toRunway1 = toAirport.getRunways()[0];
    	RealVector toOrientation1 = toRunway1.getWorldPosition().subtract(toAirport.getWorldPosition());
    	toOrientation1 = toOrientation1.add(
    			toGate.getWorldPosition().subtract(toAirport.getWorldPosition()));
    	if (toOrientation1.getNorm() != 0)
    		toOrientation1 = toOrientation1.mapMultiply(1/toOrientation1.getNorm());
    	float tempHight = hight;
    		
    	Runway toRunway2 = fromAirport.getRunways()[1];
    	
    }
}
