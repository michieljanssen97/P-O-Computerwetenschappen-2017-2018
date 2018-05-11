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

    private final int id;
    private Vector2D size;
    private Gate[] gates;
    private Runway[] runways;

    private ArrayList<Drone> currentDrones = new ArrayList<Drone>();
    private double angle;
    public Airport(int id, float length, float width, Vector2D centerToRunway0){
        this.id = id;
        size = new Vector2D((2f*length) + width, 2f*width);

        RealVector gateSize = new ArrayRealVector(new double[]{width, 0, width}, false);
        gates = new Gate[2];
        gates[0] = new Gate(this, 0);
        gates[0].setRelativePosition(new ArrayRealVector(new double[]{0, 0, width/2f}, false));
        gates[0].setScale(gateSize);
        gates[1] = new Gate(this, 1);
        gates[1].setRelativePosition(new ArrayRealVector(new double[]{0, 0, -width/2f}, false));
        gates[1].setScale(gateSize);
        this.addChildren(gates);

        RealVector runwaySize = new ArrayRealVector(new double[]{length, 0, width*2f}, false);
        runways = new Runway[2];
        runways[0] = new Runway(this, 0);
        runways[0].setRelativePosition(new ArrayRealVector(new double[]{-(length+width)/2f, 0, 0}, false));
        runways[0].setScale(runwaySize);
        runways[1] = new Runway(this, 1);
        runways[1].setRelativePosition(new ArrayRealVector(new double[]{(length+width)/2f, 0, 0}, false));
        runways[1].setScale(runwaySize);
        this.addChildren(runways);
        
        double angle = Math.atan2( centerToRunway0.getY(), -centerToRunway0.getX());
        this.setRelativeRotation(
            new Rotation(Vector3D.PLUS_J, angle)
        );
        Airport.allAirportsList.add(this);
        this.angle = angle;
    }
    
    public static ArrayList<Airport>getAllAirports() {
    	return Airport.allAirportsList;
    }
    
    public int getXPositionMiddle() {
    	return (int) this.getWorldPosition().getEntry(0);
    }
    
    public int getZPositionMiddle() {
    	return (int) this.getWorldPosition().getEntry(1);
    }
    

    public int getId() {
        return id;
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
    
    public Drone getAvailableDrone() {
    	Drone drone = this.getFirstAvailableDrone();
    	if(drone != null) {
    		return drone;
    	}
    	
    	double minDistance = Double.MAX_VALUE;
    	for(Airport airp : Airport.getAllAirports()) {
    		Drone tempDrone = airp.getFirstAvailableDrone();
    		if(tempDrone != null && tempDrone.calculateDistanceToAirport(this) < minDistance) { //Give higher priority to Drones at nearby Airports, moet mss nog anders wegens orientatie van de luchthavens
    			minDistance = tempDrone.calculateDistanceToAirport(this);
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
			if(airport.isOnAirport(position)) {
				return airport;
			}
		}
		return null;
	}
	
	/**
	 * Check if the given location is on (or above) the given airport
	 */
	public boolean isOnAirport(RealVector place) {
		RealVector airportPos = this.getWorldPosition();
		Vector2D airport2DPos = new Vector2D(airportPos.getEntry(0), airportPos.getEntry(2));
		Vector2D airportSize = this.getSize();
		double angle = this.getAngle()+Math.PI;
		Vector2D position = new Vector2D(place.getEntry(0), place.getEntry(2));
		
		Vector2D up = new Vector2D(airportSize.getX()/2, new Vector2D(-Math.cos(angle), -Math.sin(angle)));		
		Vector2D down = new Vector2D(airportSize.getX()/2, new Vector2D(Math.cos(angle), Math.sin(angle)));		
		Vector2D right = new Vector2D(airportSize.getY()/2, new Vector2D(-Math.sin(angle), Math.cos(angle)));		
		Vector2D left = new Vector2D(airportSize.getY()/2, new Vector2D(Math.sin(angle), -Math.cos(angle)));
		
		Vector2D leftUpCorner = airport2DPos.add(up).add(right);
		Vector2D leftDownCorner = airport2DPos.add(up).add(left);
		Vector2D rightDownCorner = airport2DPos.add(down).add(left);
		Vector2D rightUpCorner = airport2DPos.add(down).add(right);
		
		//The rectangle consists out of four vectors. If the tyre position is left of all four vectors, it's located within the rectangle's area.
		if ( Math.atan2(leftDownCorner.getY()-leftUpCorner.getY(), leftDownCorner.getX() - leftUpCorner.getX()) >= Math.atan2(position.getY()-leftUpCorner.getY(), position.getX()-leftUpCorner.getX()) &&
				Math.atan2(rightDownCorner.getY()-leftDownCorner.getY(), rightDownCorner.getX() - leftDownCorner.getX()) >= Math.atan2(position.getY()-leftDownCorner.getY(), position.getX()-leftDownCorner.getX()) &&
				Math.atan2(rightUpCorner.getY()-rightDownCorner.getY(), rightUpCorner.getX() - rightDownCorner.getX()) >= Math.atan2(position.getY()-rightDownCorner.getY(), position.getX()-rightDownCorner.getX()) &&
				Math.atan2(leftUpCorner.getY()-rightUpCorner.getY(), leftUpCorner.getX() - rightUpCorner.getX()) >= Math.atan2(position.getY()-rightUpCorner.getY(), position.getX()-rightUpCorner.getX()) ) {
			return true;
		}
		
		return false;
	}

	public Runway getRunwayToLand() { //TODO kan mss anders
		return this.getRunways()[0];
	}

	public Runway getRunwayToTakeOff() {
		return this.getRunways()[1];
	}
    public double getAngle() {
    	return angle;
    }
}
