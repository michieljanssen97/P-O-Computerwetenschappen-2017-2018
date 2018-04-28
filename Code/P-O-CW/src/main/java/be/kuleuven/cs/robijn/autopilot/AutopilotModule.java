package be.kuleuven.cs.robijn.autopilot;

import java.util.HashMap;

import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

public class AutopilotModule {
    private final WorldObject world;
    private final HashMap<Drone, AutopilotOutputs> latestOutputs = new HashMap<>();
    private final HashMap<Drone, Autopilot> autopilots = new HashMap<>();

    public AutopilotModule(WorldObject world){
        this.world = world;
        for (Drone drone: world.getChildrenOfType(Drone.class)) {
        	Autopilot autopilot = new Autopilot();
        	autopilot.initialise(drone.getConfig(), drone);
        	autopilots.put(drone, autopilot);
        }
    }

    public void deliverPackage(Airport fromAirport, Gate fromGate, Airport toAirport, Gate toGate) {
        //TODO
    }

    public void startTimeHasPassed(Drone drone, AutopilotInputs inputs) {
    	Autopilot autopilot = autopilots.get(drone);
        AutopilotOutputs outputs = autopilot.timePassed(inputs);
        latestOutputs.put(drone, outputs);
    }

    public AutopilotOutputs completeTimeHasPassed(Drone drone) {
        return latestOutputs.get(drone);
    }

    public void simulationEnded() throws IllegalArgumentException {
        throw new IllegalArgumentException();
    }
}
