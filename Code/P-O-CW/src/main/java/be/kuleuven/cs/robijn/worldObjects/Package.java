package be.kuleuven.cs.robijn.worldObjects;

import java.util.ArrayList;

import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.common.airports.Runway;

public class Package extends WorldObject{
	
	private static ArrayList<Package> packagesToAssignList = new ArrayList<Package>();

	private final Airport fromAirport;
	private final Gate fromGate;
    private final Airport toAirport;
    private final Gate toGate;
    private boolean delivered;
    private Drone assignedDrone;
    

    public Package(Airport fromAirport, Gate fromGate, Airport toAirport, Gate toGate){
        this.fromAirport = fromAirport;
        this.fromGate = fromGate;
        this.toAirport = toAirport;
        this.toGate = toGate;
        this.delivered = false;
        this.assignedDrone = null;
        addToPackagesToAssignList(this);
    }
    
    public static void addToPackagesToAssignList(Package p){
        packagesToAssignList.add(p);
    }
    
    public static void removeFromPackagesToAssignList(Package p){
        packagesToAssignList.remove(p);
    }
       
    public static boolean stillPackagesToAssign(){
        return (! packagesToAssignList.isEmpty());
    }
    
    public static ArrayList<Package> getAllPackagesToAssign(){
        return new ArrayList<Package>(Package.packagesToAssignList);
    }
    
    public Airport getFromAirport(){
        return this.fromAirport;
    }
    
    public Gate getFromGate(){
        return this.fromGate;
    }
    
    public Airport getToAirport(){
        return this.toAirport;
    }
    
    public Gate getToGate(){
        return this.toGate;
    }
    
    public Boolean isDelivered() {
    	return this.delivered;
    }
    
    public void setDelivered() {
    	this.delivered = true;
    	this.assignedDrone.setAssignedPackage(null);
    	this.assignedDrone = null;
    }
    
    public Drone getAssignedDrone() {
    	return this.assignedDrone;
    }
    
    public void setAssignedDrone(Drone d) {
    	this.assignedDrone = d;
    }
    
    public static void assignPackages() {
        for(Package p : getAllPackagesToAssign()){
            Airport fromAirport = p.getFromAirport();
            Gate fromGate = p.getFromGate();
            Airport toAirport = p.getToAirport();
            Gate toGate = p.getToGate();
            
            if(fromGate.hasPackage()){
                throw new IllegalStateException(); //mag maar 1 package beschikbaar zijn per Gate
            }
            
            Drone drone = Airport.getAvailableDrone(fromAirport);
            if(drone != null) {
            	if(drone.getCurrentAirport() != fromAirport) {
	            	//TODO laat drone naar fromAirport vliegen
	            	drone.assignNecessitiesLater(p, fromAirport, fromGate, toAirport, toGate);
	            	removeFromPackagesToAssignList(p);
            	}
	            else {
	        		Runway toTakeOff = fromAirport.getRunwayToTakeOff();
	        		Runway toLand = toAirport.getRunwayToLand();
	            	if (Runway.areRunwaysAvailable(toTakeOff, toLand)){
		            	p.assignPackageNecessities(drone, fromAirport, fromGate, toAirport, toGate, toTakeOff, toLand);
		            	removeFromPackagesToAssignList(p);
	            	}
	            }
            }
            
            
        }
    }
  
    
    /**
     * Zet alle pointers
     */
    public void assignPackageNecessities(Drone drone, Airport fromAirport, Gate fromGate, Airport toAirport, Gate toGate, Runway takeOffRunway, Runway landRunway) {
    	//TODO naar fromGate taxiën, en van daaruit naar takeOffRunWay
        this.setAssignedDrone(drone);
        toGate.setHasPackage(true);
        fromAirport.getRunwayToLand().setHasDrones(false); // TODO Kan mss nog worden aangepast, pas op false zetten indien er een vliegtuig zal landen of erop staat
        drone.setAssignedPackage(this);
        
        takeOffRunway.setHasDrones(true);
        drone.setTakeOffRunway(takeOffRunway);
        
    	landRunway.setHasDrones(true);
        drone.setDestinationRunway(landRunway);
    }
}
