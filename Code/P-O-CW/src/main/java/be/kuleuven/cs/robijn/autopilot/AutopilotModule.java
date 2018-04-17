package be.kuleuven.cs.robijn.autopilot;

import java.util.ArrayList;

import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import be.kuleuven.cs.robijn.worldObjects.Package;
import be.kuleuven.cs.robijn.worldObjects.WorldObject;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

public class AutopilotModule {
    private final WorldObject world;
    private ArrayList<Package> packageToAssignList = new ArrayList<Package>();

    public AutopilotModule(WorldObject world){
        this.world = world;
    }
    
    
    public void addToPackagesToAssignList(Package p){
        packageToAssignList.add(p);
    }
    
    public void removeFromPackagesToAssignList(Package p){
        packageToAssignList.remove(p);
    }
       
    public boolean stillPackagesToAssign(){
        return (! packageToAssignList.isEmpty());
    }
    
    public ArrayList<Package> getAllPackagesToAssign(){
        return new ArrayList<Package>(this.packageToAssignList);
    }
    
    public void deliverPackage(Airport fromAirport, Gate fromGate, Airport toAirport, Gate toGate) {
        Package p = new Package(fromAirport, fromGate, toAirport, toGate);
        this.addToPackagesToAssignList(p);
    }

    public void assignPackages() { //TODO zorg dat dit elke iteratie wordt opgeroepen
        for(Package p : this.getAllPackagesToAssign()){
            Airport fromAirport = p.getFromAirport();
            Gate fromGate = p.getFromGate();
            Airport toAirport = p.getToAirport();
            Gate toGate = p.getToGate();
            
            if(fromGate.hasPackage()){
                throw new IllegalStateException(); //TODO of overslaan (wachten tot volgende iteratie en opnieuw controleren totdat de fromGate wel vrij is), mag maar 1 package beschikbaar zijn per Gate
            }
            if(fromAirport.isDroneAvailable() && toGate.hasDrones()){
            	p.assignPackagNecessities(fromAirport, fromGate, toAirport, toGate);
            	this.removeFromPackagesToAssignList(p);
                //TODO fromGate terug vrij (zowel drone als package) na opstijgen -> indien status autopilot == FLightMode.Ascend na FlightMode.Taxi}
            }
        }
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
