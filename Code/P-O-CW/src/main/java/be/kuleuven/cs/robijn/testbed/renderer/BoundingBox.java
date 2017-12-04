package be.kuleuven.cs.robijn.testbed.renderer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Represents the box with minimal volume that completely contains a mesh
 */
public class BoundingBox {
    private final Vector3D boxPos;
    private final Vector3D boxSize;

    public BoundingBox(Vector3D position, Vector3D boxSize){
        this.boxPos = position;
        this.boxSize = boxSize;
    }

    public Vector3D getBoxPos() {
        return boxPos;
    }

    public Vector3D getBoxSize() {
        return boxSize;
    }
}
