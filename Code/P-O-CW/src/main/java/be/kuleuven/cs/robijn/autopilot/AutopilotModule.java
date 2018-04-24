package be.kuleuven.cs.robijn.autopilot;

import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import be.kuleuven.cs.robijn.worldObjects.Package;
import be.kuleuven.cs.robijn.worldObjects.WorldObject;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

public class AutopilotModule {
    private final WorldObject world;

    public AutopilotModule(WorldObject world){
        this.world = world;
    }
    
    public void deliverPackage(Airport fromAirport, Gate fromGate, Airport toAirport, Gate toGate) {
        new Package(fromAirport, fromGate, toAirport, toGate);
    }
    

    public void startTimeHasPassed(Drone drone, AutopilotInputs inputs) {
        //TODO
    }

    public AutopilotOutputs completeTimeHasPassed(Drone drone) {
        //TODO
        return new AutopilotOutputs() {
            @Override
            public float getThrust() {
                return 0;
            }

            @Override
            public float getLeftWingInclination() {
                return 0;
            }

            @Override
            public float getRightWingInclination() {
                return 0;
            }

            @Override
            public float getHorStabInclination() {
                return 0;
            }

            @Override
            public float getVerStabInclination() {
                return 0;
            }

            @Override
            public float getFrontBrakeForce() {
                return 0;
            }

            @Override
            public float getLeftBrakeForce() {
                return 0;
            }

            @Override
            public float getRightBrakeForce() {
                return 0;
            }
        };
    }

    public void simulationEnded() {
        //TODO
    }
}
