package be.kuleuven.cs.robijn.common.airports;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import be.kuleuven.cs.robijn.worldObjects.WorldObject;

public class Runway extends WorldObject{
	private boolean hasDrone = false;
	
    public Vector2D getSize(){
        return new Vector2D(this.getScale().getEntry(0), this.getScale().getEntry(2));
    }
    
    public boolean hasDrones(){
        return this.hasDrone;
    }
    
    public void setHasDrones(Boolean status){
        this.hasDrone = status;
    }
	public static boolean areRunwaysAvailable(Runway runway1, Runway runway2) {
		return !runway1.hasDrones() && !runway2.hasDrones();
	}
}