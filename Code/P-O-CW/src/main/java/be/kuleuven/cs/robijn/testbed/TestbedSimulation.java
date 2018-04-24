package be.kuleuven.cs.robijn.testbed;

import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import interfaces.AutopilotOutputs;

public class TestbedSimulation {
    private final WorldObject world;

    public TestbedSimulation(WorldObject world){
        this.world = world;
    }

    public void updateDrone(Drone drone, float secondsSinceStart, float secondsSinceLastUpdate, AutopilotOutputs output){
        //TODO
    }

    public boolean isSimulationFinished(){
        //TODO
        return false;
    }
}
