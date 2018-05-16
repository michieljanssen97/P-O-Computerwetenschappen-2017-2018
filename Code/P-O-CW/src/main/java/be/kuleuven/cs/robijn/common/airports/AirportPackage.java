package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.autopilot.AutopilotModule;
import be.kuleuven.cs.robijn.autopilot.routeCalculator;
import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.worldObjects.Drone;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.math3.linear.RealVector;

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
     * Sets the state of the package to IN_TRANSIT, adn sets the transporter.
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

        Runway takeOffRunway = routeCalculator.getFromRunway(transporter, this.getOrigin());
		Runway landRunway = routeCalculator.getToRunway(transporter, this.getOrigin(), this.getDestination(), takeOffRunway, transporter.getHeight());
		
		
        takeOffRunway.setHasDrones(true);
        
    	landRunway.setHasDrones(true);
        currentTransporter.setDestinationRunway(landRunway);

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
    	return (currentAirport.equals(this.getOrigin().getAirport()) && Runway.areRunwaysAvailable(toTakeOff, toLand));
    }
    
    private static Gate findClosestGate(Drone drone, Airport airport) {
    	RealVector dronePos = drone.getWorldPosition();
    	double minDistance = Double.MAX_VALUE;
    	Gate closestGate = null;
    	for(Gate g : airport.getGates()) {
    		double distance = g.getWorldPosition().getDistance(dronePos);
    		if( distance < minDistance) {
    			closestGate = g;
    			minDistance = distance;
    		}
    	}
    	
    	if(closestGate != null) {
    		return closestGate;
    	}
    	
    	throw new IllegalStateException();
    }

    public void assignPackages() {    
    	for(AirportPackage p : this.getAllPackagesToAssign()){
            Airport fromAirport = p.getOrigin().getAirport(); 
            Gate fromGate = p.getOrigin();
            Gate toGate = p.getDestination();
            Drone drone = fromAirport.getAvailableDrone();
            if(drone != null) {
            	if(drone.getCurrentAirport() != fromAirport) {
            		if(drone.getAirportOfDrone() == null) {
            			throw new IllegalStateException();
            		}
	            	module.taxiToGateAndFly(drone, findClosestGate(drone, drone.getAirportOfDrone()), fromGate);
            	}
	            else {
	            	if (p.droneCanStart(drone, fromGate, toGate, fromAirport)){
		            	p.markAsInTransit(drone);
		                module.taxiToGateAndFly(drone, fromGate, toGate);
	            	}
	            }
            }
            
            
        }
    }
}
