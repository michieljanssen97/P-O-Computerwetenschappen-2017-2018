package be.kuleuven.cs.robijn.common;

import java.util.*;
import java.util.stream.Stream;

import be.kuleuven.cs.robijn.common.math.VectorMath;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.*;

public class WorldObject {
    private WorldObject parent;

    private ArrayList<WorldObject> children = new ArrayList<>();
    private RealVector position = new ArrayRealVector(new double[]{0, 0, 0}, false);
    private Rotation rotation = new Rotation(new Vector3D(1, 0, 0), 0);
    private RealVector scale = new ArrayRealVector(new double[]{1, 1, 1}, false);
    private RealMatrix objectToWorldTransform = null;
    private String name = "";

    public WorldObject(){}

    public WorldObject(WorldObject toCopy){
        setRelativePosition(toCopy.getRelativePosition());
        setRelativeRotation(toCopy.getRelativeRotation());
        setScale(toCopy.getScale());
        setName(toCopy.getName());
    }

    ////////////////////////
    /// OBJECT HIERARCHY ///
    ////////////////////////

    /**
     * Returns an immutable list of the children of this object.
     */
    public List<WorldObject> getChildren(){
        return Collections.unmodifiableList(children);
    }
    
//	public <T extends WorldObject> T getFirstChildOfType(Class<T> clazz){
//        if(clazz == null){
//            throw new IllegalArgumentException("clazz cannot be null");
//        }
//
//        for(WorldObject child : children){
//            if(child.getClass().isAssignableFrom(clazz)){
//                return (T) child;
//            }
//        }
//
//        return null;
//    }

    /**
     * Searches the direct children of this object and returns the first child of the specified type.
     * If no such object is found, null is returned.
     * @param clazz the class of the child to return. Must not be null.
     */
    public <T extends WorldObject> T getFirstChildOfType(Class<T> clazz){
        try {
        	ArrayList<T> childrenOfType = getChildrenOfType(clazz);
        	
        	if(childrenOfType.size() == 0) {
        		return null;
        	}
        	return childrenOfType.get(0);
        	
        }
        
        catch(IllegalArgumentException e) {
        	throw e;
        }
    }

    /**
     * Searches the direct children of this object.
     * If no such object is found, null is returned.
     * @param clazz the class of the child to return. Must not be null.
     */
    @SuppressWarnings("unchecked")
	public <T extends WorldObject> ArrayList<T> getChildrenOfType(Class<T> clazz){
    	ArrayList<T> childrenOfType = new ArrayList<T>();
    	
    	if(clazz == null) {
    		throw new IllegalArgumentException("clazz cannot be null");
    	}
    	
    	
    	for(WorldObject child : getChildren()) {
    		if(clazz.isAssignableFrom(child.getClass())) {
    			childrenOfType.add((T) child);
    		}    		
    	}
    	return childrenOfType;
    	
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
     * Adds a list of children to this object.
     * @param objects an array of new children. Must not be null.
     */
    public void addChildren(WorldObject... objects){
        if(objects == null){
            throw new IllegalArgumentException("objects cannot be null");
        }

        for(WorldObject obj : objects){
            addChild(obj);
        }
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
    
    public <T extends WorldObject> void removeAllChildrenOfType(Class<T> clazz) {
    	for (WorldObject child : getChildrenOfType(clazz)) {
    		removeChild(child);
    	}
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
        this.objectToWorldTransform = null;
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
        this.objectToWorldTransform = null;
    }

    /**
     * Returns the rotation of this object, relative to its parent.
     * @return a non-null rotation
     */
    public Rotation getRelativeRotation() {
        return rotation;
    }

    /**
     * Returns the scale of this object as a 3D vector in object-space.
     */
    public RealVector getScale() {
        return scale;
    }

    /**
     * Sets the scale of this object.
     * @param scale A 3D vector in object space, non-null
     */
    public void setScale(RealVector scale) {
        if(scale == null || scale.getDimension() != 3){
            throw new IllegalArgumentException("Invalid size vector");
        }

        this.scale = scale;
        this.objectToWorldTransform = null;
    }

    /// WORLD TRANSFORM ///

    private boolean isObjectToWorldTransformDirty(){
        return objectToWorldTransform == null || (getParent() != null && getParent().isObjectToWorldTransformDirty());
    }

    /**
     * Returns an affine transformation matrix that transforms local coordinates to world coordinates.
     * @return a non-null 4x4 homogeneous transformation matrix
     */
    public RealMatrix getObjectToWorldTransform(){
        if(isObjectToWorldTransformDirty()){
            if(getParent() == null) {
                objectToWorldTransform = getObjectToParentTransform();
            }else{
                objectToWorldTransform = getParent().getObjectToWorldTransform().multiply(getObjectToParentTransform());
            }
        }
        return objectToWorldTransform;
    }

    public RealMatrix getObjectToParentTransform(){
        //Create local affine scale matrix
        RealMatrix scaleMatrix = new Array2DRowRealMatrix(new double[][]{
                {getScale().getEntry(0), 0, 0, 0},
                {0, getScale().getEntry(1), 0, 0},
                {0, 0, getScale().getEntry(2), 0},
                {0, 0, 0, 1}
        }, false);

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

        return translationMatrix.multiply(rotationMatrix.multiply(scaleMatrix));
    }

    /**
     * Returns the position of this object in world coordinates.
     * @return a non-null vector of size 3 that is immutable.
     */
    public RealVector getWorldPosition() {
        //Get object to world transform
        RealMatrix transform = getObjectToWorldTransform();
        //Return the world coordinates of the local origin
        RealVector origin = new ArrayRealVector(new double[]{0, 0, 0, 1}, false);
        return VectorMath.homogeneousToCartesian(transform.operate(origin));
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

    public void rotateAround(RealVector rotationOrigin, Rotation rotation){
        //Take the local vector 0,0,0 to 0,0,-1 and convert it to worldspace
        RealVector localPosition = new ArrayRealVector(new double[]{0, 0, 0, 1}, false);
        RealVector localOrientation = new ArrayRealVector(new double[]{0, 0, -1, 1}, false);

        //Transformation matrices for converting between object and world vector space
        RealMatrix objectToWorldMatrix = getObjectToWorldTransform();
        RealMatrix worldToObjectMatrix = new LUDecomposition(objectToWorldMatrix).getSolver().getInverse();

        //Get world-space coordinates of position and orientation
        RealVector worldPosition = objectToWorldMatrix.operate(localPosition);
        RealVector worldOrientation = objectToWorldMatrix.operate(localOrientation);

        //Apply translation so rotationOrigin is origin of the world.
        Vector3D rotationCenteredPosition = realVectorTo3DVector(worldPosition).subtract(realVectorTo3DVector(rotationOrigin));
        Vector3D rotationCenteredOrientation = realVectorTo3DVector(worldOrientation).subtract(realVectorTo3DVector(rotationOrigin));

        //Rotate vectors using rotation
        Vector3D rotationCenteredRotatedPosition = rotation.applyTo(rotationCenteredPosition);
        Vector3D rotationCenteredRotatedOrientation = rotation.applyTo(rotationCenteredOrientation);

        //Revert translation
        Vector3D worldRotationPosition = rotationCenteredRotatedPosition.add(realVectorTo3DVector(rotationOrigin));
        Vector3D worldRotationOrientation = rotationCenteredRotatedOrientation.add(realVectorTo3DVector(rotationOrigin));

        //New position in object space (delta vector relative to old position)
        RealVector localRotatedPosition = worldToObjectMatrix.operate(vector3DToRealVector(worldRotationPosition));
        RealVector localRotatedOrientation = worldToObjectMatrix.operate(vector3DToRealVector(worldRotationOrientation));

        //Use transformed 0,0,0 as new position and (transformed 0,0,-1 minus transformed 0,0,0) as new direction
        this.setRelativePosition(VectorMath.homogeneousToCartesian(localRotatedPosition).add(this.getRelativePosition()));
        Vector3D newDirection = realVectorTo3DVector(localRotatedOrientation.subtract(localRotatedPosition));

        //Calculate and apply new local rotation as change from 0,0,-1 to new direction
        this.setRelativeRotation(new Rotation(new Vector3D(0, 0, -1), newDirection));
    }

    private Vector3D realVectorTo3DVector(RealVector vector){
        return new Vector3D(vector.getEntry(0), vector.getEntry(1), vector.getEntry(2));
    }

    private RealVector vector3DToRealVector(Vector3D vector){
        return new ArrayRealVector(new double[]{vector.getX(), vector.getY(), vector.getZ(), 1});
    }

    @Override
    public String toString() {
        if(this.getName() != null && !this.getName().equals("")){
            return this.getClass().getName() + ": " + getName();
        }
        return super.toString();
    }
}

