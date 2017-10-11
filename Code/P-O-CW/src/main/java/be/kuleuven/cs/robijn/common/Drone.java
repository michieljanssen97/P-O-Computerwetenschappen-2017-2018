package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.common.math.Vector3f;
import be.kuleuven.cs.robijn.testbed.VirtualTestbed;

public class Drone {
	
	/**
	 * Return the heading of this drone.
	 */
	public float getHeading() {
		return this.heading;
	}
	
	/**
	 * Check whether the given heading is a valid heading for
	 * any drone.
	 *  
	 * @param  heading
	 *         The heading to check.
	 * @return True if and only if the given heading is not negative
	 * 		   and less than 2*Math.PI.
	 *       | result == ((heading >= 0) && (heading < 2*Math.PI))
	*/
	public static boolean isValidHeading(float heading) {
		return ((heading >= 0) && (heading < 2*Math.PI));
	}
	
	/**
	 * Set the heading of this drone to the given heading.
	 * 
	 * @param  heading
	 *         The new heading for this drone.
	 * @post   The heading of this new drone is equal to
	 *         the given heading.
	 *       | new.getHeading() == heading
	 * @throws IllegalArgumentException
	 *         The given heading is not a valid heading for any drone.
	 *       | ! isValidHeading(heading)
	 */
	public void setHeading(float heading) 
			throws IllegalArgumentException {
		if (! isValidHeading(heading))
			throw new IllegalArgumentException();
		this.heading = heading;
	}
	
	/**
	 * Variable registering the heading of this drone.
	 */
	private float heading;
	
	/**
	 * Return the pitch of this drone.
	 */
	public float getPitch() {
		return this.pitch;
	}
	
	/**
	 * Check whether the given pitch is a valid heading for
	 * any drone.
	 *  
	 * @param  pitch
	 *         The pitch to check.
	 * @return True if and only if the given pitch is not negative
	 * 		   and less than 2*Math.PI.
	 *       | result == ((pitch >= 0) && (pitch < 2*Math.PI))
	*/
	public static boolean isValidPitch(float pitch) {
		return ((pitch >= 0) && (pitch < 2*Math.PI));
	}
	
	/**
	 * Set the pitch of this drone to the given pitch.
	 * 
	 * @param  pitch
	 *         The new pitch for this drone.
	 * @post   The pitch of this new drone is equal to
	 *         the given pitch.
	 *       | new.getPitch() == pitch
	 * @throws IllegalArgumentException
	 *         The given pitch is not a valid pitch for any drone.
	 *       | ! isValidPitch(pitch)
	 */
	public void setPitch(float pitch) 
			throws IllegalArgumentException {
		if (! isValidPitch(pitch))
			throw new IllegalArgumentException();
		this.pitch = pitch;
	}
	
	/**
	 * Variable registering the pitch of this drone.
	 */
	private float pitch;
	
	/**
	 * Return the roll of this drone.
	 */
	public float getRoll() {
		return this.roll;
	}
	
	/**
	 * Check whether the given roll is a valid roll for
	 * any drone.
	 *  
	 * @param  roll
	 *         The roll to check.
	 * @return True if and only if the given roll is not negative
	 * 		   and less than 2*Math.PI.
	 *       | result == ((roll >= 0) && (roll < 2*Math.PI))
	*/
	public static boolean isValidRoll(float roll) {
		return ((roll >= 0) && (roll < 2*Math.PI));
	}
	
	/**
	 * Set the roll of this drone to the given roll.
	 * 
	 * @param  roll
	 *         The new pitch for this drone.
	 * @post   The roll of this new drone is equal to
	 *         the given pitch.
	 *       | new.getRoll() == roll
	 * @throws IllegalArgumentException
	 *         The given roll is not a valid pitch for any drone.
	 *       | ! isValidPitch(pitch)
	 */
	public void setRoll(float roll) 
			throws IllegalArgumentException {
		if (! isValidRoll(roll))
			throw new IllegalArgumentException();
		this.roll = roll;
	}
	
	/**
	 * Variable registering the roll of this drone.
	 */
	private float roll;
	
	/**
	 * Return the position of the center of mass of this drone.
	 */
	public Vector3f getPosition() {
		return this.position;
	}
	
	/**
	 * Check whether the given position is a valid position for
	 * any drone.
	 *  
	 * @param  position
	 *         The position to check.
	 * @return True if and only if the given position is effective.
	 *       | result == (position != null)
	*/
	public static boolean isValidPosition(Vector3f position) {
		return (position != null);
	}
	
	/**
	 * Set the position of this drone to the given position.
	 * 
	 * @param  position
	 *         The new position for this drone.
	 * @post   The position of this new drone is equal to the given position.
	 *       | new.getPosition() == position
	 * @throws IllegalArgumentException
	 *         The given position is not a valid position for any drone.
	 *       | ! isValidPosition(position)
	 */
	public void setPosition(Vector3f position) 
			throws IllegalArgumentException {
		if (! isValidPosition(position))
			throw new IllegalArgumentException();
		this.position = position;
	}
	
	/**
	 * Variable registering the position of the center of mass of this drone.
	 */
	private Vector3f position = new Vector3f(0, 0, 0);
	
	/**
	 * Return the velocity of the center of mass of this drone.
	 */
	public Vector3f getVelocity() {
		return this.velocity;
	}
	
	/**
	 * Check whether the given velocity is a valid velocity for
	 * any drone.
	 *  
	 * @param  velocity
	 *         The velocity to check.
	 * @return True if and only if the given velocity is effective.
	 *       | result == (position != null)
	*/
	public static boolean isValidVelocity(Vector3f velocity) {
		return (velocity != null);
	}
	
	/**
	 * Set the velocity of this drone to the given velocity.
	 * 
	 * @param  velocity
	 *         The new velocity for this drone.
	 * @post   The velocity of this new drone is equal to the given velocity.
	 *       | new.getVelocity() == velocity
	 * @throws IllegalArgumentException
	 *         The given velocity is not a valid velocity for any drone.
	 *       | ! isValidVelocity(velocity)
	 */
	public void setVelocity(Vector3f velocity) 
			throws IllegalArgumentException {
		if (! isValidVelocity(velocity))
			throw new IllegalArgumentException();
		this.velocity = velocity;
	}
	
	/**
	 * Variable registering the velocity of the center of mass of this drone.
	 */
	private Vector3f velocity;
	
	/**
	 * Return the angular velocity of the heading of this drone.
	 */
	public float getHeadingAngularVelocity() {
		return this.headingAngularVelocity;
	}
	
	/**
	 * Check whether the given angular velocity of the heading is a valid angular velocity of the heading for
	 * any drone.
	 *  
	 * @param  headingAngularVelocity
	 *         The angular velocity of the heading to check.
	 * @return True if and only if the given angular velocity of the heading is not Not a Number and is finite.
	 *       | result == ((! Float.isNaN(headingAngularVelocity)) & (Float.isFinite(headingAngularVelocity))
	*/
	public static boolean isValidHeadingAngularVelocity(float headingAngularVelocity) {
		return ((! Float.isNaN(headingAngularVelocity)) & (Float.isFinite(headingAngularVelocity)));
	}
	
	/**
	 * Set the angular velocity of the heading of this drone to the given angular velocity of the heading.
	 * 
	 * @param  headingAngularVelocity
	 *         The new angular velocity of the heading for this drone.
	 * @post   The angular velocity of the heading of this new drone is equal to the given angular velocity of the heading.
	 *       | new.getHeadingAngularVelocity() == headingAngularVelocity
	 * @throws IllegalArgumentException
	 *         The given angular velocity of the heading is not a valid angular velocity of the heading for any drone.
	 *       | ! isValidHeadingAngularVelocity(headingAngularVelocity)
	 */
	public void setHeadingAngleVelocity(float headingAngularVelocity) 
			throws IllegalArgumentException {
		if (! isValidHeadingAngularVelocity(headingAngularVelocity))
			throw new IllegalArgumentException();
		this.headingAngularVelocity = headingAngularVelocity;
	}
	
	/**
	 * Variable registering the angular velocity of the heading of this drone.
	 */
	private float headingAngularVelocity = 0;
	
	/**
	 * Return the angular velocity of the pitch of this drone.
	 */
	public float getPitchAngularVelocity() {
		return this.pitchAngularVelocity;
	}
	
	/**
	 * Check whether the given angular velocity of the pitch is a valid angular velocity of the pitch for
	 * any drone.
	 *  
	 * @param  pitchAngularVelocity
	 *         The angular velocity of the pitch to check.
	 * @return True if and only if the given angular velocity of the pitch is not Not a Number and is finite.
	 *       | result == ((! Float.isNaN(pitchAngularVelocity)) & (Float.isFinite(pitchAngularVelocity))
	*/
	public static boolean isValidPitchAngularVelocity(float pitchAngularVelocity) {
		return ((! Float.isNaN(pitchAngularVelocity)) & (Float.isFinite(pitchAngularVelocity)));
	}
	
	/**
	 * Set the angular velocity of the pitch of this drone to the given angular velocity of the pitch.
	 * 
	 * @param  pitchAngularVelocity
	 *         The new angular velocity of the pitch for this drone.
	 * @post   The angular velocity of the pitch of this new drone is equal to the given angular velocity of the pitch.
	 *       | new.getPitchAngularVelocity() == pitchAngularVelocity
	 * @throws IllegalArgumentException
	 *         The given angular velocity of the pitch is not a valid angular velocity of the pitch for any drone.
	 *       | ! isValidPitchAngularVelocity(pitchAngularVelocity)
	 */
	public void setPitchAngleVelocity(float pitchAngularVelocity) 
			throws IllegalArgumentException {
		if (! isValidPitchAngularVelocity(pitchAngularVelocity))
			throw new IllegalArgumentException();
		this.pitchAngularVelocity = pitchAngularVelocity;
	}
	
	/**
	 * Variable registering the angular velocity of the pitch of this drone.
	 */
	private float pitchAngularVelocity = 0;
	
	/**
	 * Return the angular velocity of the roll of this drone.
	 */
	public float getRollAngularVelocity() {
		return this.rollAngularVelocity;
	}
	
	/**
	 * Check whether the given angular velocity of the roll is a valid angular velocity of the roll for
	 * any drone.
	 *  
	 * @param  rollAngularVelocity
	 *         The angular velocity of the roll to check.
	 * @return True if and only if the given angular velocity of the roll is not Not a Number and is finite.
	 *       | result == ((! Float.isNaN(rollAngularVelocity)) & (Float.isFinite(rollAngularVelocity))
	*/
	public static boolean isValidRollAngularVelocity(float rollAngularVelocity) {
		return ((! Float.isNaN(rollAngularVelocity)) & (Float.isFinite(rollAngularVelocity)));
	}
	
	/**
	 * Set the angular velocity of the roll of this drone to the given angular velocity of the roll.
	 * 
	 * @param  rollAngularVelocity
	 *         The new angular velocity of the roll for this drone.
	 * @post   The angular velocity of the roll of this new drone is equal to the given angular velocity of the roll.
	 *       | new.getRollAngularVelocity() == rollAngularVelocity
	 * @throws IllegalArgumentException
	 *         The given angular velocity of the roll is not a valid angular velocity of the roll for any drone.
	 *       | ! isValidRollAngularVelocity(rollAngularVelocity)
	 */
	public void setRollAngleVelocity(float rollAngularVelocity) 
			throws IllegalArgumentException {
		if (! isValidRollAngularVelocity(rollAngularVelocity))
			throw new IllegalArgumentException();
		this.rollAngularVelocity = rollAngularVelocity;
	}
	
	/**
	 * Variable registering the angular velocity of the roll of this drone.
	 */
	private float rollAngularVelocity = 0;
	
	/**
	 * Return the virtual testbed of this drone.
	 * A null reference is returned if this drone has no virtual testbed.
	 */
	public VirtualTestbed getVirtualTestbed() {
		return this.virtualTestbed;
	}
	
	/**
	 * Check whether this drone has a proper virtual testbed.
	 * 
	 * @return True if and only if the virtual testbed of this drone, if it is effective, in turn has this drone as
	 *         its drone.
	 *       | result == 
	 *       |   ((this.getVirtualTestbed() == null) || (this.getVirtualTestbed().getDrone() == this))
	 */
	public boolean hasProperVirtualTestbed() {
		return ((this.getVirtualTestbed() == null) || (this.getVirtualTestbed().getDrone() == this));
	}
	
	/**
	 * Check whether this drone has a virtual testbed.
	 * 
	 * @return True if this drone has an effective virtual testbed, false otherwise.
	 *       | result == (this.getVirtualTestbed() != null)
	 */
	public boolean hasVirtualTestbed() {
		return this.getVirtualTestbed() != null;
	}
	
	/**
	 * Add this drone to the given virtual testbed.
	 * 
	 * @param  virtualTestbed
	 *         The new virtual testbed for this drone.
	 * @post   The given virtual testbed is set as the virtual testbed of this drone.
	 *       | new.getVirtualTestbed() == virtualTestbed
	 * @post   This drone is set as the drone of the given virtual testbed.
	 *       | (new virtualTestbed).getDrone() == this
	 * @throws NullPointerException
	 *         The given virtual testbed is not effective.
	 *       | virtualTestbed == null
	 * @throws IllegalStateException
	 *         This drone has already a virtual testbed.
	 *       | this.hasVirtualTestbed()
	 */
	public void addVirtualTestbed(VirtualTestbed virtualTestbed) 
			throws NullPointerException, IllegalStateException {
		if (virtualTestbed == null)
			throw new NullPointerException();
		if (this.hasVirtualTestbed())
			throw new IllegalStateException("Each drone is located in at most one vitual testbed");
		setVirtualTestbed(virtualTestbed);
		virtualTestbed.addDrone(this);
	}
	
	/**
	 * Remove this drone from its virtual testbed, if any.
	 * 
	 * @post This drone has no longer a virtual testbed.
	 *     | ! new.hasVirtualTestbed()
	 * @post The former virtual testbed of this drone, if any, has no longer this drone
	 *       as its drone.
	 *     | if (this.hasVirtualTestbed())
	 *     |   then (new (this.getVirtualTestbed())).getDrone != this
	 */
	public void removeVirtualTestbed() {
		try {
			VirtualTestbed virtualTestbed = this.getVirtualTestbed();
			this.setVirtualTestbed(null);
			virtualTestbed.removeDrone(this);
		} catch (NullPointerException exc) {
			assert (! this.hasVirtualTestbed());
		}
	}
	
	/**
	 * Add the given virtual testbed as the virtual testbed of this drone.
	 * 
	 * @param virtualTestbed
	 *        The virtual testbed to be added as the virtual testbed of this drone.
	 * @post  The virtual testbed of this drone is the same as the given virtual testbed.
	 *      | new.getVirtualTestbed() == virtualTestbed
	 *        
	 */
	protected void setVirtualTestbed(VirtualTestbed virtualTestbed) {
		this.virtualTestbed = virtualTestbed;
	}
	
	/**
	 * Variable referencing the virtual testbed of this drone.
	 */
	private VirtualTestbed virtualTestbed = null;
}