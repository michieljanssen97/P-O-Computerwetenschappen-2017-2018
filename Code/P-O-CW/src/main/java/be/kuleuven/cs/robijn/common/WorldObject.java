package be.kuleuven.cs.robijn.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class WorldObject {
    private ArrayList<WorldObject> children = new ArrayList<>();
    private RealVector position = new ArrayRealVector(new double[]{0, 0, 0}, false);
    private RealVector rotation = new ArrayRealVector(new double[]{0, 0, 0}, false);

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
                return (T)child;
            }
        }

        return null;
    }

    /**
     * Adds a child to this object. The transformation of this object will now be relative to this object.
     * @param obj the new child. Must not be null
     */
    public void addChild(WorldObject obj){
        if(obj == null){
            throw new IllegalArgumentException("obj cannot be null");
        }

        children.add(obj);
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

        return children.remove(obj);
    }

    /**
     * Returns the position of this object, relative to its parent.
     * @return a non-null vector that is immutable.
     */
    public RealVector getPosition() {
    	return RealVector.unmodifiableRealVector(position);
    }

    /**
     * Sets the position of this object relative to its parent.
     * @param vector the new position vector of this object. Must not be null.
     */
    public void setPosition(RealVector vector) {
        if(vector == null){
            throw new IllegalArgumentException("vector cannot be null");
        }

        this.position = vector;
    }

    /**
     * Returns the rotation of this object, relative to its parent.
     * @return a non-null vector
     */
    public RealVector getRotation() {
        return RealVector.unmodifiableRealVector(rotation);
    }

    /**
     * Sets the rotation of this object relative to its parent.
     * @param vector the new rotational vector of this object. Must not be null.
     */
    public void setRotation(RealVector vector) {
        if(vector == null){
            throw new IllegalArgumentException("vector cannot be null");
        }

        this.rotation = vector;
    }
}

