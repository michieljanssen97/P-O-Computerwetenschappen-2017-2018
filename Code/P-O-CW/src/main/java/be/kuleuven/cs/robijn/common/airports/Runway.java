package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.common.WorldObject;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Runway extends WorldObject{
    private Vector2D size;

    public Runway(Vector2D size){
        this.size = size;
    }

    public Vector2D getSize() {
        return size;
    }
}
