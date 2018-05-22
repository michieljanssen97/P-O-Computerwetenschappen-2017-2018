package be.kuleuven.cs.robijn.autopilot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.AirportPackage;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.gui.GUI;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

public class AutopilotModule {
    private final WorldObject world; //Must not be modified by Autopilot or race conditions might occur!
    public final HashMap<Drone, Autopilot> autopilots = new HashMap<>();
    private final HashMap<Drone, Future<AutopilotOutputs>> autopilotTasks = new HashMap<>();
    private final ExecutorService threadPool;

    public AutopilotModule(WorldObject world){
    	GUI.println("No imminent collisions.");
        this.world = world;

        // Create a new autopilot for each drone in the world.
        ArrayList<Drone> drones = world.getChildrenOfType(Drone.class);
        int index = 0;
        for (Drone drone : drones) {
        	Autopilot autopilot = new Autopilot();
        	autopilot.initialise(drone.getConfig(), drone);
        	autopilots.put(drone, autopilot);
        	drone.setHeight(40 + index*10);
        	index += 1;
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
    
    public void beforeUpdate() {
    	Object[] collision = this.calculateFirstDroneCollision();
        if (((collision != null) && (this.collision != null) && (collision[0] != this.collision[0])) || 
        		((collision != null) && (this.collision != null) && (((Double)collision[1]) >= 5))) {
        	Drone[] collisionDrones = (Drone[]) this.collision[0];
        	Autopilot autopilot1 = autopilots.get(collisionDrones[0]);
        	Autopilot autopilot2 = autopilots.get(collisionDrones[1]);
        	autopilot1.stopPrevention();
        	autopilot2.stopPrevention();
        	this.collision = null;
        	GUI.println("No imminent collisions.");
        }
        if ((collision != null) && (((Double)collision[1]) < 5)) {
        	Drone[] collisionDrones = (Drone[]) collision[0];
        	if (collision != this.collision)
        		GUI.println("Preventing collision between drone" + collisionDrones[0].getDroneID()
            			+ "and drone" + collisionDrones[1].getDroneID() + ".");
        	this.collision = collision;
        	Autopilot autopilot1 = autopilots.get(collisionDrones[0]);
        	Autopilot autopilot2 = autopilots.get(collisionDrones[1]);
        	autopilot1.preventCollisionFirst();
        	autopilot2.preventCollisionSecond();
        }
    }

    public void startTimeHasPassed(Drone drone, AutopilotInputs inputs) {
        // Get the autopilot that controls this drone
    	Autopilot autopilot = autopilots.get(drone);
    	// Run the autopilot update on a separate thread in the threadpool
    	Future<AutopilotOutputs> task = threadPool.submit(() -> autopilot.timePassed(inputs));
    	// Store the task.
        autopilotTasks.put(drone, task);
    }
    
    private Object[] collision = null;

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
    	Autopilot autopilot = autopilots.get(drone);
//    	if(!drone.isOnGate(fromGate) && drone.canBeAssigned() && drone.getAirportOfDrone().equals(fromGate.getAirport())) {//first taxi to the correct Gate
//    		
//    		System.out.println("-------------------- TAXI");
//    		
//	    	autopilot.setTargetPosition(fromGate.getWorldPosition());
//	    	autopilot.setFlightMode(FlightMode.TAXI);
//	    	
//	    	autopilot.setFlyAfterPackagePicked(drone, fromGate, toGate);
//    	}
//    	else {
//    		System.out.println("-------------------- FLY");

    		
    		autopilot.flyRoute(drone, fromGate, toGate);
//   	}
    }
    
    public Object[] calculateFirstDroneCollision(){
		WorldObject world = this.getWorld();
		List<Drone> drones = world.getChildrenOfType(Drone.class);
		Map<Drone[], Double> collisionDroneMap = new HashMap<>();
		Double collisionTime;

		for(int i = 0; i < drones.size(); i++){
			for(int j = i+1; j < drones.size(); j++){
				double deltaPosX = drones.get(i).getWorldPosition().getEntry(0)-drones.get(j).getWorldPosition().getEntry(0);
				double deltaPosY = drones.get(i).getWorldPosition().getEntry(1)-drones.get(j).getWorldPosition().getEntry(1);
				double deltaPosZ = drones.get(i).getWorldPosition().getEntry(2)-drones.get(j).getWorldPosition().getEntry(2);


				double deltaVelX = drones.get(i).getVelocity().getEntry(0)-drones.get(j).getVelocity().getEntry(0);
				double deltaVelY = drones.get(i).getVelocity().getEntry(1)-drones.get(j).getVelocity().getEntry(1);
				double deltaVelZ = drones.get(i).getVelocity().getEntry(2)-drones.get(j).getVelocity().getEntry(2);

				double deltaRR = Math.pow(deltaPosX, 2) + Math.pow(deltaPosY, 2) + Math.pow(deltaPosZ, 2);
				double deltaVV = Math.pow(deltaVelX, 2) + Math.pow(deltaVelY, 2) + Math.pow(deltaVelZ, 2);
				double deltaVR = (deltaVelX*deltaPosX)  + (deltaVelY*deltaPosY) +  (deltaVelZ*deltaPosZ);
				double d = Math.pow(deltaVR, 2) - (deltaVV)*(deltaRR - Math.pow(5, 2));


				if (deltaVR >= 0){
					collisionTime =  Double.POSITIVE_INFINITY;
				} else if (d < 0){
					collisionTime = Double.POSITIVE_INFINITY;
				} else {
					collisionTime =  - ((deltaVR + Math.sqrt(d))/(deltaVV));
				}
				if (collisionTime != Double.POSITIVE_INFINITY) {
					Drone[] collisionArray = {drones.get(i), drones.get(j)};
					collisionDroneMap.put(collisionArray, collisionTime);
				}
			}
		}
		if (collisionDroneMap.isEmpty()) {
			return null;
		} else {
			Drone[] firstCollisionDroneArray = Collections.min(collisionDroneMap.entrySet(), Map.Entry.comparingByValue()).getKey();
			return new Object[] {firstCollisionDroneArray, collisionDroneMap.get(firstCollisionDroneArray)};
		}
	}

	public WorldObject getWorld() {
		return this.world;
	}
}
