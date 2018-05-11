package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.worldObjects.Drone;
import be.kuleuven.cs.robijn.worldObjects.WorldObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public class AirportPackage {
    private final Gate origin, destination;

    private ArrayList<Consumer<AirportPackage>> stateUpdateEventHandlers = new ArrayList<>();
    private State packageState;
    private Gate currentGate;
    private Drone currentTransporter;
    private boolean isAssignedLater = false;

    public AirportPackage(Gate origin, Gate destination){
        if(origin == null || destination == null){
            throw new IllegalArgumentException();
        }
        
      if(origin.hasPackage()){
    	  throw new IllegalStateException(); //mag maar 1 package beschikbaar zijn per Gate
      }

        this.origin = origin;
        this.destination = destination;

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
        
        Airport fromAirport = this.getOrigin().getAirport();
        Airport toAirport = this.getDestination().getAirport();
		Runway takeOffRunway = fromAirport.getRunwayToTakeOff();
		Runway landRunway = toAirport.getRunwayToLand();
		
		
    	//TODO naar fromGate taxiën (packet ophalen), en van daaruit naar takeOffRunWay om op te stijgen
        takeOffRunway.setHasDrones(true);
        currentTransporter.setTakeOffRunway(takeOffRunway);
        
    	landRunway.setHasDrones(true); //TODO mss pas later locken, vanaf inzetten landing ofzo -> Wel rekening houden dat hij dan niet vrij kan zijn, erboven blijven cirkelen, enz...
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
    
    public static ArrayList<AirportPackage> getAllPackagesToAssign(){
    	ArrayList<AirportPackage> packageList = new ArrayList<AirportPackage>();
    	for (Gate gate : WorldObject.getChildrenOfType(Gate.class)) {
    		if(gate.hasPackage() && gate.getPackage().isAssignedLater == false) {
    			packageList.add(gate.getPackage());
    		}
    	}
        return packageList;
    }

    public static void assignPackages() {
        for(AirportPackage p : getAllPackagesToAssign()){
            Gate fromGate = p.getOrigin();
            Gate toGate = p.getDestination();
            Airport fromAirport = fromGate.getAirport();
            Airport toAirport = toGate.getAirport();
            
            Drone drone = fromAirport.getAvailableDrone();
            if(drone != null) {
            	if(drone.getCurrentAirport() != fromAirport) {
	            	//TODO laat drone naar fromAirport vliegen
            		p.isAssignedLater = true;
	            	drone.assignNecessitiesLater(p, fromGate, toGate);
            	}
	            else {
	        		Runway toTakeOff = fromAirport.getRunwayToTakeOff();
	        		Runway toLand = toAirport.getRunwayToLand();
	            	if (Runway.areRunwaysAvailable(toTakeOff, toLand)){
		            	p.markAsInTransit(drone);;
	            	}
	            }
            }
            
            
        }
    }
    
}
