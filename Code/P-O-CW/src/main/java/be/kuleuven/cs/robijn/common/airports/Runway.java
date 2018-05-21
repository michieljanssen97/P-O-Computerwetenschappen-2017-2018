package be.kuleuven.cs.robijn.common.airports;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.worldObjects.Drone;


public class Runway extends WorldObject{
	private Drone currentDrone = null;
    private final Airport parent;
    private final int id;
    
    public boolean hasDrone(){
        return this.getCurrentDrone() != null;
    }
	public static boolean areRunwaysAvailable(Runway runway1, Runway runway2) {
		return !runway1.hasDrone() && !runway2.hasDrone();
	}

    public Runway(Airport parent, int id) {
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

	public Drone getCurrentDrone() {
		return this.currentDrone;
	}
	public void setCurrentDrone(Drone drone) {
		if(this.hasDrone() && !this.getCurrentDrone().equals(drone)) {
			throw new IllegalStateException();
		}
		else {
			this.currentDrone = drone;
		}
	}
	public void removeCurrentDrone() {
		this.currentDrone = null;
	}
}