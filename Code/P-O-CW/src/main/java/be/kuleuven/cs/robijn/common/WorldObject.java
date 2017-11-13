package be.kuleuven.cs.robijn.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class WorldObject {
    private WorldObject parent;
    private ArrayList<WorldObject> children = new ArrayList<>();
    private RealVector position = new ArrayRealVector(new double[]{0, 0, 0}, false);
    private Rotation rotation = new Rotation(new Vector3D(1, 0, 0), 0);
    private String name = "";

    ////////////////////////
    /// OBJECT HIERARCHY ///
    ////////////////////////

    /**
     * Returns an immutable list of the children of this object.
     */
    public List<WorldObject> getChildren(){
        return Collections.unmodifiableList(children);
    }

    /**
     * Searches the direct children of this object and returns the first child of the specified type.
     * If no such object is found, null is returned.
     * @param clazz the class of the child to return. Must not be null.
     */
    @SuppressWarnings("unchecked")
	public <T extends WorldObject> T getFirstChildOfType(Class<T> clazz){
        if(clazz == null){
            throw new IllegalArgumentException("clazz cannot be null");
        }

        for(WorldObject child : children){
            if(child.getClass().isAssignableFrom(clazz)){
            	if ((T) child != null){//make sure the child is a valid child
                	return (T) child;
            	}
            }
        }

        return null;
    }

    /**
     * Searches the direct children of this object and returns the first child with a matching name and type.
     * If no such object is found, null is returned.
     * @param name the name of the object to be returned.
     * @param clazz the class of the child to return. Must not be null.
     */
    @SuppressWarnings("unchecked")
    public <T extends WorldObject> T getChildByName(String name, Class<T> clazz){
        if(clazz == null){
            throw new IllegalArgumentException("clazz cannot be null");
        }

        for(WorldObject child : children){
            if(child.getClass().isAssignableFrom(clazz) && Objects.equals(name, child.getName())){
                return (T)child;
            }
        }

        return null;
    }

    /**
     * Returns a stream with this object, this objects children, their children and so on.
     */
    public Stream<WorldObject> getDescendantsStream(){
        return Stream.concat(
                Stream.of(this),
                getChildren().stream().flatMap(WorldObject::getDescendantsStream)
        );
    }

    /**
     * Adds a child to this object. The transformation of this object will now be relative to this object.
     * @param obj the new child. Must not be null
     */
    public void addChild(WorldObject obj){
        if(obj == null){
            throw new IllegalArgumentException("obj cannot be null");
        }
        if(obj.parent != null){
            throw new IllegalArgumentException("obj already has a parent");
        }

        children.add(obj);
        obj.parent = this;
    }

    /**
     * Removes a child from this object.
     * @param obj the child to remove. Must not be null
     * @return true if obj was a child of this object.
     */
    public boolean removeChild(WorldObject obj){
        if(obj == null){
            throw new IllegalArgumentException("obj cannot be null");
        }

        if(children.remove(obj)){
            obj.parent = null;
            return true;
        }
        return false;
    }

    /**
     * Returns the WorldObject of which this object is the child.
     * Returns null if this has no parent.
     */
    public WorldObject getParent() {
        return parent;
    }

    ///////////////////
    /// OBJECT NAME ///
    ///////////////////

    /**
     * Returns the name of this object. If no name was set using setName(), this method return "".
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this object.
     * @param name the new name of this object.
     */
    public void setName(String name) {
        this.name = name;
    }

    ////////////////////////
    /// OBJECT TRANSFORM ///
    ////////////////////////

    /// RELATIVE TRANSFORM ///

    /**
     * Sets the position of this object relative to its parent.
     * @param vector the new position vector of this object. Must not be null.
     */
    public void setRelativePosition(RealVector vector) {
        if(vector == null){
            throw new IllegalArgumentException("vector cannot be null");
        }

        this.position = vector;
    }

    /**
     * Returns the position of this object, relative to its parent.
     * @return a non-null vector that is immutable.
     */
    public RealVector getRelativePosition() {
        return RealVector.unmodifiableRealVector(position);
    }

    /**
     * Sets the rotation of this object relative to its parent.
     * @param rotation the new rotational vector of this object. Must not be null.
     */
    public void setRelativeRotation(Rotation rotation) {
        if(rotation == null){
            throw new IllegalArgumentException("rotation cannot be null");
        }

        this.rotation = rotation;
    }

    /**
     * Returns the rotation of this object, relative to its parent.
     * @return a non-null rotation
     */
    public Rotation getRelativeRotation() {
        return rotation;
    }

    /// WORLD TRANSFORM ///

    /**
     * Returns an affine transformation matrix that transforms local coordinates to world coordinates.
     * @return a non-null 4x4 homogeneous transformation matrix
     */
    public RealMatrix getObjectToWorldTransform(){
        //Create local affine transformation matrix
        RealMatrix rotationMatrix = new Array2DRowRealMatrix(new double[4][4], false);
        rotationMatrix.setEntry(3, 3, 1); //Identity
        rotationMatrix.setSubMatrix(this.getRelativeRotation().getMatrix(), 0, 0);

        //Create local affine translation matrix
        RealVector localTranslation = getRelativePosition();
        RealMatrix translationMatrix = new Array2DRowRealMatrix(new double[][]{
            {1, 0, 0, localTranslation.getEntry(0)},
            {0, 1, 0, localTranslation.getEntry(1)},
            {0, 0, 1, localTranslation.getEntry(2)},
            {0, 0, 0, 1}
        }, false);

        RealMatrix objectToParentTransform = translationMatrix.multiply(rotationMatrix);
        if(getParent() != null) {
            return getParent().getObjectToWorldTransform().multiply(objectToParentTransform);
        }
        return objectToParentTransform;
    }

    /**
     * Returns the position of this object in world coordinates.
     * @return a non-null vector of size 3 that is immutable.
     */
    public RealVector getWorldPosition() {
        //Get object to world transform
        RealMatrix transform = getObjectToWorldTransform();
        //Return the world coordinates of the local origin
        return new ArrayRealVector(transform.operate(new double[]{0, 0, 0, 1}), 0, 3);
    }

    /**
     * Returns the rotation of this object, relative to the world axis.
     * @return a non-null vector
     */
    public Rotation getWorldRotation() {
        //Get object to world transform
        RealMatrix transform = getObjectToWorldTransform();
        //Cut out the translation and scaling bits.
        RealMatrix rotationMatrix = transform.getSubMatrix(0, 2, 0, 2);
        //Convert to rotation
        return new Rotation(rotationMatrix.getData(), 0.0001d);
    }
}

