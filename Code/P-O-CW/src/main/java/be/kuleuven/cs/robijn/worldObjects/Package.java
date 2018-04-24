package be.kuleuven.cs.robijn.worldObjects;

import java.util.ArrayList;

import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;

public class Package extends WorldObject{
	
	private static ArrayList<Package> packagesToAssignList = new ArrayList<Package>();

	private final Airport fromAirport;
	private final Gate fromGate;
    private final Airport toAirport;
    private final Gate toGate;
    private boolean delivered; //TODO Zet op true indien vliegtuig met package is geland -> indien status autopilot == FlightMode.Taxi na FlightMode.Land
    private Drone assignedDrone;
    

    public Package(Airport fromAirport, Gate fromGate, Airport toAirport, Gate toGate){
        this.fromAirport = fromAirport;
        this.fromGate = fromGate;
        this.toAirport = toAirport;
        this.toGate = toGate;
        this.delivered = false;
        this.assignedDrone = null;
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
    
    public void assignPackages() { //TODO zorg dat dit elke iteratie wordt opgeroepen
        for(Package p : getAllPackagesToAssign()){
            Airport fromAirport = p.getFromAirport();
            Gate fromGate = p.getFromGate();
            Airport toAirport = p.getToAirport();
            Gate toGate = p.getToGate();
            
            if(fromGate.hasPackage()){
                throw new IllegalStateException(); //TODO of overslaan (wachten tot volgende iteratie en opnieuw controleren totdat de fromGate wel vrij is), mag maar 1 package beschikbaar zijn per Gate
            }
            if(isDroneAvailable() && ! toGate.hasDrones()){
            	p.assignPackageNecessities(fromAirport, fromGate, toAirport, toGate);
            	removeFromPackagesToAssignList(p);
                //TODO fromGate terug vrij (zowel drone als package) na opstijgen -> indien status autopilot == FLightMode.Ascend na FlightMode.Taxi}
            }
        }
    }
    
    /**
     * Check all Airports for an available Drone
     */
    public boolean isDroneAvailable(){
    	boolean result = false;
    	for(Airport airport : Airport.getAllAirports()) {
    		if(airport.isDroneAvailable()) {
    			result = true;
    			break;
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Zet alle pointers
     */
    public void assignPackageNecessities(Airport fromAirport, Gate fromGate, Airport toAirport, Gate toGate) {
        Drone drone = Airport.getAvailableDrone(fromAirport);
        this.setAssignedDrone(drone);
        toGate.setHasPackage(true);
        toGate.setHasDrones(false); // TODO Kan mss nog worden aangepast, pas op false zetten indien er een vliegtuig zal landen of erop staat
        drone.setAssignedPackage(this);
        drone.setDestinationAirport(toAirport); //TODO niet indien drone NIET op fromAirport is, moet eerst naar fromAirport vliegen an dan mag dit pas
    }
}
