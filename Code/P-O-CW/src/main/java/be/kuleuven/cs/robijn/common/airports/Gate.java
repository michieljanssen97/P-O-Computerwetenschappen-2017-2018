package be.kuleuven.cs.robijn.common.airports;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import be.kuleuven.cs.robijn.worldObjects.WorldObject;

public class Gate extends WorldObject{
    private boolean hasPackage;
    private boolean hasDrones;
    
    public Gate() {
    	this.hasPackage = false;
    	this.hasDrones = true;
    }

    public boolean hasDrones(){
        return this.hasDrones;
    }
    
    public void setHasDrones(Boolean status){
        this.hasDrones = status;
    }

    public Vector2D getSize(){
        return new Vector2D(this.getScale().getEntry(0), this.getScale().getEntry(2));
    }

    public boolean hasPackage(){
        return hasPackage;
    }

    public void setHasPackage(boolean hasPackage){
        this.hasPackage = hasPackage;
    }
}