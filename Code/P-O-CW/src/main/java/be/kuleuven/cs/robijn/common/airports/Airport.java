package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.common.WorldObject;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.ArrayRealVector;

public class Airport extends WorldObject {
    private Vector2D size;
    private Gate[] gates;
    private Runway[] runways;

    public Airport(float length, float width, Vector2D centerToRunway0){
        size = new Vector2D((2f*length) + width, 2f*width);

        Vector2D gateSize = new Vector2D(width, width);
        gates = new Gate[2];
        gates[0] = new Gate(gateSize);
        gates[0].setRelativePosition(new ArrayRealVector(new double[]{0, 0, width/2f}, false));
        gates[1] = new Gate(gateSize);
        gates[1].setRelativePosition(new ArrayRealVector(new double[]{0, 0, -width/2f}, false));
        this.addChildren(gates);

        Vector2D runwaySize = new Vector2D(length, width*2f);
        runways = new Runway[2];
        runways[0] = new Runway(runwaySize);
        runways[0].setRelativePosition(new ArrayRealVector(new double[]{-(length+width)/2f, 0, 0}, false));
        runways[1] = new Runway(runwaySize);
        runways[1].setRelativePosition(new ArrayRealVector(new double[]{(length+width)/2f, 0, 0}, false));
        this.addChildren(runways);

        this.setRelativeRotation(
            new Rotation(
                Vector3D.MINUS_I,
                new Vector3D(centerToRunway0.getX(), 0, centerToRunway0.getY())
            )
        );
    }

    public Vector2D getSize(){
        return size;
    }

    public Gate[] getGates() {
        return gates;
    }

    public Runway[] getRunways() {
        return runways;
    }
}
