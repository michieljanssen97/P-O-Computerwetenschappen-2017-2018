package be.kuleuven.cs.robijn.worldObjects;

import be.kuleuven.cs.robijn.common.WorldObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Represents a camera in the 3D simulated world.
 */
public abstract class Camera extends WorldObject {
    public static final Predicate<WorldObject> HIDE_DEBUG_OBJECTS = obj -> obj.getName().startsWith("debug");

    private final ArrayList<Predicate<WorldObject>> visibilityFilters = new ArrayList<>();

    /**
     * Returns the distance from the camera to the near plane.
     * Any object closer than this distance to the camera will not be visible.
     * @return the near plane distance
     */
    public abstract float getNearPlane();

    /**
     * Sets the near plane distance. Must not be NaN or infinite.
     */
    public abstract void setNearPlane(float zNear);

    /**
     * Returns the distance from the camera to the far plane.
     * Any object further than this distance to the camera will not be visible.
     * @return the far plane distance.
     */
    public abstract float getFarPlane();

    /**
     * Sets the far plane distance. Must not be NaN or infinite.
     */
    public abstract void setFarPlane(float zFar);

    /**
     * Adds a visibility filter to this camera.
     * When the world is rendered, each object is passed to every filter.
     * If any filter returns false, the object is not rendered for this camera.
     * @param filter the predicate function that decides the visibility of each object.
     */
    public void addVisibilityFilter(Predicate<WorldObject> filter){
        visibilityFilters.add(filter);
    }

    /**
     * Removes a filter from this camera.
     * @param filter the predicate function.
     */
    public void removeVisibilityFilter(Predicate<WorldObject> filter){
        visibilityFilters.remove(filter);
    }

    /**
     * Returns an immutable version of the list of filters used for this camera.
     */
    public List<Predicate<WorldObject>> getVisibilityFilters(){
        return Collections.unmodifiableList(visibilityFilters);
    }

    /**
     * Returns true if the specified object is visible to this camera.
     * @param object the object to check.
     * @return true if the object is visible, false otherwise.
     */
    public boolean isVisible(WorldObject object){
        return visibilityFilters.stream().allMatch(p -> p.test(object));
    }
}

