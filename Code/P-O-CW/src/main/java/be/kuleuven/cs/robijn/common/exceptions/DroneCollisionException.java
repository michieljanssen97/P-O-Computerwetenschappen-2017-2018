package be.kuleuven.cs.robijn.common.exceptions;

import be.kuleuven.cs.robijn.worldObjects.Drone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DroneCollisionException extends RuntimeException {
    private final List<Drone> collidingDrones;

    public DroneCollisionException(List<Drone> collidingDrones){
        this.collidingDrones = new ArrayList<>(collidingDrones);
    }

    public List<Drone> getCollidingDrones() {
        return Collections.unmodifiableList(collidingDrones);
    }
}
