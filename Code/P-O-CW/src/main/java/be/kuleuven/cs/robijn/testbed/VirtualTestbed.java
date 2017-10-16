package be.kuleuven.cs.robijn.testbed;

import be.kuleuven.cs.robijn.common.Drone;

public class VirtualTestbed {
	
	public VirtualTestbed(Drone drone) {
		if (drone != null)
			addDrone(drone);
	}
	
	public VirtualTestbed() {
		this(null);
	}
	
	/**
	 * Return the drone of this virtual testbed.
	 * A null reference is returned if this virtual testbed has no drone.
	 */
	public Drone getDrone() {
		return this.drone;
	}
	
	/**
	 * Check whether this virtual testbed has a proper drone.
	 * 
	 * @return True if and only if the drone of this virtual testbed, if it is effective, in turn has this virtual testbed
	 *         as its virtual testbed.
	 *       | result == 
	 *       |   ((this.getDrone() == null) || (this.getDrone.getVirtualTestbed() == this))
	 */
	public boolean hasProperDrone() {
		return ((this.getDrone() == null) || (this.getDrone().getVirtualTestbed() == this));
	}
	
	/**
	 * Check whether this virtual testbed has a drone.
	 * 
	 * @return True if this virtual testbed has an effective drone, false otherwise.
	 *       | result == (this.getDrone() != null)
	 */
	public boolean hasDrone() {
		return this.getDrone() != null;
	}

	/**
	 * Add the given drone to this virtual testbed.
	 * 
	 * @param  drone
	 *         The drone to be added.
	 * @post  
	 *       | new.getDrone() == drone
	 * @throws NullPointerException
	 *       | drone == null
	 * @throws IllegalArgumentException
	 *       | drone.getVirtualTestbed() != this
	 */
	public void addDrone(Drone drone) throws NullPointerException, IllegalArgumentException {
		if (drone.getVirtualTestbed() != this)
			throw new IllegalArgumentException();
		this.drone = drone;
	}

	/**
	 * Remove the given drone from this virtual testbed.
	 * 
	 * @param  drone
	 *         The drone to be removed.
	 * @post   
	 *       | ! new.hasDrone()
	 * @throws IllegalArgumentException
	 *       | this.getDrone() != drone
	 * @throws IllegalArgumentException
	 *       | drone.getVirtualTestbed() != null
	 */
	public void removeDrone(Drone drone) throws IllegalArgumentException {
		if (this.getDrone() != drone)
			throw new IllegalArgumentException();
		if (drone.getVirtualTestbed() != null)
			throw new IllegalArgumentException();
		this.drone = null;
	}
	
	/**
	 * Variable referencing the drone of this virtual testbed.
	 */
	private Drone drone = null;
}