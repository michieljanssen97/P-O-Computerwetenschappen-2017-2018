package be.kuleuven.cs.robijn.common.airports;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import be.kuleuven.cs.robijn.worldObjects.WorldObject;

public class Runway extends WorldObject{
    public Vector2D getSize(){
        return new Vector2D(this.getScale().getEntry(0), this.getScale().getEntry(2));
    }
}
