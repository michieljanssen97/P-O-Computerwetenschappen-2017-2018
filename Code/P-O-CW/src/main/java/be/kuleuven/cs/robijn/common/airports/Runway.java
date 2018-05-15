package be.kuleuven.cs.robijn.common.airports;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import be.kuleuven.cs.robijn.common.WorldObject;


public class Runway extends WorldObject{
	private boolean hasDrone = false;
    private final Airport parent;
    private final int id;
    
    public boolean hasDrones(){
        return this.hasDrone;
    }
    
    public void setHasDrones(Boolean status){
        this.hasDrone = status;
    }
	public static boolean areRunwaysAvailable(Runway runway1, Runway runway2) {
		return !runway1.hasDrones() && !runway2.hasDrones();
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
}