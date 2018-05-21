package be.kuleuven.cs.robijn.autopilot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.AirportPackage;
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
    	AirportPackage p = new AirportPackage(fromGate, toGate, this);
    	fromGate.setPackage(p);
    	this.world.addChild(p);
    	
//    	float closestDistance = Float.POSITIVE_INFINITY;
//    	Drone bestDrone = null;
//    	ArrayList<Drone> drones = world.getChildrenOfType(Drone.class);
//		for (Drone drone : drones) {
//			float distance = (float) fromGate.getWorldPosition().getDistance(drone.getWorldPosition());
//			if (distance < closestDistance) {
//				closestDistance = distance;
//				bestDrone = drone;
//			}
//	    }
//		autopilots.get(bestDrone).flyRoute(bestDrone, fromGate, toGate);
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
    
    public void taxiToGateAndFly(Drone drone, Gate fromGate, Gate toGate) {
    	
    	System.out.println(drone.getWorldPosition());
    	System.out.println(fromGate.getWorldPosition());
    	System.out.println(toGate.getWorldPosition());
        
    	Autopilot autopilot = autopilots.get(drone);
    	if(!drone.isOnGate(fromGate) && drone.canBeAssigned() && drone.getAirportOfDrone().equals(fromGate.getAirport())) {//first taxi to the correct Gate
    		
    		System.out.println("-------------------- TAXI");
    		
	    	autopilot.setTargetPosition(fromGate.getWorldPosition());
	    	autopilot.setFlightMode(FlightMode.TAXI);
	    	
	    	autopilot.setFlyAfterPackagePicked(drone, fromGate, toGate);
    	}
    	else {
    		System.out.println("-------------------- FLY");
    		
    		autopilot.flyRoute(drone, fromGate, toGate);
    	}
    }

	public WorldObject getWorld() {
		return this.world;
	}
}
