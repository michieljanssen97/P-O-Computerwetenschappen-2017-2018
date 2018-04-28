package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.common.WorldObject;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Gate extends WorldObject{
    private final Airport parent;
    private final int id;
    private AirportPackage queuedPackage;

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

    public AirportPackage getPackage() {
        return queuedPackage;
    }

    public boolean hasPackage() {
        return queuedPackage != null;
    }

    public void setPackage(AirportPackage pack){
        this.queuedPackage = pack;
    }
}
