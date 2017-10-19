package be.kuleuven.cs.robijn.common;

import org.apache.commons.math3.linear.*;
import be.kuleuven.cs.robijn.common.math.VectorMath;
import be.kuleuven.cs.robijn.testbed.*;
import p_en_o_cw_2017.*;

public class Drone extends WorldObject {
	
	
    //  -----------------   //
    //                      //
    //   INITIALISE DRONE   //
    //                      //
    //  -----------------   //
	
	/*
	 * Create a drone
	 */
	public Drone(AutopilotConfig config, RealVector velocity) 
					throws IllegalArgumentException {
		if (! isValidWingX(config.getWingX()))
			throw new IllegalArgumentException();
		this.wingX = config.getWingX();
		if (! isValidTailSize(config.getTailSize()))
			throw new IllegalArgumentException();
		this.tailSize = config.getTailSize();
		if (! isValidEngineMass(config.getEngineMass()))
			throw new IllegalArgumentException();
		this.engineMass = config.getEngineMass();
		if (! isValidWingMass(config.getWingMass()))
			throw new IllegalArgumentException();
		this.wingMass = config.getWingMass();
		if (! isValidTailMass(config.getTailMass()))
			throw new IllegalArgumentException();
		this.tailMass = config.getTailMass();
		if (! isValidMaxThrust(config.getMaxThrust()))
			throw new IllegalArgumentException();
		this.maxThrust = config.getMaxThrust();
		if (! isValidMaxAOA(config.getMaxAOA()))
			throw new IllegalArgumentException();
		this.maxAOA = config.getMaxAOA();
		if (! isValidWingLiftSlope(config.getWingLiftSlope()))
			throw new IllegalArgumentException();
		this.wingLiftSlope = config.getWingLiftSlope();
		if (! isValidHorStabLiftSlope(config.getHorStabLiftSlope()))
			throw new IllegalArgumentException();
		this.horStabLiftSlope = config.getHorStabLiftSlope();
		if (! isValidVerStabLiftSlope(config.getVerStabLiftSlope()))
			throw new IllegalArgumentException();
		this.verStabLiftSlope = config.getVerStabLiftSlope();
		//if (! isValidPosition(position))
		//	throw new IllegalArgumentException();
		//this.position = position;
		if (! isValidVelocity(velocity))
			throw new IllegalArgumentException();
		this.velocity = velocity;
		//if (! isValidHeading(heading))
		//	throw new IllegalArgumentException();
		//this.heading = heading;
		//if (! isValidPitch(pitch))
		//	throw new IllegalArgumentException();
		//this.pitch = pitch;
		//if (! isValidRoll(roll))
		//	throw new IllegalArgumentException();
		//this.roll = roll;
		//if (! isValidHeadingAngularVelocity(headingAngularVelocity))
		//	throw new IllegalArgumentException();
		//this.headingAngularVelocity = headingAngularVelocity;
		//if (! isValidPitchAngularVelocity(pitchAngularVelocity))
		//	throw new IllegalArgumentException();
		//this.pitchAngularVelocity = pitchAngularVelocity;
		//if (! isValidRollAngularVelocity(rollAngularVelocity))
		//	throw new IllegalArgumentException();
		//this.rollAngularVelocity = rollAngularVelocity;
	}
	
    //     -----------------     //
    //                           //
    //  GETTERS DRONE ATTRIBUTES //
    //                           //
    //     -----------------     //
	
	private final float gravity = (float) 9.81;
	
	private final float engineMass;
	
	private final float tailSize;
	
	private final float wingX;
	
	private final float wingMass;
	
	private final float maxThrust;
	
	private final float maxAOA;
	
	private final float wingLiftSlope;
	
	private final float horStabLiftSlope;
	
	private final float verStabLiftSlope;
	
	private final float tailMass;
	
	
	public float getGravity() {
		return this.gravity;
	}

	public float getEngineMass() {
		return this.engineMass;
	}
	
	public static boolean isValidEngineMass(float engineMass) {
		return ((engineMass > 0) & (engineMass <= Float.MAX_VALUE));
	}
	
	public float getTailSize() {
		return this.tailSize;
	}
	
	public static boolean isValidTailSize(float tailSize) {
		return ((tailSize > 0) & (tailSize <= Float.MAX_VALUE));
	}
	
	public float getWingX() {
		return this.wingX;
	}
	
	public static boolean isValidWingX(float wingX) {
		return ((wingX > 0) & (wingX <= Float.MAX_VALUE));
	}
	
	public float getWingMass() {
		return this.wingMass;
	}
	
	public static boolean isValidWingMass(float wingMass) {
		return ((wingMass > 0) & (wingMass <= Float.MAX_VALUE));
	}
	
	public float getMaxThrust() {
		return this.maxThrust;
	}
	
	public static boolean isValidMaxThrust(float maxThrust) {
		return ((maxThrust >= 0) & (maxThrust <= Float.MAX_VALUE));
	}
	
	public float getMaxAOA() {
		return this.maxAOA;
	}
	
	public static boolean isValidMaxAOA(float maxAOA) {
		return ((maxAOA > -(Math.PI/2)) & (maxAOA < (Math.PI/2)));
	}
	
	public float getWingLiftSlope() {
		return this.wingLiftSlope;
	}
	
	public static boolean isValidWingLiftSlope(float wingLiftSlope) {
		return ((wingLiftSlope > 0) & (wingLiftSlope <= Float.MAX_VALUE));
	}
	
	public float getHorStabLiftSlope() {
		return this.horStabLiftSlope;
	}
	
	public static boolean isValidHorStabLiftSlope(float horStabLiftSlope) {
		return ((horStabLiftSlope > 0) & (horStabLiftSlope <= Float.MAX_VALUE));
	}
	
	public float getVerStabLiftSlope() {
		return this.verStabLiftSlope;
	}
	
	public static boolean isValidVerStabLiftSlope(float verStabLiftSlope) {
		return ((verStabLiftSlope > 0) & (verStabLiftSlope <= Float.MAX_VALUE));
	}
	
	public float getTailMass() {
		return this.tailMass;
	}
	
	public static boolean isValidTailMass(float tailMass) {
		return ((tailMass > 0) & (tailMass <= Float.MAX_VALUE));
	}

	public float getEngineDistance() {
		return ((this.getTailMass() * this.getTailSize()) / this.getEngineMass());
	}
	
    //  -----------------   //
    //                      //
    //       HEADING        //
    //                      //
    //  -----------------   //
	/**
	 * Variable registering the heading (yaw) of this drone.
	 * The heading is equal to atan2(H . (-1, 0, 0), H . (0, 0, -1)), 
	 * where H is the drone's heading vector (which we define as the drone's forward vector ((0, 0, -1) in drone coordinates) 
	 * projected onto the world XZ plane.
	 */
	private float heading = 0;
	
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

	
    //  -----------------   //
    //                      //
    //         PITCH        //
    //                      //
    //  -----------------   //
	/**
	 * Variable registering the pitch of this drone.
	 * The pitch is equal to atan2(F . (0, 1, 0), F . H), 
	 * where F is the drone's forward vector and H is the drone's heading vector.
	 */
	private float pitch = 0;
	
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
	
	
	
    //  -----------------   //
    //                      //
    //         ROLL         //
    //                      //
    //  -----------------   //
	/**
	 * Variable registering the roll (bank) of this drone.
	 * The roll is is equal to atan2(R . (0, 1, 0), R . R0), 
	 * where R is the drone's right direction ((1, 0, 0) in drone coordinates) and R0 = H x (0, 1, 0).
	 */
	private float roll = 0;
	
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
	
	
    //  -----------------   //
    //                      //
    //    POSITION DRONE    //
    //                      //
    //  -----------------   //
//	/**
//	 * Variable registering the position of the center of mass of this drone.
//	 */
//	private RealVector position = new ArrayRealVector(new double[] { 0, 0, 0 }, false);
	
//	/**
//	 * Return the position of the center of mass of this drone.
//	 */
//	public RealVector getPosition() {
//		return this.position;
//	}
//	
//	/**
//	 * Check whether the given position is a valid position for
//	 * any drone.
//	 *  
//	 * @param  position
//	 *         The position to check.
//	 * @return True if and only if the given position is effective.
//	 *       | result == (position != null)
//	*/
//	public static boolean isValidPosition(RealVector position) {
//		return (position != null);
//	}
//	
//	/**
//	 * Set the position of this drone to the given position.
//	 * 
//	 * @param  position
//	 *         The new position for this drone.
//	 * @post   The position of this new drone is equal to the given position.
//	 *       | new.getPosition() == position
//	 * @throws IllegalArgumentException
//	 *         The given position is not a valid position for any drone.
//	 *       | ! isValidPosition(position)
//	 */
//	public void setPosition(RealVector position) 
//			throws IllegalArgumentException {
//		if (! isValidPosition(position))
//			throw new IllegalArgumentException();
//		this.position = position;
//	}
	
	
    //  -----------------   //
    //                      //
    //    VELOCITY DRONE    //
    //                      //
    //  -----------------   //
	
	/**
	 * Variable registering the velocity of the center of mass of this drone.
	 */
	private RealVector velocity;
	
	/**
	 * Variable registering the angular velocity of the heading of this drone.
	 */
	private float headingAngularVelocity = 0;
	
	/**
	 * Variable registering the angular velocity of the pitch of this drone.
	 */
	private float pitchAngularVelocity = 0;
	
	/**
	 * Variable registering the angular velocity of the roll of this drone.
	 */
	private float rollAngularVelocity = 0;
	
	
	/**
	 * Return the velocity of the center of mass of this drone.
	 */
	public RealVector getVelocity() {
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
	public static boolean isValidVelocity(RealVector velocity) {
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
	public void setVelocity(RealVector velocity) 
			throws IllegalArgumentException {
		if (! isValidVelocity(velocity))
			throw new IllegalArgumentException();
		this.velocity = velocity;
	}
	
	
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
	public void setHeadingAngularVelocity(float headingAngularVelocity) 
			throws IllegalArgumentException {
		if (! isValidHeadingAngularVelocity(headingAngularVelocity))
			throw new IllegalArgumentException();
		this.headingAngularVelocity = headingAngularVelocity;
	}
	

	
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
	public void setPitchAngularVelocity(float pitchAngularVelocity) 
			throws IllegalArgumentException {
		if (! isValidPitchAngularVelocity(pitchAngularVelocity))
			throw new IllegalArgumentException();
		this.pitchAngularVelocity = pitchAngularVelocity;
	}

	
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
	public void setRollAngularVelocity(float rollAngularVelocity) 
			throws IllegalArgumentException {
		if (! isValidRollAngularVelocity(rollAngularVelocity))
			throw new IllegalArgumentException();
		this.rollAngularVelocity = rollAngularVelocity;
	}

	

	
    //  -----------------   //
    //                      //
    //    VIRTUAL TESTBED   //
    //                      //
    //  -----------------   //
//	/**
//	 * Variable referencing the virtual testbed of this drone.
//	 */
//	private VirtualTestbed virtualTestbed = null;
//	
//	/**
//	 * Return the virtual testbed of this drone.
//	 * A null reference is returned if this drone has no virtual testbed.
//	 */
//	public VirtualTestbed getVirtualTestbed() {
//		return this.virtualTestbed;
//	}
//	
//	/**
//	 * Check whether this drone has a proper virtual testbed.
//	 * 
//	 * @return True if and only if the virtual testbed of this drone, if it is effective, in turn has this drone as
//	 *         its drone.
//	 *       | result == 
//	 *       |   ((this.getVirtualTestbed() == null) || (this.getVirtualTestbed().getDrone() == this))
//	 */
//	public boolean hasProperVirtualTestbed() {
//		return ((this.getVirtualTestbed() == null) || (this.getVirtualTestbed().getDrone() == this));
//	}
//	
//	/**
//	 * Check whether this drone has a virtual testbed.
//	 * 
//	 * @return True if this drone has an effective virtual testbed, false otherwise.
//	 *       | result == (this.getVirtualTestbed() != null)
//	 */
//	public boolean hasVirtualTestbed() {
//		return this.getVirtualTestbed() != null;
//	}
	
//	/**
//	 * Add this drone to the given virtual testbed.
//	 * 
//	 * @param  virtualTestbed
//	 *         The new virtual testbed for this drone.
//	 * @post   The given virtual testbed is set as the virtual testbed of this drone.
//	 *       | new.getVirtualTestbed() == virtualTestbed
//	 * @post   This drone is set as the drone of the given virtual testbed.
//	 *       | (new virtualTestbed).getDrone() == this
//	 * @throws NullPointerException
//	 *         The given virtual testbed is not effective.
//	 *       | virtualTestbed == null
//	 * @throws IllegalStateException
//	 *         This drone has already a virtual testbed.
//	 *       | this.hasVirtualTestbed()
//	 */
//	public void addVirtualTestbed(VirtualTestbed virtualTestbed) 
//			throws NullPointerException, IllegalStateException {
//		if (virtualTestbed == null)
//			throw new NullPointerException();
//		if (this.hasVirtualTestbed())
//			throw new IllegalStateException("Each drone is located in at most one vitual testbed");
//		setVirtualTestbed(virtualTestbed);
//		virtualTestbed.addDrone(this);
//	}
//	
//	/**
//	 * Remove this drone from its virtual testbed, if any.
//	 * 
//	 * @post This drone has no longer a virtual testbed.
//	 *     | ! new.hasVirtualTestbed()
//	 * @post The former virtual testbed of this drone, if any, has no longer this drone
//	 *       as its drone.
//	 *     | if (this.hasVirtualTestbed())
//	 *     |   then (new (this.getVirtualTestbed())).getDrone != this
//	 */
//	public void removeVirtualTestbed() {
//		try {
//			VirtualTestbed virtualTestbed = this.getVirtualTestbed();
//			this.setVirtualTestbed(null);
//			virtualTestbed.removeDrone(this);
//		} catch (NullPointerException exc) {
//			assert (! this.hasVirtualTestbed());
//		}
//	}
	
	/**
	 * Add the given virtual testbed as the virtual testbed of this drone.
	 * 
	 * @param virtualTestbed
	 *        The virtual testbed to be added as the virtual testbed of this drone.
	 * @post  The virtual testbed of this drone is the same as the given virtual testbed.
	 *      | new.getVirtualTestbed() == virtualTestbed
	 *        
	 */
//	protected void setVirtualTestbed(VirtualTestbed virtualTestbed) {
//		this.virtualTestbed = virtualTestbed;
//	}

	
    //     -----------------      //
    //                            //
    //  TRANSFORMATION MATRICES   //				WORLD TO DRONE COORDINATES
    //                            //
    //     -----------------      //
	public RealVector rollTransformation(RealVector realVector) 
			throws IllegalStateException {
//		if (! this.hasVirtualTestbed())
//			throw new IllegalStateException("this drone has no virtual testbed");
		RealMatrix rollTransformation = new Array2DRowRealMatrix(new double[][] {{Math.cos(this.getRoll()), Math.sin(this.getRoll()), 0},
			{-Math.sin(this.getRoll()), Math.cos(this.getRoll()), 0}, {0, 0, 1}}, false);
		return rollTransformation.operate(realVector);	
	}
	
	public RealVector pitchTransformation(RealVector realVector) 
			throws IllegalStateException {
//		if (! this.hasVirtualTestbed())
//			throw new IllegalStateException("this drone has no virtual testbed");
		RealMatrix pitchTransformation = new Array2DRowRealMatrix(new double[][] {{1, 0, 0},
			{0, Math.cos(this.getPitch()), Math.sin(this.getPitch())},
			{0, -Math.sin(this.getPitch()), Math.cos(this.getPitch())}}, false);
		return pitchTransformation.operate(realVector);	
	}
	
	public RealVector headingTransformation(RealVector realVector) 
			throws IllegalStateException {
//		if (! this.hasVirtualTestbed())
//			throw new IllegalStateException("this drone has no virtual testbed");
		RealMatrix headingTransformation = new Array2DRowRealMatrix(new double[][] {{Math.cos(this.getHeading()), 0, -Math.sin(this.getHeading())},
			{0, 1, 0}, {Math.sin(this.getHeading()), 0, Math.cos(this.getHeading())}}, false);
		return headingTransformation.operate(realVector);	
	}
	
	public RealVector transformationToDroneCoordinates(RealVector realVector) {
		return this.rollTransformation(this.pitchTransformation(this.headingTransformation(realVector)));
	}
	
    //     -----------------      //
    //                            //
    //  TRANSFORMATION MATRICES   //				DRONE TO WORLD COORDINATES
    //                            //
    //     -----------------      //
	
	public RealVector inverseRollTransformation(RealVector realVector) 
			throws IllegalStateException {
//		if (! this.hasVirtualTestbed())
//			throw new IllegalStateException("this drone has no virtual testbed");
		RealMatrix inverseRollTransformation = new Array2DRowRealMatrix(new double[][] {{Math.cos(this.getRoll()), -Math.sin(this.getRoll()), 0},
			{Math.sin(this.getRoll()), Math.cos(this.getRoll()), 0}, {0, 0, 1}}, false);
		return inverseRollTransformation.operate(realVector);	
	}
	
	public RealVector inversePitchTransformation(RealVector realVector) 
			throws IllegalStateException {
//		if (! this.hasVirtualTestbed())
//			throw new IllegalStateException("this drone has no virtual testbed");
		RealMatrix inversePitchTransformation = new Array2DRowRealMatrix(new double[][] {{1, 0, 0},
			{0, Math.cos(this.getPitch()), -Math.sin(this.getPitch())},
			{0, Math.sin(this.getPitch()), Math.cos(this.getPitch())}}, false);
		return inversePitchTransformation.operate(realVector);	
	}
	
	public RealVector inverseHeadingTransformation(RealVector realVector) 
			throws IllegalStateException {
//		if (! this.hasVirtualTestbed())
//			throw new IllegalStateException("this drone has no virtual testbed");
		RealMatrix inverseHeadingTransformation = new Array2DRowRealMatrix(new double[][] {{Math.cos(this.getHeading()), 0,
			Math.sin(this.getHeading())}, {0, 1, 0}, {-Math.sin(this.getHeading()), 0, Math.cos(this.getHeading())}}, false);
		return inverseHeadingTransformation.operate(realVector);	
	}
	
	public RealVector transformationToWorldCoordinates(RealVector realVector) {
		return this.inverseHeadingTransformation(this.inversePitchTransformation(this.inverseRollTransformation(realVector)));
	}
	
	
    //     -----------------      //
    //                            //
    //  ANGULAR VELOCITY VECTOR   //
    //                            //
    //     -----------------      //
	
	public RealVector getHeadingAngularVelocityVector() {
//		if (! this.hasVirtualTestbed())
//			throw new IllegalStateException("this drone has no virtual testbed");
		return new ArrayRealVector(new double[] {0, this.getHeadingAngularVelocity(), 0}, false);
	}
	
	public RealVector getPitchAngularVelocityVector() throws IllegalStateException {
//		if (! this.hasVirtualTestbed())
//			throw new IllegalStateException("this drone has no virtual testbed");
		return this.inverseHeadingTransformation(new ArrayRealVector(new double[] {this.getPitchAngularVelocity(), 0, 0}, false));
	}
	
	public RealVector getRollAngularVelocityVector() {
//		if (! this.hasVirtualTestbed())
//			throw new IllegalStateException("this drone has no virtual testbed");
		return this.inverseHeadingTransformation(this.inversePitchTransformation(
				new ArrayRealVector(new double[] {0, 0, this.getRollAngularVelocity()}, false)));
	}
	
	
    //        -----------------         //
    //                                  //
    //   POSITION IN WORLD COORDINATES  //
    //                                  //
    //        -----------------         //
	
	public RealVector getLeftWingPosition() {
		return this.getPosition().add(this.transformationToWorldCoordinates(
				new ArrayRealVector(new double[] {-this.getWingX(), 0, 0}, false)));
	}
	
	public RealVector getRightWingPosition() {
		return this.getPosition().add(this.transformationToWorldCoordinates(
				new ArrayRealVector(new double[] {this.getWingX(), 0, 0}, false)));
	}
	
	public RealVector getTailPosition() {
		return this.getPosition().add(this.transformationToWorldCoordinates(
				new ArrayRealVector(new double[] {0, 0, this.getTailSize()}, false)));
	}
	
	public RealVector getEnginePosition() {
		return this.getPosition().add(this.transformationToWorldCoordinates(
				new ArrayRealVector(new double[] {0, 0, -this.getEngineDistance()}, false)));
	}
	
	
    //        -----------------        //
    //                                 //
    //             FORCES              //
    //                                 //
    //        -----------------        //
	
	/*
	 * Return the gravitational force for the given mass
	 */
	public RealVector getGravitationalForceWing() {
		return new ArrayRealVector(new double[] {0, this.getWingMass() * this.getGravity(), 0 }, false);
	}
	
	public RealVector getGravitationalForceTail() {
		return new ArrayRealVector(new double[] {0, this.getTailMass() * this.getGravity(), 0 }, false);
	}
	
	public RealVector getGravitationalForceEngine() {
		return new ArrayRealVector(new double[] {0, this.getEngineMass() * this.getGravity(), 0 }, false);
	}
	
	/*
	 * Attack vectors of the Wings and stabilizers
	 */
	public RealVector getAttackVectorHor(float horInclination) {
		RealVector attackVectorH = new ArrayRealVector(new double[] {0, Math.sin(horInclination), -Math.cos(horInclination)}, false);
		return this.transformationToWorldCoordinates(attackVectorH);
	}
	
	public RealVector getAttackVectorVer(float verInclination) {
		RealVector attackVectorV = new ArrayRealVector(new double[] {-Math.sin(verInclination), 0, -Math.cos(verInclination)}, false);
		return this.transformationToWorldCoordinates(attackVectorV);
	}
	
	/*
	 * Axis vectors of the Wings and Stabilizers
	 */
	public RealVector getAxisVectorHor() {
		RealVector axisVectorH = new ArrayRealVector(new double[] {1,0,0}, false);
		return this.transformationToDroneCoordinates(axisVectorH);
	}
	
	public RealVector getAxisVectorVer() {
		RealVector axisVectorV = new ArrayRealVector(new double[] {0,1,0}, false);
		return this.transformationToWorldCoordinates(axisVectorV);
	}
	
	/*
	 * Normals of the Wings and Stabilizers
	 */
	public RealVector getNormalHor(float horInclination) {
		return VectorMath.crossProduct(this.getAxisVectorHor(), this.getAttackVectorHor(horInclination));
	}
	
	public RealVector getNormalVer(float verInclination) {
		return VectorMath.crossProduct(this.getAxisVectorVer(), this.getAttackVectorVer(verInclination));
	}
	
	/*
	 * Calculate the lift force
	 */
	public RealVector getLiftForceLeftWing(float leftWingInclination) throws IllegalArgumentException {
		RealVector distance = this.transformationToWorldCoordinates(new ArrayRealVector(new double[] {-this.getWingX(), 0, 0}, false));
		RealVector velocityWorldCoordinates = this.getVelocity().add(VectorMath.crossProduct(this.getHeadingAngularVelocityVector(), distance))
				.add(VectorMath.crossProduct(this.getPitchAngularVelocityVector(), distance))
				.add(VectorMath.crossProduct(this.getRollAngularVelocityVector(), distance));
		RealVector velocityDroneCoordinates = this.transformationToDroneCoordinates(velocityWorldCoordinates);
		velocityDroneCoordinates.setEntry(0, 0);
		RealVector projectedVelocity = this.transformationToWorldCoordinates(velocityDroneCoordinates);
		
		float AOA = (float) -Math.atan2(this.getNormalHor(leftWingInclination).dotProduct(projectedVelocity)
				, this.getAttackVectorHor(leftWingInclination).dotProduct(projectedVelocity));
		if (AOA > this.maxAOA)
			throw new IllegalArgumentException();
		
		float liftForce = (float) (AOA * this.getWingLiftSlope() * Math.pow(projectedVelocity.getNorm(),2));
		return this.getNormalHor(leftWingInclination).mapMultiply(liftForce);
	}
	
	public RealVector getLiftForceRightWing(float rightWingInclination) throws IllegalArgumentException {
		RealVector distance = this.transformationToWorldCoordinates(new ArrayRealVector(new double[] {this.getWingX(), 0, 0}, false));
		RealVector velocityWorldCoordinates = this.getVelocity().add(VectorMath.crossProduct(this.getHeadingAngularVelocityVector(), distance))
				.add(VectorMath.crossProduct(this.getPitchAngularVelocityVector(), distance))
				.add(VectorMath.crossProduct(this.getRollAngularVelocityVector(), distance));
		RealVector velocityDroneCoordinates = this.transformationToDroneCoordinates(velocityWorldCoordinates);
		velocityDroneCoordinates.setEntry(0, 0);
		RealVector projectedVelocity = this.transformationToWorldCoordinates(velocityDroneCoordinates);
		
		float AOA = (float) -Math.atan2(this.getNormalHor(rightWingInclination).dotProduct(projectedVelocity)
				, this.getAttackVectorHor(rightWingInclination).dotProduct(projectedVelocity));
		if (AOA > this.maxAOA)
			throw new IllegalArgumentException();
		
		float liftForce = (float) (AOA * this.getWingLiftSlope() * Math.pow(projectedVelocity.getNorm(),2));
		return this.getNormalHor(rightWingInclination).mapMultiply(liftForce);
	}
	
	public RealVector getLiftForceHorStab(float horStabInclination) throws IllegalArgumentException {
		RealVector distance = this.transformationToWorldCoordinates(new ArrayRealVector(new double[] {0, 0, this.getTailSize()}, false));
		RealVector velocityWorldCoordinates = this.getVelocity().add(VectorMath.crossProduct(this.getHeadingAngularVelocityVector(), distance))
				.add(VectorMath.crossProduct(this.getPitchAngularVelocityVector(), distance))
				.add(VectorMath.crossProduct(this.getRollAngularVelocityVector(), distance));
		RealVector velocityDroneCoordinates = this.transformationToDroneCoordinates(velocityWorldCoordinates);
		velocityDroneCoordinates.setEntry(0, 0);
		RealVector projectedVelocity = this.transformationToWorldCoordinates(velocityDroneCoordinates);
		
		float AOA = (float) -Math.atan2(this.getNormalHor(horStabInclination).dotProduct(projectedVelocity)
				, this.getAttackVectorHor(horStabInclination).dotProduct(projectedVelocity));
		if (AOA > this.maxAOA)
			throw new IllegalArgumentException();
		
		float liftForce = (float) (AOA * this.getHorStabLiftSlope() * Math.pow(projectedVelocity.getNorm(),2));
		return this.getNormalHor(horStabInclination).mapMultiply(liftForce);
	}
	
	public RealVector getLiftForceVerStab(float verStabInclination) throws IllegalArgumentException {
		RealVector distance = this.transformationToWorldCoordinates(new ArrayRealVector(new double[] {0, 0, this.getTailSize()}, false));
		RealVector velocityWorldCoordinates = this.getVelocity().add(VectorMath.crossProduct(this.getHeadingAngularVelocityVector(), distance))
				.add(VectorMath.crossProduct(this.getPitchAngularVelocityVector(), distance))
				.add(VectorMath.crossProduct(this.getRollAngularVelocityVector(), distance));
		RealVector velocityDroneCoordinates = this.transformationToDroneCoordinates(velocityWorldCoordinates);
		velocityDroneCoordinates.setEntry(1, 0);
		RealVector projectedVelocity = this.transformationToWorldCoordinates(velocityDroneCoordinates);
		
		float AOA = (float) -Math.atan2(this.getNormalVer(verStabInclination).dotProduct(projectedVelocity)
				, this.getAttackVectorVer(verStabInclination).dotProduct(projectedVelocity));
		if (AOA > this.maxAOA)
			throw new IllegalArgumentException();
		
		float liftForce = (float) (AOA * this.getVerStabLiftSlope() * Math.pow(projectedVelocity.getNorm(),2));
		return this.getNormalVer(verStabInclination).mapMultiply(liftForce);
	}
	
	/**
	 * krachtenevenwicht
	 * @param thrust
	 * @param leftWingInclination
	 * @param rightWingInclination
	 * @param horStabInclination
	 * @param verStabInclination
	 * @return
	 */
	public RealVector getAcceleration(float thrust, float leftWingInclination, float rightWingInclination, float horStabInclination,
			float verStabInclination) throws IllegalArgumentException {
		if (thrust > this.getMaxThrust())
			throw new IllegalArgumentException();
		
		return this.getGravitationalForceEngine().add(this.getGravitationalForceTail()).add(this.getGravitationalForceWing().mapMultiply(2)) 
				.add(this.getLiftForceLeftWing(leftWingInclination)).add(this.getLiftForceRightWing(rightWingInclination))
				.add(this.getLiftForceHorStab(horStabInclination)).add(this.getLiftForceVerStab(verStabInclination))
				.add(this.transformationToWorldCoordinates(new ArrayRealVector(new double[] {0, 0, -thrust}, false)))
				.mapMultiply(1/(this.getEngineMass()+(2*this.getWingMass())+this.getTailMass()));
	}
	
	/**
	 * Eerste onbekende is heading angular acceleration, daarna pitch en daarna roll.
	 * @param leftWingInclination
	 * @param rightWingInclination
	 * @param horStabInclination
	 * @param verStabInclination
	 * @return
	 */
	public float[] getAngularAccelerations(float leftWingInclination, float rightWingInclination, float horStabInclination,
			float verStabInclination) {
		float inertiaMatrixXX = (float) (this.getTailMass()*Math.pow(this.getTailSize(),2) 
				+ this.getEngineMass()*Math.pow(this.getEngineDistance(), 2));
		float inertiaMatrixZZ = (float) (2*(this.getWingMass()*Math.pow(this.getWingX(),2)));
		float inertiaMatrixYY = inertiaMatrixXX + inertiaMatrixZZ;
		RealVector totalAngularVelocityDroneCoordinates = this.transformationToDroneCoordinates(this.getHeadingAngularVelocityVector()
				.add(this.getPitchAngularVelocityVector())
				.add(this.getRollAngularVelocityVector()));
		RealMatrix inertiaMatrix = new Array2DRowRealMatrix(new double[][] {{inertiaMatrixXX, 0, 0},
			{0, inertiaMatrixYY, 0}, {0, 0, inertiaMatrixZZ}}, false);
		RealVector angularMomentumDroneCoordinates = inertiaMatrix.operate(totalAngularVelocityDroneCoordinates);
		
		RealMatrix coefficients = 
				new Array2DRowRealMatrix(new double[][] {
					{inertiaMatrixXX * Math.cos(this.getPitch()) * Math.sin(this.getRoll()), inertiaMatrixXX * Math.cos(this.getRoll()), 0},
					{inertiaMatrixYY * Math.cos(this.getPitch()) * Math.cos(this.getRoll()), -inertiaMatrixYY * Math.sin(this.getRoll()), 0}, 
					{-inertiaMatrixZZ * Math.sin(this.getPitch()), 0, inertiaMatrixZZ}
				}, false);
		DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
		
		RealVector constants = 
				VectorMath.crossProduct(
					new ArrayRealVector(new double[] {-this.getWingX(), 0, 0}, false), 
					this.transformationToDroneCoordinates(this.getGravitationalForceWing().add(this.getLiftForceLeftWing(leftWingInclination)))
				)
				.add(VectorMath.crossProduct(
					new ArrayRealVector(new double[] {this.getWingX(), 0, 0}, false), 
					this.transformationToDroneCoordinates(this.getGravitationalForceWing().add(this.getLiftForceRightWing(rightWingInclination)))
				))
				.add(VectorMath.crossProduct(
					new ArrayRealVector(new double[] {0, 0, this.getTailSize()}, false), 
					this.transformationToDroneCoordinates(
						this.getGravitationalForceTail().add(this.getLiftForceHorStab(horStabInclination))
						.add(this.getLiftForceVerStab(verStabInclination))
					)
				))
				.subtract(VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates))
				.subtract(inertiaMatrix.operate(
					this.transformationToDroneCoordinates(
						VectorMath.crossProduct(this.getHeadingAngularVelocityVector(), this.getPitchAngularVelocityVector())
						.add(VectorMath.crossProduct(
							this.getHeadingAngularVelocityVector().add(this.getPitchAngularVelocityVector()),
							this.getRollAngularVelocityVector()
						))
					)
				));
		RealVector solution = solver.solve(constants);
		
		return new float[] {(float)solution.getEntry(0), (float)solution.getEntry(1), (float)solution.getEntry(2)};
	}
}