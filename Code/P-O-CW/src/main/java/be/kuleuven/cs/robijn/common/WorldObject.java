package be.kuleuven.cs.robijn.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class WorldObject {
    private WorldObject parent;
    private ArrayList<WorldObject> children = new ArrayList<>();
    private RealVector position = new ArrayRealVector(new double[]{0, 0, 0}, false);
    private RealVector rotation = new ArrayRealVector(new double[]{0, 0, 0}, false);
    private String name = "";

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

    /**
     * Returns the position of this object in world coordinates.
     * @return a non-null vector that is immutable.
     */
    public RealVector getWorldPosition() {
        if(parent == null){
            return getRelativePosition();
        }

        RealVector worldPosition = parent.getWorldPosition().add(this.getRelativePosition());
        return RealVector.unmodifiableRealVector(worldPosition);
    }

    /**
     * Returns the position of this object, relative to its parent.
     * @return a non-null vector that is immutable.
     */
    public RealVector getRelativePosition() {
        return RealVector.unmodifiableRealVector(position);
    }

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
}

