package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.common.math.Vector3f;
import be.kuleuven.cs.robijn.testbed.VirtualTestbed;

public class Drone {
	
	private final int NB_AXES = 3; // x-, y- and z-axis
	
	/*
	 * Create a drone
	 */
	public Drone(float wingX, float tailSize, float engineMass, float wingMass, float tailMass, float maxThrust, float maxAOA,
			float wingLiftSlope, float horStabLiftSlope, float verStabLiftSlope, Vector3f position, Vector3f velocity, float heading,
			float pitch, float roll, float headingAngularVelocity, float pitchAngularVelocity, float rollAngularVelocity,
			VirtualTestbed virtualTestbed) 
					throws IllegalArgumentException {
		if (! isValidWingX(wingX))
			throw new IllegalArgumentException();
		this.wingX = wingX;
		if (! isValidTailSize(tailSize))
			throw new IllegalArgumentException();
		this.tailSize = tailSize;
		if (! isValidEngineMass(engineMass))
			throw new IllegalArgumentException();
		this.engineMass = engineMass;
		if (! isValidWingMass(wingMass))
			throw new IllegalArgumentException();
		this.wingMass = wingMass;
		if (! isValidTailMass(tailMass))
			throw new IllegalArgumentException();
		this.tailMass = tailMass;
		if (! isValidMaxThrust(maxThrust))
			throw new IllegalArgumentException();
		this.maxThrust = maxThrust;
		if (! isValidMaxAOA(maxAOA))
			throw new IllegalArgumentException();
		this.maxAOA = maxAOA;
		if (! isValidWingLiftSlope(wingLiftSlope))
			throw new IllegalArgumentException();
		this.wingLiftSlope = wingLiftSlope;
		if (! isValidHorStabLiftSlope(horStabLiftSlope))
			throw new IllegalArgumentException();
		this.horStabLiftSlope = horStabLiftSlope;
		if (! isValidVerStabLiftSlope(verStabLiftSlope))
			throw new IllegalArgumentException();
		this.verStabLiftSlope = verStabLiftSlope;
		if (! isValidPosition(position))
			throw new IllegalArgumentException();
		this.position = position;
		if (! isValidVelocity(velocity))
			throw new IllegalArgumentException();
		this.velocity = velocity;
		if (! isValidHeading(heading))
			throw new IllegalArgumentException();
		this.heading = heading;
		if (! isValidPitch(pitch))
			throw new IllegalArgumentException();
		this.pitch = pitch;
		if (! isValidRoll(roll))
			throw new IllegalArgumentException();
		this.roll = roll;
		if (! isValidHeadingAngularVelocity(headingAngularVelocity))
			throw new IllegalArgumentException();
		this.headingAngularVelocity = headingAngularVelocity;
		if (! isValidPitchAngularVelocity(pitchAngularVelocity))
			throw new IllegalArgumentException();
		this.pitchAngularVelocity = pitchAngularVelocity;
		if (! isValidRollAngularVelocity(rollAngularVelocity))
			throw new IllegalArgumentException();
		this.rollAngularVelocity = rollAngularVelocity;
		if (virtualTestbed != null)
			addVirtualTestbed(virtualTestbed);
	}
	
	public Drone(float wingX, float tailSize, float engineMass, float wingMass, float tailMass, float maxThrust, float maxAOA,
			float wingLiftSlope, float horStabLiftSlope, float verStabLiftSlope, Vector3f position, Vector3f velocity, float heading,
			float pitch, float roll, float headingAngularVelocity, float pitchAngularVelocity, float rollAngularVelocity)
					throws IllegalArgumentException {
		this(wingX, tailSize, engineMass, wingMass, tailMass, maxThrust, maxAOA, wingLiftSlope, horStabLiftSlope, verStabLiftSlope,
				position, velocity, heading, pitch, roll, headingAngularVelocity, pitchAngularVelocity, rollAngularVelocity, null);
	}
	
	/**
	 * Return the heading (yaw) of this drone.
	 * The heading is equal to atan2(H . (-1, 0, 0), H . (0, 0, -1)), 
	 * where H is the drone's heading vector (which we define as the drone's forward vector ((0, 0, -1) in drone coordinates) 
	 * projected onto the world XZ plane.
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
	 * Variable registering the heading (yaw) of this drone.
	 * The heading is equal to atan2(H . (-1, 0, 0), H . (0, 0, -1)), 
	 * where H is the drone's heading vector (which we define as the drone's forward vector ((0, 0, -1) in drone coordinates) 
	 * projected onto the world XZ plane.
	 */
	private float heading;
	
	/**
	 * Return the pitch of this drone.
	 * The pitch is equal to atan2(F . (0, 1, 0), F . H), 
	 * where F is the drone's forward vector and H is the drone's heading vector.
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
	 * The pitch is equal to atan2(F . (0, 1, 0), F . H), 
	 * where F is the drone's forward vector and H is the drone's heading vector.
	 */
	private float pitch;
	
	/**
	 * Return the roll (bank) of this drone.
	 * The roll is is equal to atan2(R . (0, 1, 0), R . R0), 
	 * where R is the drone's right direction ((1, 0, 0) in drone coordinates) and R0 = H x (0, 1, 0).
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
	 * Variable registering the roll (bank) of this drone.
	 * The roll is is equal to atan2(R . (0, 1, 0), R . R0), 
	 * where R is the drone's right direction ((1, 0, 0) in drone coordinates) and R0 = H x (0, 1, 0).
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
	private Vector3f position;
	
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
	private float headingAngularVelocity;
	
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
	private float pitchAngularVelocity;
	
	/**
	 * Return the angular velocity of the roll of this drone.
	 */
	public float getRollAngularVelocity() {
		return this.rollAngularVelocity;
	}
	
	/**
	 * Check whether the given angular velocity of the roll  is a valid angular velocity of the roll for
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
	private float rollAngularVelocity;
	
	public float getGravity() {
		return this.gravity;
	}
	
	private final float gravity = (float) 9.81;
	
	private final float engineMass;
	
	public static boolean isValidEngineMass(float engineMass) {
		return ((engineMass > 0) & (engineMass <= Float.MAX_VALUE));
	}
	
	private final float tailSize;
	
	public static boolean isValidTailSize(float tailSize) {
		return ((tailSize > 0) & (tailSize <= Float.MAX_VALUE));
	}
	
	private final float wingX;
	
	public static boolean isValidWingX(float wingX) {
		return ((wingX > 0) & (wingX <= Float.MAX_VALUE));
	}
	
	private final float wingMass;
	
	public static boolean isValidWingMass(float wingMass) {
		return ((wingMass > 0) & (wingMass <= Float.MAX_VALUE));
	}
	
	private final float maxThrust;
	
	public static boolean isValidMaxThrust(float maxThrust) {
		return ((maxThrust >= 0) & (maxThrust <= Float.MAX_VALUE));
	}
	
	private final float maxAOA;
	
	public static boolean isValidMaxAOA(float maxAOA) {
		return ((maxAOA >= 0) & (maxAOA < 2*Math.PI));
	}
	
	private final float wingLiftSlope;
	
	public static boolean isValidWingLiftSlope(float wingLiftSlope) {
		return ((wingLiftSlope > 0) & (wingLiftSlope <= Float.MAX_VALUE));
	}
	
	private final float horStabLiftSlope;
	
	public static boolean isValidHorStabLiftSlope(float horStabLiftSlope) {
		return ((horStabLiftSlope > 0) & (horStabLiftSlope <= Float.MAX_VALUE));
	}
	
	private final float verStabLiftSlope;
	
	public static boolean isValidVerStabLiftSlope(float verStabLiftSlope) {
		return ((verStabLiftSlope > 0) & (verStabLiftSlope <= Float.MAX_VALUE));
	}
	
	private final float tailMass;
	
	public static boolean isValidTailMass(float tailMass) {
		return ((tailMass > 0) & (tailMass <= Float.MAX_VALUE));
	}
	
	private Vector3f enginePos = null;
	
	public Vector3f getEnginePosition() {
		return this.enginePos;
	}
	
	/*
	 * Setter for the position of the engine in drone coordinates
	 */
	public void setEnginePosition() {
		this.enginePos.setX(0);
		this.enginePos.setY(0);
		this.enginePos.setZ((-this.getTailMass() * this.getTailSize()) / this.getEngineMass());
	}
	
	
	/*
	 * getters for the drone's attributes
	 */
	public float getWingX() {
		return this.wingX;
	}
	
	public float getTailSize() {
		return this.tailSize;
	}
	
	public float getEngineMass() {
		return this.engineMass;
	}
	
	public float getWingMass() {
		return this.wingMass;
	}
	
	public float getTailMass() {
		return this.tailMass;
	}
	
	public float getMaxThrust() {
		return this.maxThrust;
	}
	
	public float getMaxAOA() {
		return this.maxAOA;
	}
	
	public float getWingLiftSlope() {
		return this.wingLiftSlope;
	}
	
	public float getHorStabLiftSlope() {
		return this.horStabLiftSlope;
	}
	
	public float getVerStabLiftSlope() {
		return this.verStabLiftSlope;
	}
	
	/*
	 * Return the gravitational force for the given mass
	 */
	public float getGravitationalForce() {
		return (this.getEngineMass() + this.getTailMass() + (2*this.getWingMass())) * this.getGravity();
	}
	
	/*
	 * Calculate the lift force
	 */
	public Vector3f getLiftForce(Vector3f normal, float AOA, float liftslope, float projectedVelocityScalar) {
		float scalar = (float) (AOA * liftslope * Math.pow(projectedVelocityScalar,2));
		
		Vector3f liftForce = new Vector3f(normal.getX() * scalar, normal.getY() * scalar, normal.getZ() * scalar);
		
		return liftForce;
	}
	
	/*
	 * Calculate the Projected Velocity vector of the drone as a Vector in x-, y- and z-coordinates
	 * @param 	droneVelocityVector
	 * 			The velocity vector of the drone
	 * 
	 * @param	windVelocityVector
	 * 			The Velocity vector of the wind
	 * 
	 * @Return	Returns the accumulated velocity of the drone and the wind, projected onto the correct axis.
	 */
	public Vector3f getProjectedVelocityVector(Vector3f droneVelocityVector, Vector3f windVelocityVector) {
		Vector3f accumulatedVelocity = new Vector3f(droneVelocityVector.getX() - windVelocityVector.getX(),
													droneVelocityVector.getY() - windVelocityVector.getY(),
													droneVelocityVector.getZ() - windVelocityVector.getZ());
		//TODO project correctly
		return accumulatedVelocity;
	}
	
	/*
	 * Calculate the Projected Velocity of the drone as a Scalar.
	 */
	public float getProjectedVelocityScalar(Vector3f projectedVelocityVector) {
		return projectedVelocityVector.length();
	}
	
	
	/*
	 * Attack vectors of the Wings and stabilizers
	 */
	public Vector3f getAttackVectorLeftWing(float leftWingInclination) {
		Vector3f attackVectorLW = new Vector3f(0, (float) Math.sin(leftWingInclination), (float) -Math.cos(leftWingInclination));
		return attackVectorLW;
	}

	public Vector3f getAttackVectorRightWing(float rightWingInclination) {
		Vector3f attackVectorRW = new Vector3f(0, (float) Math.sin(rightWingInclination), (float) -Math.cos(rightWingInclination));
		return attackVectorRW;
	}
	
	public Vector3f getAttackVectorHorizontalStab(float horStabInclination) {
		Vector3f attackVectorHS = new Vector3f(0, (float) Math.sin(horStabInclination), (float) -Math.cos(horStabInclination));
		return attackVectorHS;
	}
	
	public Vector3f getAttackVectorVerticalStab(float verStabInclination) {
		Vector3f attackVectorVS = new Vector3f((float) -Math.sin(verStabInclination), 0, (float) -Math.cos(verStabInclination));
		return attackVectorVS;
	}
		
	/*
	 * Normals of the Wings and Stabilizers
	 */
	public Vector3f getNormalLeftWing() {
		return (this.getAxisvectorLeftWing()).crossProduct(this.getAttackVectorLeftWing());
	}
	
	public Vector3f getNormaRightWing() {
		return this.getAxisVectorRightWing().crossProduct(this.getAttackVectorRightWing());
	}
	
	public Vector3f getNoralHorizontalStab() {
		return this.getAxisVectorHorizontalStab().crossProduct(this.getAttackVectorHorizontalStab());
	}
	
	public Vector3f getNormalVerticalStab() {
		return this.getAxisVectorVerticalStab().crossProduct(this.getAttackVectorVerticalStab());
	}

	
	/*
	 * Axis vectors of the Wings and Stabilizers
	 */
	public Vector3f getAxisvectorLeftWing() {
		Vector3f axisVectorLW = new Vector3f(1,0,0);
		return axisVectorLW;
	}
	
	public Vector3f getAxisVectorRightWing() {
		Vector3f axisVectorRW = new Vector3f(1,0,0);
		return axisVectorRW;
	}
	
	public Vector3f getAxisVectorHorizontalStab() {
		Vector3f axisVectorHS = new Vector3f(1,0,0);
		return axisVectorHS;
	}
	
	public Vector3f getAxisVectorVerticalStab() {
		Vector3f axisVectorVS = new Vector3f(0,1,0);
		return axisVectorVS;
	}
	
	/*
	 * Calculate the Angle Of Attack
	 */
	public float getAngleOfAttack(Vector3f normal, Vector3f projSpeedVector, Vector3f attackVector) {
		float aoa = (float) -Math.atan2(normal.dot(projSpeedVector), attackVector.dot(projSpeedVector));
		if (aoa > this.maxAOA)
			throw new IndexOutOfBoundsException();
		return aoa;
	}
	
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