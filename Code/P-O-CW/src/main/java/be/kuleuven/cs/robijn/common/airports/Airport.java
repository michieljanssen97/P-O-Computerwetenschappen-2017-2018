package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.worldObjects.Drone;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.awt.Polygon;
import java.util.ArrayList;

public class Airport extends WorldObject {

    private final int id;
    private Vector2D size;
    private Gate[] gates;
    private Runway[] runways;
    private double angle;
    
    public float width;
    
//    private ArrayList<Drone> currentDrones = new ArrayList<Drone>();

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
        this.angle = angle;
        this.width = width;
    }
    
    public ArrayList<Airport>getAllAirports() {
    	return this.getParent().getChildrenOfType(Airport.class);
    }
    
    public int getXPositionMiddle() {
    	return (int) this.getWorldPosition().getEntry(0);
    }
    
    public int getZPositionMiddle() {
    	return (int) this.getWorldPosition().getEntry(2);
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
    	ArrayList<Drone> currentDrones = new ArrayList<Drone>();
    	Gate g0 = this.getGates()[0];
    	Gate g1 = this.getGates()[1];
    	Runway r0 = this.getRunways()[0];
    	Runway r1 = this.getRunways()[1];
    	if(g0.hasDrone()) {currentDrones.add(g0.getCurrentDrone());}
    	if(g1.hasDrone()) {currentDrones.add(g1.getCurrentDrone());}
    	if(r0.hasDrone()) {currentDrones.add(r0.getCurrentDrone());}
    	if(r1.hasDrone()) {currentDrones.add(r1.getCurrentDrone());}
    	
    	return currentDrones;
    }
    
//    public void addDroneToCurrentDrones(Drone d, Gate g, Runway r){
//    	if(g.hasDrone() || r.hasDrone()) {
//    		throw new IllegalArgumentException();
//    	}
//        g.setCurrentDrone(d);
//        r.setCurrentDrone(d);
//    }
//    
//    public void removeDroneFromCurrentDrones(Drone d, Gate g, Runway r){
//    	if(!g.getCurrentDrone().equals(d) || !r.getCurrentDrone().equals(d)) {
//    		throw new IllegalArgumentException();
//    	}
//        g.removeDrone();
//        r.removeDrone();
//    }
    
    /**
     * Check all Airports for an available Drone
     */
    public boolean isDroneAvailableInWorld(){
    	boolean result = false;
    	for(Airport airport : this.getAllAirports()) {
    		if(airport.isDroneAvailableOnThisAirport()) {
    			result = true;
    			break;
    		}
    	}
    	
    	return result;
    }
    
    public Drone getFirstAvailableDrone(){
    	if(! this.getAllAvailableDrones().isEmpty()) {
    		return this.getAllAvailableDrones().get(0);
    	}
        return null;
    }
    
    public Drone getAvailableDrone() {
    	Drone drone = this.getFirstAvailableDrone();
    	if(drone != null) {
    		return drone;
    	}
    	
    	double minDistance = Double.MAX_VALUE;
    	for(Airport airp : this.getAllAirports()) {
    		Drone tempDrone = airp.getFirstAvailableDrone();
    		if(tempDrone != null && tempDrone.calculateDistanceToAirport(this) < minDistance && airp.hasSufficientAvailableDrones()) {
    			minDistance = tempDrone.calculateDistanceToAirport(this);
    			drone = tempDrone;
    		}
    	}    	
    	return drone; //Is null if no drones are Available
    }
    
    /**
     * Check if the airport has more available drones than packages that have yet to be assigned
     */
    private boolean hasSufficientAvailableDrones() {
    	return this.getAllAvailableDrones().size() > this.getAllPackagesThatMustBeAssignedAtThisAirport().size();
    }
    
    private ArrayList<Drone> getAllAvailableDrones() {
    	ArrayList<Drone> availableDrones = new ArrayList<Drone>();
		for(Drone d : this.getCurrentDrones()) {
			if(d.isAvailable()) {
				availableDrones.add(d);
			}
		}
		
		return availableDrones;
	}
    
    private ArrayList<AirportPackage> getAllPackagesThatMustBeAssignedAtThisAirport(){
    	ArrayList<AirportPackage> allPackages = new ArrayList<AirportPackage>();
    	for(Gate g : this.getGates()) {
    		if(g.hasPackage()) {
    			allPackages.add(g.getPackage());
    		}
    	}
    	
    	return allPackages;
    }

	public boolean isDroneAvailableOnThisAirport() {
        return this.getFirstAvailableDrone() != null;
    }

    /*
     * Get the airport at the given position
     */
	public Airport getAirportAt(RealVector position) {
		for(Airport airport : this.getAllAirports()) {
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
		Vector2D airport2DPos = new Vector2D(this.getXPositionMiddle(), this.getZPositionMiddle());
		Vector2D airportSize = this.getSize();
		double angle = this.getAngle() + Math.PI;
		Vector2D positionToCheck = new Vector2D(place.getEntry(0), place.getEntry(2));
		
		Vector2D up = new Vector2D(airportSize.getX()/2, new Vector2D(-Math.cos(angle), -Math.sin(angle)));		
		Vector2D down = new Vector2D(airportSize.getX()/2, new Vector2D(Math.cos(angle), Math.sin(angle)));		
		Vector2D right = new Vector2D(airportSize.getY()/2, new Vector2D(-Math.sin(angle), Math.cos(angle)));		
		Vector2D left = new Vector2D(airportSize.getY()/2, new Vector2D(Math.sin(angle), -Math.cos(angle)));
		
		Vector2D rightUpCorner = airport2DPos.add(up).add(right);
		Vector2D rightDownCorner = airport2DPos.add(up).add(left);
		Vector2D leftDownCorner = airport2DPos.add(down).add(left);
		Vector2D leftUpCorner = airport2DPos.add(down).add(right);
		
//		Vector2D leftUpCorner = airport2DPos.add(up).add(right);
//		Vector2D leftDownCorner = airport2DPos.add(up).add(left);
//		Vector2D rightDownCorner = airport2DPos.add(down).add(left);
//		Vector2D rightUpCorner = airport2DPos.add(down).add(right);
		
//		//The rectangle consists out of four vectors. If the positionToCheck is left of all four vectors, it's located within the rectangle's area.
//		if ( Math.atan2(leftDownCorner.getY()-leftUpCorner.getY(), leftDownCorner.getX() - leftUpCorner.getX()) >= Math.atan2(positionToCheck.getY()-leftUpCorner.getY(), positionToCheck.getX()-leftUpCorner.getX()) &&
//				Math.atan2(rightDownCorner.getY()-leftDownCorner.getY(), rightDownCorner.getX() - leftDownCorner.getX()) >= Math.atan2(positionToCheck.getY()-leftDownCorner.getY(), positionToCheck.getX()-leftDownCorner.getX()) &&
//				Math.atan2(rightUpCorner.getY()-rightDownCorner.getY(), rightUpCorner.getX() - rightDownCorner.getX()) >= Math.atan2(positionToCheck.getY()-rightDownCorner.getY(), positionToCheck.getX()-rightDownCorner.getX()) &&
//				Math.atan2(leftUpCorner.getY()-rightUpCorner.getY(), leftUpCorner.getX() - rightUpCorner.getX()) >= Math.atan2(positionToCheck.getY()-rightUpCorner.getY(), positionToCheck.getX()-rightUpCorner.getX()) ) {
//			return true;
//		}
//		return false;
		
		int[] xPos = {(int) rightUpCorner.getX(), (int) rightDownCorner.getX(), (int) leftDownCorner.getX(), (int) leftUpCorner.getX()};
		int[] yPos = {(int) rightUpCorner.getY(), (int) rightDownCorner.getY(), (int) leftDownCorner.getY(), (int) leftUpCorner.getY()};
		
		
		Polygon airportPolygon = new Polygon(xPos, yPos, 4);
		
		return airportPolygon.contains(positionToCheck.getX(), positionToCheck.getY());

	}
	
    public double getAngle() {
    	return angle;
    }
    
}
