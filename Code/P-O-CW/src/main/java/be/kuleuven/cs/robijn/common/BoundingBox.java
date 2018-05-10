package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.common.math.VectorMath;
import be.kuleuven.cs.robijn.worldObjects.WorldObject;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.stream.Stream;

/**
 * Represents the box with minimal volume that completely contains a mesh
 */
public class BoundingBox extends WorldObject {
    private Vector3D boxDimensions;

    public BoundingBox(Vector3D boxDimensions){
        this.boxDimensions = boxDimensions;
    }

    public BoundingBox(BoundingBox toCopy){
        super(toCopy);
        this.boxDimensions = toCopy.getBoxDimensions();
    }

    public Vector3D getBoxDimensions() {
        return boxDimensions;
    }

    public boolean contains(Vector3D pos, float margin){
        Vector3D worldPos = VectorMath.realTo3D(getWorldPosition());
        Vector3D vectToBox = worldPos.subtract(pos);
        return Math.abs(vectToBox.getX()) < (boxDimensions.getX() / 2.0d) + margin &&
                Math.abs(vectToBox.getY()) < (boxDimensions.getY() / 2.0d) + margin &&
                Math.abs(vectToBox.getZ()) < (boxDimensions.getZ() / 2.0d) + margin;
    }

    public void scaleDimensions(RealVector scale){
        boxDimensions = new Vector3D(
            boxDimensions.getX() * scale.getEntry(0),
            boxDimensions.getY() * scale.getEntry(1),
            boxDimensions.getZ() * scale.getEntry(2)
        );
    }

    public BoundingBox merge(BoundingBox box2){
        Vector3D box1Pos = VectorMath.realTo3D(this.getWorldPosition());
        Vector3D box2Pos = VectorMath.realTo3D(box2.getWorldPosition());

        double minX = Math.min(
                box1Pos.getX() - (this.getBoxDimensions().getX()/2d),
                box2Pos.getX() - (box2.getBoxDimensions().getX()/2d)
        );
        double minY = Math.min(
                box1Pos.getY() - (this.getBoxDimensions().getY()/2d),
                box2Pos.getY() - (box2.getBoxDimensions().getY()/2d)
        );
        double minZ = Math.min(
                box1Pos.getZ() - (this.getBoxDimensions().getZ()/2d),
                box2Pos.getZ() - (box2.getBoxDimensions().getZ()/2d)
        );
        double maxX = Math.max(
                box1Pos.getX() + (this.getBoxDimensions().getX()/2d),
                box2Pos.getX() + (box2.getBoxDimensions().getX()/2d)
        );
        double maxY = Math.max(
                box1Pos.getY() + (this.getBoxDimensions().getY()/2d),
                box2Pos.getY() + (box2.getBoxDimensions().getY()/2d)
        );
        double maxZ = Math.max(
                box1Pos.getZ() + (this.getBoxDimensions().getZ()/2d),
                box2Pos.getZ() + (box2.getBoxDimensions().getZ()/2d)
        );
        double width = maxX - minX;
        double height = maxY - minY;
        double depth = maxZ - minZ;
        double x = minX + (width / 2d);
        double y = minY + (height / 2d);
        double z = minZ + (depth / 2d);
        BoundingBox box = new BoundingBox(new Vector3D(width, height, depth));
        box.setRelativePosition(new ArrayRealVector(new double[]{x, y, z}));
        return box;
    }
}
