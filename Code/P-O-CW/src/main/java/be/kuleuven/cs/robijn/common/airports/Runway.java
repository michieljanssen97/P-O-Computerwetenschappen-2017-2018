package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.common.WorldObject;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Runway extends WorldObject{
    private final Airport parent;
    private final int id;

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
