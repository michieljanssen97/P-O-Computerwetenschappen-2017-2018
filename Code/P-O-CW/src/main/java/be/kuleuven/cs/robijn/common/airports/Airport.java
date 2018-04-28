package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.worldObjects.Drone;
import be.kuleuven.cs.robijn.worldObjects.WorldObject;

import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class Airport extends WorldObject {
	
	private static ArrayList<Airport> allAirportsList = new ArrayList<Airport>();
	
	private final RealVector position; //The middle of the airport
    private Vector2D size;
    private Gate[] gates;
    private Runway[] runways;
    private ArrayList<Drone> currentDrones = new ArrayList<Drone>();

    public Airport(float xPosition, float zPosition, float length, float width, Vector2D centerToRunway0){
        size = new Vector2D((2f*length) + width, 2f*width);

        RealVector gateSize = new ArrayRealVector(new double[]{width, 0, width}, false);
        gates = new Gate[2];
        gates[0] = new Gate();
        gates[0].setRelativePosition(new ArrayRealVector(new double[]{0, 0, width/2f}, false));
        gates[0].setScale(gateSize);
        gates[1] = new Gate();
        gates[1].setRelativePosition(new ArrayRealVector(new double[]{0, 0, -width/2f}, false));
        gates[1].setScale(gateSize);
        this.addChildren(gates);

        RealVector runwaySize = new ArrayRealVector(new double[]{length, 0, width*2f}, false);
        runways = new Runway[2];
        runways[0] = new Runway();
        runways[0].setRelativePosition(new ArrayRealVector(new double[]{-(length+width)/2f, 0, 0}, false));
        runways[0].setScale(runwaySize);
        runways[1] = new Runway();
        runways[1].setRelativePosition(new ArrayRealVector(new double[]{(length+width)/2f, 0, 0}, false));
        runways[1].setScale(runwaySize);
        this.addChildren(runways);

        this.setRelativeRotation(
            new Rotation(Vector3D.PLUS_J, Math.acos(new Vector2D(-1, 0).dotProduct(centerToRunway0)))
        );
        Airport.allAirportsList.add(this);
        
        this.position = new ArrayRealVector(new Double[] {(double) xPosition, (double) zPosition});
    }
    
    public static ArrayList<Airport>getAllAirports() {
    	return Airport.allAirportsList;
    }
    
    public RealVector getPosition() {
    	return new ArrayRealVector(this.position);
    }
    
    public int getXPositionMiddle() {
    	return (int) this.getPosition().getEntry(0);
    }
    
    public int getZPositionMiddle() {
    	return (int) this.getPosition().getEntry(1);
    }
    

    public Vector2D getSize() {
        return size;
    }

    public Gate[] getGates() {
        return gates;
    }

    public Runway[] getRunways() {
        return runways;
    }
    
    public ArrayList<Drone> getCurrentDrones() {
        return new ArrayList<Drone>(this.currentDrones);
    }
    
    public void addDroneToCurrentDrones(Drone d){
        currentDrones.add(d);
    }
    
    public void removeDroneFromCurrentDrones(Drone d){
        currentDrones.remove(d);
    }
    
    /**
     * Check all Airports for an available Drone
     */
    public static boolean isDroneAvailableInWorld(){
    	boolean result = false;
    	for(Airport airport : Airport.getAllAirports()) {
    		if(airport.isDroneAvailableOnThisAirport()) {
    			result = true;
    			break;
    		}
    	}
    	
    	return result;
    }
    
    public Drone getFirstAvailableDrone(){
        for(Drone d : this.getCurrentDrones()){
            if (d.isAvailable()){
                return d;
            }
        }
        
        return null;
    }
    
    public static Drone getAvailableDrone(Airport airport) {
    	Drone drone = airport.getFirstAvailableDrone();
    	if(drone != null) {
    		return drone;
    	}
    	
    	double distance = Double.MAX_VALUE;
    	for(Airport airp : Airport.getAllAirports()) {
    		Drone tempDrone = airp.getFirstAvailableDrone();
    		if(tempDrone.calculateDistanceToAirport(airport) < distance) { //Give higher priority to Drones at nearby Airports, moet mss nog anders wegens orientatie van de luchthavens
    			distance = tempDrone.calculateDistanceToAirport(airport);
    			drone = tempDrone;
    		}
    	}    	
    	return drone; //Is null if no drones are Available
    }
    
    public boolean isDroneAvailableOnThisAirport() {
        return this.getFirstAvailableDrone() != null;
    }

    /*
     * Get the airport at the given position
     */
	public static Airport getAirportAt(RealVector position) {
		for(Airport airport : allAirportsList) {
			if(airport.isInsideAirport(position)) {
				return airport;
			}
		}
		return null;
	}
	
	/**
	 * Check if the given location is on (or above) the given airport
	 */
	public boolean isInsideAirport(RealVector position) { //TODO hou rekening met orientatie van de airport tov wereldassenstelsel
		Vector2D size = this.getSize();
		if( (position.getEntry(0) < this.getXPositionMiddle() - (size.getX()/1.5)) || // delen door 1.5 ipv 2 om iets meer speling te hebben
				(position.getEntry(0) > this.getXPositionMiddle() + (size.getX()/1.5)) ||
				(position.getEntry(2) < this.getZPositionMiddle() - (size.getY()/1.5)) ||
				(position.getEntry(2) > this.getZPositionMiddle() + (size.getY()/1.5))) {
			return false;
		}
		return true;
	}

	public Runway getRunwayToLand() { //TODO kan mss anders
		return this.getRunways()[0];
	}

	public Runway getRunwayToTakeOff() {
		return this.getRunways()[1];
	}
}
