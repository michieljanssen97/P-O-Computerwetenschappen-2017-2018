package be.kuleuven.cs.robijn.common.airports;

import be.kuleuven.cs.robijn.common.WorldObject;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class Gate extends WorldObject{
    private Vector2D size;
    private boolean hasPackage;

    public Gate(Vector2D size){
        this.size = size;
    }

    public Vector2D getSize() {
        return size;
    }

    public boolean hasPackage(){
        return hasPackage;
    }

    public void setHasPackage(boolean hasPackage){
        this.hasPackage = hasPackage;
    }
}
