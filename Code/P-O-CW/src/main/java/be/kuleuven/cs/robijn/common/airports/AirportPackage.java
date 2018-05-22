package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.autopilot.Autopilot;
import be.kuleuven.cs.robijn.autopilot.AutopilotModule;
import be.kuleuven.cs.robijn.autopilot.routeCalculator;
import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.worldObjects.Drone;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public class AirportPackage extends WorldObject{
    private final Gate origin, destination;

    private ArrayList<Consumer<AirportPackage>> stateUpdateEventHandlers = new ArrayList<>();
    private State packageState;
    private Gate currentGate;
    private Drone currentTransporter;
    private AutopilotModule module;

    public AirportPackage(Gate origin, Gate destination, AutopilotModule module){
        if(origin == null || destination == null){
            throw new IllegalArgumentException();
        }

        this.origin = origin;
        this.destination = destination;
        this.module = module;

        markAsAtGate(origin);
    }

    public Gate getOrigin(){
        return origin;
    }

    public Gate getDestination() {
        return destination;
    }

    public void addStateUpdateEventHandler(Consumer<AirportPackage> deliveryHandler){
        stateUpdateEventHandlers.add(deliveryHandler);
    }

    public enum State {
        AT_GATE, IN_TRANSIT, DELIVERED
    }

    /**
     * Sets the state of package to AT_GATE, and sets the gate.
     * @param gate the gate the package is currently at
     */
    public void markAsAtGate(Gate gate){
        if(packageState == State.DELIVERED){
            throw new IllegalStateException("Packages that have been delivered cannot be marked as at gate.");
        }else if(packageState == State.AT_GATE){
            throw new IllegalStateException("This package was already at a gate.");
        }

        packageState = State.AT_GATE;
        currentGate = gate;
        currentGate.setPackage(this);
        if(currentTransporter != null){
            currentTransporter.setPackage(null);
            currentTransporter = null;
        }

        for(Consumer<AirportPackage> handler : stateUpdateEventHandlers){
            handler.accept(this);
        }
    }

    /**
     * Sets the state of the package to IN_TRANSIT, and sets the transporter.
     * @param transporter the drone that is carrying the package
     */
    public void markAsInTransit(Drone transporter){
        if(packageState == State.DELIVERED){
            throw new IllegalStateException("Packages that have been delivered cannot be marked as in transit.");
        }else if(packageState == State.IN_TRANSIT){
            throw new IllegalStateException("This package was already in transit.");
        }

        packageState = State.IN_TRANSIT;
        currentGate.setPackage(null);
        currentGate = null;
        currentTransporter = transporter;
        currentTransporter.setPackage(this);

        for(Consumer<AirportPackage> handler : stateUpdateEventHandlers){
            handler.accept(this);
        }
    }

    /**
     * Sets the state of the package to DELIVERED, and removes it from the transporter.
     */
    public void markAsDelivered(){
        if(packageState == State.DELIVERED){
            throw new IllegalStateException("This package was already marked as delivered.");
        }

        packageState = State.DELIVERED;
        if(currentGate != null){
            currentGate.setPackage(null);
            currentGate = null;
        }
        if(currentTransporter != null){
            currentTransporter.setPackage(null);
            currentTransporter = null;
        }

        for(Consumer<AirportPackage> handler : stateUpdateEventHandlers){
            handler.accept(this);
        }
    }

    public State getState() {
        return packageState;
    }

    public Gate getCurrentGate() {
        return currentGate;
    }

    public Drone getCurrentTransporter() {
        return currentTransporter;
    }

    public boolean hasBeenDelivered(){
        return packageState == State.DELIVERED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AirportPackage that = (AirportPackage) o;
        return Objects.equals(origin, that.origin) &&
                Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Package{");
        builder.append("origin=");
        builder.append(origin.getAirport().getId()).append(":").append(origin.getId());
        builder.append(", destination=").append(destination.getAirport().getId()).append(":").append(destination.getId());
        builder.append(", state=").append(packageState);
        if(packageState == State.AT_GATE){
            builder.append(", gate=").append(currentGate.getAirport().getId()).append(":").append(currentGate.getId());
        }else if(packageState == State.IN_TRANSIT){
            builder.append(", transporter=").append(currentTransporter.getDroneID());
        }
        builder.append('}');
        return builder.toString();
    } 
    
    public ArrayList<AirportPackage> getAllPackagesToAssign(){
    	ArrayList<AirportPackage> packageList = new ArrayList<AirportPackage>();
    	for (Gate gate : Gate.getAllGates(this.getParent())) {
    		if(gate.hasPackage()) {
    			packageList.add(gate.getPackage());
    		}
    	}
        return packageList;
    }
    
    public boolean droneCanStart(Drone drone, Gate fromGate, Gate toGate, Airport currentAirport) {
    	if(currentAirport == null) {
    		return false;
    	}
		Runway toTakeOff = routeCalculator.getFromRunway(drone, fromGate);
		Runway toLand = routeCalculator.getToRunway(drone, fromGate, toGate, toTakeOff, drone.getHeight());
		
		System.out.println(Runway.areRunwaysAvailable(toTakeOff, toLand) + " " + !toGate.hasDrone());
    	return (Runway.areRunwaysAvailable(toTakeOff, toLand) && !toGate.hasDrone());
    }
    
    private static Gate findClosestGate(Drone drone) {
    	Gate closestGate = drone.getClosestGate(drone.getAirportOfDrone());
    	
    	if(closestGate != null) {
    		return closestGate;
    	}
    	
    	throw new IllegalStateException();
    }
    
    private void lockEverything(Drone drone, Gate fromGate, Gate toGate) {
        Runway takeOffRunway = routeCalculator.getFromRunway(drone, fromGate);
		Runway landRunway = routeCalculator.getToRunway(drone, fromGate, toGate, takeOffRunway, drone.getHeight());
		
		System.out.println(landRunway.getCurrentDrone() + " " + drone);
		
        takeOffRunway.setCurrentDrone(drone);
    	landRunway.setCurrentDrone(drone);
        drone.setDestinationRunway(landRunway);
        toGate.setCurrentDrone(drone);
    }

    public void assignPackages() {
    	int oldAmountOfPackages = this.getAllPackagesToAssign().size();
    	for(AirportPackage p : this.getAllPackagesToAssign()){
            Airport fromAirport = p.getOrigin().getAirport(); 
            Gate fromGate = p.getOrigin();
            Gate toGate = p.getDestination();
            Drone drone = fromAirport.getAvailableDrone(fromGate);
           
            if(drone != null && drone.canBeAssigned()) {
            	if(drone.getCurrentAirport() != fromAirport) {
            		if(drone.getAirportOfDrone() == null) {
            			throw new IllegalStateException();
            		}
            		Gate newFromGate = findClosestGate(drone);
            		if(p.droneCanStart(drone, newFromGate, fromGate, drone.getAirportOfDrone())) {
	            		p.lockEverything(drone, newFromGate, fromGate);
	            		drone.setCanBeAssigned(false);
	            		
	            		Autopilot autopilot = module.autopilots.get(drone);
	            		autopilot.setTargetPosition(fromGate.getWorldPosition());
	            		autopilot.setFlyAfterPackagePicked(drone, fromGate, toGate);
	            		
		            	module.taxiToGateAndFly(drone, newFromGate, fromGate);
            		}
            		else {
            			System.out.println("HIERO1 " + this.getAllPackagesToAssign().size());
            		}
            	}
	            else if (p.droneCanStart(drone, fromGate, toGate, fromAirport)) {
	            	p.markAsInTransit(drone);
		            p.lockEverything(drone, fromGate, toGate);
	                module.taxiToGateAndFly(drone, fromGate, toGate);
	            }
	            else {
	            	System.out.println("HIERO2 " + this.getAllPackagesToAssign().size());
	            }
            }
        }
    	
    	if(oldAmountOfPackages != 0 && this.getAllPackagesToAssign().size() == oldAmountOfPackages) {//No package was assigned to a drone
    		boolean isDeadlock = true;
    		for(Drone d : this.getParent().getChildrenOfType(Drone.class)) {
    			if(!d.canBeAssigned() || d.hasPackage()) {
    				isDeadlock = false;
    				break;
    			}
    		}
    		
    		if(isDeadlock) {
	    		//TODO laat een vliegtuig (op een airp zonder pakje) naar een vrije gate vliegen zodat 'deadlock' wordt opgelost
	    		System.out.println("--------------------------------- DEADLOCK: " + oldAmountOfPackages);
    		}
    	}
    }
}
