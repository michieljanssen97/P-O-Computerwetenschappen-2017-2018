package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.common.WorldObject;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Runway extends WorldObject{
    public Vector2D getSize(){
        return new Vector2D(this.getScale().getEntry(0), this.getScale().getEntry(2));
    }
}
