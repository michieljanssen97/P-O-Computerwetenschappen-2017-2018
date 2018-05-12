package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.worldObjects.Drone;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.worldObjects.WorldObject;

public class Gate extends WorldObject{
    private final Airport parent;
    private final int id;
    private AirportPackage queuedPackage;

    //TODO mss een vertrek gate voor pakjes en een aankomstgate
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
        return queuedPackage != null;
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
}