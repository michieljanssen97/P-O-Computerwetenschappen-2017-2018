package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.common.SimulationSettings;
import be.kuleuven.cs.robijn.common.WorldObject;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class Airport extends WorldObject {
    private final int id;
    private Vector2D size;
    private Gate[] gates;
    private Runway[] runways;

    public Airport(int id, float length, float width, Vector2D centerToRunway0){
        this.id = id;
        size = new Vector2D((2f*length) + width, 2f*width);

        RealVector gateSize = new ArrayRealVector(new double[]{width, 0, width}, false);
        gates = new Gate[2];
        gates[0] = new Gate(this, 0);
        gates[0].setRelativePosition(new ArrayRealVector(new double[]{0, 0, width/2f}, false));
        gates[0].setScale(gateSize);
        gates[1] = new Gate(this, 1);
        gates[1].setRelativePosition(new ArrayRealVector(new double[]{0, 0, -width/2f}, false));
        gates[1].setScale(gateSize);
        this.addChildren(gates);

        RealVector runwaySize = new ArrayRealVector(new double[]{length, 0, width*2f}, false);
        runways = new Runway[2];
        runways[0] = new Runway(this, 0);
        runways[0].setRelativePosition(new ArrayRealVector(new double[]{-(length+width)/2f, 0, 0}, false));
        runways[0].setScale(runwaySize);
        runways[1] = new Runway(this, 1);
        runways[1].setRelativePosition(new ArrayRealVector(new double[]{(length+width)/2f, 0, 0}, false));
        runways[1].setScale(runwaySize);
        this.addChildren(runways);

        this.setRelativeRotation(
            new Rotation(Vector3D.PLUS_J, Math.acos(new Vector2D(-1, 0).dotProduct(centerToRunway0)))
        );
    }

    public int getId() {
        return id;
    }

    public Vector2D getSize() {
        return size;
    }

    public Gate[] getGates() {
        return gates;
    }

    public Runway[] getRunways() {
        return runways;
    }
}
