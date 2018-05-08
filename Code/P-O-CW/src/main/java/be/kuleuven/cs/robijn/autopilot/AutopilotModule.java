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
    	Autopilot autopilot = autopilots.get(drone);
    	RealVector[] route = routeCalculator.calculateRoute(drone, fromGate, toGate, hight);
    	autopilot.setTargets(route);
    	autopilot.setTargetPosition(toGate.getWorldPosition());
    	autopilot.setFlightMode(FlightMode.ASCEND);
    }
}
