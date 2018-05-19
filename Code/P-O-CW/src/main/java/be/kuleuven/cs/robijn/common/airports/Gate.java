package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.worldObjects.Drone;

import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.RealVector;

public class Gate extends WorldObject{
    private final Airport parent;
    private final int id;
    private AirportPackage queuedPackage;
    
    private Drone currentDrone = null;

    public Gate(Airport parent, int id){
        this.parent = parent;
        this.id = id;
    }

    public Vector2D getSize(){
        return new Vector2D(this.getScale().getEntry(0), this.getScale().getEntry(2));
    }
    
    public Airport getAirport() {
        return parent;
    }

    public int getId(){
        return id;
    }

    public String getUID(){
        return parent.getId() + ":" + getId();
    }

    public AirportPackage getPackage() {
        return queuedPackage;
    }

    public boolean hasPackage() {
        return this.getPackage() != null;
    }

    public void setPackage(AirportPackage pack){
        this.queuedPackage = pack;
    }

    public boolean isDroneAbove(Drone drone) {
        RealVector dronePos = drone.getWorldPosition();
        RealVector gatePos = this.getWorldPosition();
        RealVector droneToGateVect = gatePos.subtract(dronePos);
        Vector2D gateSize = this.getSize();

        return Math.abs(droneToGateVect.getEntry(0)) < gateSize.getX() &&
                Math.abs(droneToGateVect.getEntry(2)) < gateSize.getY();
    }
    
    public static ArrayList<Gate> getAllGates(WorldObject world){
    	ArrayList<Gate> allGates = new ArrayList<Gate>();
    	for(Airport airp : world.getChildrenOfType(Airport.class)) {
    		for(Gate g : airp.getChildrenOfType(Gate.class)) {
    			allGates.add(g);
    		}
    	}
    	
    	return allGates;
    }

	public boolean hasDrone() {
		return(this.getCurrentDrone() != null);
	}

	public void setCurrentDrone(Drone drone) {
		if(this.hasDrone() && !this.getCurrentDrone().equals(drone)) {
			throw new IllegalStateException();
		}
		else {
			this.currentDrone = drone;
		}
		
	}

	public Drone getCurrentDrone() {
		return this.currentDrone;
	}

	public void removeCurrentDrone() {
		this.currentDrone = null;
	}
}