package be.kuleuven.cs.robijn.common;

import org.apache.commons.math3.geometry.euclidean.threed.*;
import org.apache.commons.math3.linear.*;

import be.kuleuven.cs.robijn.common.exceptions.CrashException;
import be.kuleuven.cs.robijn.common.math.VectorMath;
import be.kuleuven.cs.robijn.tyres.*;
import interfaces.*;

/**
 * A class of drones.
 * 
 * @author Pieter Vandensande en Roy De Prins
 */
public class Drone extends WorldObject {
	
	
    //  -----------------   //
    //                      //
    //   INITIALISE DRONE   //
    //                      //
    //  -----------------   //
	
/**
 * Create a new Drone
 * 
 * @param config
 * 			The attributes of this drone
 * 			For example WingX, TailSize, EngineMass, ...
 * @param velocity
 * 			The starting velocity of the drone
 * @throws IllegalArgumentException
 * 			One of the given parameters is not a valid parameter for this drone.
 */
	public Drone(AutopilotConfig config, RealVector velocity) 
					throws IllegalArgumentException {
		this.droneID = config.getDroneID();
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
		if (! isValidVelocity(velocity))
			throw new IllegalArgumentException();
		this.velocity = velocity;
		this.initialVelocity = velocity;
		
		FrontWheel frontWheel = new FrontWheel(config);
		RightRearWheel rightRearWheel = new RightRearWheel(config);
		LeftRearWheel leftRearWheel = new LeftRearWheel(config);
		this.addChild(frontWheel);
		this.addChild(rightRearWheel);
		this.addChild(leftRearWheel);
		
	}
	
    //     -----------------     //
    //                           //
    //     DRONE ATTRIBUTES      //
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
	
	private final String droneID;
	
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
	
	public String getDroneID() {
		return this.droneID;
	}
	
	@Override
	public void setRelativePosition(RealVector vector) throws NullPointerException, CrashException {
		
		if (vector == null)
			throw new NullPointerException();
		
		if (vector.getEntry(1) <= 0)
			throw new CrashException();
		
		super.setRelativePosition(vector);
	}
	
	public float getTotalMass() {
		return (this.getEngineMass() + this.getTailMass() + 2*this.getWingMass());
	}
	
    //  -----------------   //
    //                      //
    //       HEADING        //
    //                      //
    //  -----------------   //
	/**
	 * Variable registering the heading (yaw) of this drone.
	 * The heading is equal to atan2(H . (-1, 0, 0), H . (0, 0, -1)), where H is the drone's heading vector
	 * (which we define as H0/||H0|| where H0 is the drone's forward vector ((0, 0, -1) in drone coordinates) projected onto the world XZ plane.
	 */
	private float heading = 0;
	
	/**
	 * Return the heading (yaw) of this drone.
	 * The heading is equal to atan2(H . (-1, 0, 0), H . (0, 0, -1)), where H is the drone's heading vector
	 * (which we define as H0/||H0|| where H0 is the drone's forward vector ((0, 0, -1) in drone coordinates) projected onto the world XZ plane.
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
	 * Check whether the given pitch is a valid pitch for
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
	 * atan2(R . U0, R . R0), where R is the drone's right direction ((1, 0, 0) in drone coordinates),
	 * R0 = H x (0, 1, 0), and U0 = R0 x F.
	 */
	private float roll = 0;
	
	/**
	 * Return the roll (bank) of this drone.
	 * atan2(R . U0, R . R0), where R is the drone's right direction ((1, 0, 0) in drone coordinates),
	 * R0 = H x (0, 1, 0), and U0 = R0 x F.
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
    //   VELOCITIES DRONE   //
    //                      //
    //  -----------------   //
	/**
	 * Variable registering the velocity of the center of mass of this drone.
	 */
	private RealVector velocity;
	
	private final RealVector initialVelocity;
	
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
	
	public RealVector getInitialVelocity() {
		return this.initialVelocity;
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

    //     -----------------      //
    //                            //
    //  TRANSFORMATION MATRICES   //				WORLD TO DRONE COORDINATES
    //                            //
    //     -----------------      //
	/**
	 * Transform the given vector from Heading-Pitch coordinates to Drone coordinates
	 * @param realVector
	 * 			The vector to transform from Heading-Pitch to Drone coordinates
	 * @return The given vector in Drone coordinates
	 */
	public RealVector rollTransformation(RealVector realVector){
		float rollAngle = this.getRoll();
		RealMatrix rollTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for roll
			{Math.cos(rollAngle),      Math.sin(rollAngle),    0},
			{-Math.sin(rollAngle),     Math.cos(rollAngle),    0}, 
			{0,                        0,                      1}
			}, false);
		return rollTransformation.operate(realVector);	
	}
	
	/**
	 * Transform the given vector from Heading coordinates to Heading-Pitch coordinates
	 * @param realVector
	 * 			The vector to transform from World to Drone coordinates
	 * @return The given vector in Heading-Pitch coordinates
	 */
	public RealVector pitchTransformation(RealVector realVector){
		float pitchAngle = this.getPitch();
		RealMatrix pitchTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for pitch
			{1,       0,                          0},
			{0,       Math.cos(pitchAngle),       Math.sin(pitchAngle)},
			{0,      -Math.sin(pitchAngle),       Math.cos(pitchAngle)}
			}, false);
		return pitchTransformation.operate(realVector);	
	}
	
	/**
	 * Transform the given vector from World coordinates to Heading coordinates
	 * @param realVector
	 * 			The vector to transform from World to Heading coordinates
	 * @return The given vector in Heading coordinates
	 */
	public RealVector headingTransformation(RealVector realVector){
		float headingAngle = this.getHeading();
		RealMatrix headingTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for heading
			{Math.cos(headingAngle),       0,          -Math.sin(headingAngle)},
			{0,                            1,           0}, 
			{Math.sin(headingAngle),       0,           Math.cos(headingAngle)}
			}, false);
		return headingTransformation.operate(realVector);	
	}
	
	/**
	 * Transform the given vector from World coordinates to Drone Coordinates
	 * @param realVector
	 * 			The vector to transform from World to Drone coordinates
	 * @return The given vector in Drone Coordinates
	 */
	public RealVector transformationToDroneCoordinates(RealVector realVector) {
		return this.rollTransformation(this.pitchTransformation(this.headingTransformation(realVector)));
	}
	
	public RealMatrix getRotationMatrix() {
		float rollAngle = this.getRoll();
		float pitchAngle = this.getPitch();
		float headingAngle = this.getHeading();
		RealMatrix inverseRollTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for roll
			{Math.cos(rollAngle),      -Math.sin(rollAngle),    0},
			{Math.sin(rollAngle),       Math.cos(rollAngle),    0}, 
			{0,                        0,                      1}
			}, false);
		RealMatrix inversePitchTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for pitch
			{1,       0,                          0},
			{0,       Math.cos(pitchAngle),       -Math.sin(pitchAngle)},
			{0,       Math.sin(pitchAngle),        Math.cos(pitchAngle)}
			}, false);
		RealMatrix inverseHeadingTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for heading
			{Math.cos(headingAngle),       0,            Math.sin(headingAngle)},
			{0,                            1,           0}, 
			{-Math.sin(headingAngle),       0,           Math.cos(headingAngle)}
			}, false);
		//return rollTransformation.multiply(pitchTransformation.multiply(headingTransformation));
		return inverseHeadingTransformation.multiply(inversePitchTransformation.multiply(inverseRollTransformation));
	}
	
    //     -----------------      //
    //                            //
    //  TRANSFORMATION MATRICES   //				DRONE TO WORLD COORDINATES
    //                            //
    //     -----------------      //
	/**
	 * Transform the given vector from Drone coordinates to Heading-Pitch coordinates
	 * @param realVector
	 * 			The vector to transform from Drone to Heading-Pitch coordinates
	 * @return The given vector in Heading-Pitch coordinates
	 */
	public RealVector inverseRollTransformation(RealVector realVector){
		float rollAngle = this.getRoll();
		RealMatrix inverseRollTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for inverse roll
			{Math.cos(rollAngle),      -Math.sin(rollAngle),       0},
			{Math.sin(rollAngle),       Math.cos(rollAngle),       0}, 
			{0,                         0,                         1}
			}, false);
		return inverseRollTransformation.operate(realVector);	
	}
	
	/**
	 * Transform the given vector from Heading-Pitch coordinates to Heading coordinates
	 * @param realVector
	 * 			The vector to transform from Heading-Pitch to Heading coordinates
	 * @return The given vector in Heading coordinates
	 */
	public RealVector inversePitchTransformation(RealVector realVector){
		float PitchAngle = this.getPitch();
		RealMatrix inversePitchTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for inverse pitch
			{1,       0,                        0},
			{0,       Math.cos(PitchAngle),    -Math.sin(PitchAngle)},
			{0,       Math.sin(PitchAngle),     Math.cos(PitchAngle)}
			}, false);
		return inversePitchTransformation.operate(realVector);	
	}
	
	/**
	 * Transform the given vector from Heading coordinates to World coordinates
	 * @param realVector
	 * 			The vector to transform from Heading to World coordinates
	 * @return The given vector in World coordinates
	 */
	public RealVector inverseHeadingTransformation(RealVector realVector){
		float headingAngle = this.getHeading();
		RealMatrix inverseHeadingTransformation = new Array2DRowRealMatrix(new double[][] { //transformation matrix for inverse heading
			{Math.cos(headingAngle),     0,       Math.sin(headingAngle)}, 
			{0,                          1,       0}, 
			{-Math.sin(headingAngle),    0,       Math.cos(headingAngle)}
			}, false);
		return inverseHeadingTransformation.operate(realVector);	
	}
	
	/**
	 * Transform the given vector from Drone coordinates to World Coordinates
	 * @param realVector
	 * 			The vector to transform from Drone to World coordinates
	 * @return The given vector in World Coordinates
	 */
	public RealVector transformationToWorldCoordinates(RealVector realVector) {
		return this.inverseHeadingTransformation(this.inversePitchTransformation(this.inverseRollTransformation(realVector)));
	}
	
	
    //     -----------------      //
    //                            //
    //  ANGULAR VELOCITY VECTORS  //
    //                            //
    //     -----------------      //
	/**
	 * Return the Heading Angular velocity of this drone as a vector in World Coordinates.
	 */
	public RealVector getHeadingAngularVelocityVector() {
		return new ArrayRealVector(new double[] {0, this.getHeadingAngularVelocity(), 0}, false);
	}
	
	/**
	 * Return the Pitch Angular velocity of this drone as a vector in World Coordinates.
	 */
	public RealVector getPitchAngularVelocityVector() throws IllegalStateException {
		return this.inverseHeadingTransformation(new ArrayRealVector(new double[] {this.getPitchAngularVelocity(), 0, 0}, false));
	}
	
	/**
	 * Return the Roll Angular velocity of this drone as a vector in World Coordinates.
	 */
	public RealVector getRollAngularVelocityVector() {
		return this.inverseHeadingTransformation(this.inversePitchTransformation(
				new ArrayRealVector(new double[] {0, 0, this.getRollAngularVelocity()}, false)));
	}
	
	
    //        -----------------         //
    //                                  //
    //   POSITIONS IN WORLD COORDINATES //
    //                                  //
    //        -----------------         //
	/**
	 * Return to position of the Left Wing of this drone in World Coordinates.
	 */
	public RealVector getLeftWingPosition() {
		return this.getWorldPosition().add(this.transformationToWorldCoordinates(
				new ArrayRealVector(new double[] {-this.getWingX(), 0, 0}, false)));
	}
	
	/**
	 * Return to position of the Right Wing of this drone in World Coordinates.
	 */
	public RealVector getRightWingPosition() {
		return this.getWorldPosition().add(this.transformationToWorldCoordinates(
				new ArrayRealVector(new double[] {this.getWingX(), 0, 0}, false)));
	}
	
	/**
	 * Return to position of the tail (Horizontal and Vertical Stabilizer) of this drone in World Coordinates.
	 */
	public RealVector getTailPosition() {
		return this.getWorldPosition().add(this.transformationToWorldCoordinates(
				new ArrayRealVector(new double[] {0, 0, this.getTailSize()}, false)));
	}
	
	/**
	 * Return to position of the Engine of the drone in World Coordinates.
	 */
	public RealVector getEnginePosition() {
		return this.getWorldPosition().add(this.transformationToWorldCoordinates(
				new ArrayRealVector(new double[] {0, 0, -this.getEngineDistance()}, false)));
	}
	
	
    //        -----------------        //
    //                                 //
    //             FORCES              //
    //                                 //
    //        -----------------        //
	/**
	 * Return the gravitational force on the Wing in World Coordinates.
	 */
	public RealVector getGravitationalForceWing() {
		return new ArrayRealVector(new double[] {0, -(this.getWingMass() * this.getGravity()), 0 }, false);
	}
	
	/**
	 * Return the gravitational force on the Tail in World Coordinates.
	 */
	public RealVector getGravitationalForceTail() {
		return new ArrayRealVector(new double[] {0, -(this.getTailMass() * this.getGravity()), 0 }, false);
	}
	
	/**
	 * Return the gravitational force on the Engine in World Coordinates.
	 */
	public RealVector getGravitationalForceEngine() {
		return new ArrayRealVector(new double[] {0, -(this.getEngineMass() * this.getGravity()), 0 }, false);
	}
	
	public RealVector getTotalGravitationalForce() {
		return this.getGravitationalForceWing().mapMultiply(2).add(this.getGravitationalForceEngine()).add(this.getGravitationalForceTail());
	}
	
	/**
	 * Return the Attack Vector for the horizontal Components (Wings and Horizontal Stabilizer) in World Coordinates.
	 * @param horInclination
	 * 			The Horizontal Inclination of the Wing or Horizontal Stabilizer.
	 */
	public RealVector getAttackVectorHor(float horInclination) {
		RealVector attackVectorH = new ArrayRealVector(new double[] {0, Math.sin(horInclination), -Math.cos(horInclination)}, false);
		return this.transformationToWorldCoordinates(attackVectorH);
	}
	
	/**
	 * return the Attack Vector for the Vertical Components (Vertical Stabilizer) in World Coordinates.
	 * @param verInclination
	 * 			The Vertical Inclination of the Vertical Stabilizer.
	 */
	public RealVector getAttackVectorVer(float verInclination) {
		RealVector attackVectorV = new ArrayRealVector(new double[] {-Math.sin(verInclination), 0, -Math.cos(verInclination)}, false);
		return this.transformationToWorldCoordinates(attackVectorV);
	}
	
	/**
	 * Return the Axis Vector of the Horizontal Components (Wings and Horizontal Stabilizer) in World Coordinates.
	 */
	public RealVector getAxisVectorHor() {
		RealVector axisVectorH = new ArrayRealVector(new double[] {1,0,0}, false);
		return this.transformationToWorldCoordinates(axisVectorH);
	}
	
	/**
	 * Return the Axis Vector of the Vertical Components (vertical Stabilizer) in World Coordinates.
	 */
	public RealVector getAxisVectorVer() {
		RealVector axisVectorV = new ArrayRealVector(new double[] {0,1,0}, false);
		return this.transformationToWorldCoordinates(axisVectorV);
	}
	
	/**
	 * Return the Normal of the Horizontal Components (Wings and Horizontal Stabilizer) as a Vector in World Coordinates.
	 * @param horInclination
	 * 			The Horizontal Inclination of the Wing or Horizontal Stabilizer in Drone Coordinates.
	 */
	public RealVector getNormalHor(float horInclination) {
		return VectorMath.crossProduct(this.getAxisVectorHor(), this.getAttackVectorHor(horInclination)); 
		//Cross product of 2 vectors in World Coordinates results in a vector in World Coordinates
	}
	
	/**
	 * Return the Normal of the Vertical Components (vertical Stabilizer) as a vector in World Coordinates.
	 * @param verInclination
	 * 			The Vertical Inclination of the Vertical Stabilizer in Drone Coordinates.
	 */
	public RealVector getNormalVer(float verInclination) {
		return VectorMath.crossProduct(this.getAxisVectorVer(), this.getAttackVectorVer(verInclination));
	}
	
	/**
	 * Calculate the Velocity of the point with a certain distance to the drone's center of mass in World Coordinates.
	 * @param velocity
	 * 			The velocity of the drone's center of mass.
	 * @param headingVelocity
	 * 			The heading angular velocity.
	 * @param pitchVelocity
	 * 			The pitch angular velocity.
	 * @param rollVelocity
	 * 			The roll angular velocity.
	 * @param distance
	 * 			The distance between the point and the drone's center of mass.
	 * @return The velocity of the point in World Coordinates.
	 */
	public RealVector calculateVelocityWorldCo(RealVector velocity, RealVector headingVelocity, RealVector pitchVelocity, RealVector rollVelocity, RealVector distance) {
		return (velocity
				.add(VectorMath.crossProduct(headingVelocity, distance))
				.add(VectorMath.crossProduct(pitchVelocity, distance))
				.add(VectorMath.crossProduct(rollVelocity, distance)));
	}
	
	/**
	 * Calculate the Angle Of Attack of a certain airfoil.
	 * @param normal
	 * 			The normal of the airfoil.
	 * @param projectedVelocity
	 * 			The projected velocity of the airfoil.
	 * @param attackVector
	 * 			The attack vector of the airfoil.
	 * @return	The Angle Of Attack of the airfoil.
	 */
	public float calculateAOA(RealVector normal, RealVector projectedVelocity, RealVector attackVector) {
		float AOA = (float) -Math.atan2(normal.dotProduct(projectedVelocity), attackVector.dotProduct(projectedVelocity));
		return AOA;
	}
	
	public RealVector getProjectedVelocityLeftWing() {
		RealVector distance = this.transformationToWorldCoordinates(new ArrayRealVector(new double[] {-this.getWingX(), 0, 0}, false)); //distance between left wing and center of mass in World Coordinates
		RealVector velocityWorldCoordinates = calculateVelocityWorldCo(this.getVelocity(), this.getHeadingAngularVelocityVector(), 
																	   this.getPitchAngularVelocityVector(), this.getRollAngularVelocityVector(), distance);
		
		RealVector velocityDroneCoordinates = this.transformationToDroneCoordinates(velocityWorldCoordinates);
		velocityDroneCoordinates.setEntry(0, 0); //Set first value (X) to zero
		return this.transformationToWorldCoordinates(velocityDroneCoordinates);
	}
	
	/**
	 * Calculate the Lift Force on the Left Wing.
	 * @param leftWingInclination
	 * 			The current Inclination of the left wing.
	 * @return The Lift Force on the Left Wing of this drone.
	 */
	public RealVector getLiftForceLeftWing(float leftWingInclination) throws IllegalArgumentException {
		RealVector projectedVelocity = this.getProjectedVelocityLeftWing();
		
		float AOA = this.calculateAOA(this.getNormalHor(leftWingInclination), projectedVelocity, this.getAttackVectorHor(leftWingInclination));
		
		float liftForce = (float) (AOA * this.getWingLiftSlope() * Math.pow(projectedVelocity.getNorm(),2));
		
		if ((liftForce >= 50) && ((AOA > this.getMaxAOA()) || (AOA < -this.getMaxAOA())))
			throw new IllegalArgumentException();
		
		return this.getNormalHor(leftWingInclination).mapMultiply(liftForce);
	}
	
	public RealVector getProjectedVelocityRightWing() {
		RealVector distance = this.transformationToWorldCoordinates(new ArrayRealVector(new double[] {this.getWingX(), 0, 0}, false));
		RealVector velocityWorldCoordinates = calculateVelocityWorldCo(this.getVelocity(), this.getHeadingAngularVelocityVector(), 
				   													   this.getPitchAngularVelocityVector(), this.getRollAngularVelocityVector(), distance);
		
		RealVector velocityDroneCoordinates = this.transformationToDroneCoordinates(velocityWorldCoordinates);
		velocityDroneCoordinates.setEntry(0, 0);
		return this.transformationToWorldCoordinates(velocityDroneCoordinates);
	}
	
	/**
	 * Calculate the Lift Force on the Right Wing.
	 * @param rightWingInclination
	 * 			The current Inclination of the Right wing.
	 * @return The Lift Force on the Right Wing of this drone.
	 */
	public RealVector getLiftForceRightWing(float rightWingInclination) throws IllegalArgumentException {
		RealVector projectedVelocity = this.getProjectedVelocityRightWing();
		
		float AOA = this.calculateAOA(this.getNormalHor(rightWingInclination), projectedVelocity, this.getAttackVectorHor(rightWingInclination));
		
		float liftForce = (float) (AOA * this.getWingLiftSlope() * Math.pow(projectedVelocity.getNorm(),2));
		
		if ((liftForce >= 50) && ((AOA > this.getMaxAOA()) || (AOA < -this.getMaxAOA())))
			throw new IllegalArgumentException();
		
		return this.getNormalHor(rightWingInclination).mapMultiply(liftForce);
	}
	
	public RealVector getProjectedVelocityHorStab() {
		RealVector distance = this.transformationToWorldCoordinates(new ArrayRealVector(new double[] {0, 0, this.getTailSize()}, false));
		RealVector velocityWorldCoordinates = calculateVelocityWorldCo(this.getVelocity(), this.getHeadingAngularVelocityVector(), 
				   													   this.getPitchAngularVelocityVector(), this.getRollAngularVelocityVector(), distance);
		RealVector velocityDroneCoordinates = this.transformationToDroneCoordinates(velocityWorldCoordinates);
		velocityDroneCoordinates.setEntry(0, 0);
		return this.transformationToWorldCoordinates(velocityDroneCoordinates);
	}
	
	/**
	 * Calculate the Lift Force on the Horizontal Stabilizer.
	 * @param horStabInclination
	 * 			The current Inclination of the Horizontal Stabilizer.
	 * @return The Lift Force on the Horizontal Stabilizer of this drone.
	 */
	public RealVector getLiftForceHorStab(float horStabInclination) throws IllegalArgumentException {
		RealVector projectedVelocity = this.getProjectedVelocityHorStab();
		
		float AOA = this.calculateAOA(this.getNormalHor(horStabInclination), projectedVelocity, this.getAttackVectorHor(horStabInclination));
		
		float liftForce = (float) (AOA * this.getHorStabLiftSlope() * Math.pow(projectedVelocity.getNorm(),2));
		
		if ((liftForce >= 50) && ((AOA > this.getMaxAOA()) || (AOA < -this.getMaxAOA())))
			throw new IllegalArgumentException();
		
		return this.getNormalHor(horStabInclination).mapMultiply(liftForce);
	}
	
	public RealVector getProjectedVelocityVerStab() {
		RealVector distance = this.transformationToWorldCoordinates(new ArrayRealVector(new double[] {0, 0, this.getTailSize()}, false));
		RealVector velocityWorldCoordinates = calculateVelocityWorldCo(this.getVelocity(), this.getHeadingAngularVelocityVector(), 
				   													   this.getPitchAngularVelocityVector(), this.getRollAngularVelocityVector(), distance);
		RealVector velocityDroneCoordinates = this.transformationToDroneCoordinates(velocityWorldCoordinates);
		velocityDroneCoordinates.setEntry(1, 0);
		return this.transformationToWorldCoordinates(velocityDroneCoordinates);
	}
	
	/**
	 * Calculate the Lift Force on the Vertical Stabilizer.
	 * @param verStabInclination
	 * 			The current Inclination of the Vertical Stabilizer.
	 * @return The Lift Force on the Vertical Stabilizer of the drone.
	 */
	public RealVector getLiftForceVerStab(float verStabInclination) throws IllegalArgumentException {
		RealVector projectedVelocity = this.getProjectedVelocityVerStab();
		
		float AOA = this.calculateAOA(this.getNormalVer(verStabInclination), projectedVelocity, this.getAttackVectorVer(verStabInclination));
		
		float liftForce = (float) (AOA * this.getVerStabLiftSlope() * Math.pow(projectedVelocity.getNorm(),2));
		
		if ((liftForce >= 50) && ((AOA > this.getMaxAOA()) || (AOA < -this.getMaxAOA())))
			throw new IllegalArgumentException();
		
		return this.getNormalVer(verStabInclination).mapMultiply(liftForce);
	}
	
	/**
	 * Calculate the drone's acceleration, with the second law of Newton:
	 * "The total force on this drone is equal to 
	 * the mass of this drone multiplied with the acceleration of this drone's center of mass."
	 * @param thrust
	 * 			The current Thrust of the Engine.
	 * @param leftWingInclination
	 * 			The current inclination of the Left Wing.
	 * @param rightWingInclination
	 * 			The current inclination of the Right Wing.
	 * @param horStabInclination
	 * 			The current inclination of the Horizontal Stabilizer.
	 * @param verStabInclination
	 * 			The current inclination of the Vertical Stabilizer.
	 * @return The acceleration of this drone's center of mass.
	 * @throws IllegalArgumentException
	 * 			The given thrust is not a valid thrust for any drone.
	 * 		  | thrust > this.maxThrust()
	 */
	public RealVector getAcceleration(float thrust, float leftWingInclination, float rightWingInclination, float horStabInclination, float verStabInclination
			, float frontBrakeForce, float leftRearBrakeForce, float rightRearBrakeForce)
			throws IllegalArgumentException {
		if (thrust > this.getMaxThrust())
			throw new IllegalArgumentException();
		
		RealVector liftForce = this.getLiftForceLeftWing(leftWingInclination)
				.add(this.getLiftForceRightWing(rightWingInclination))
				.add(this.getLiftForceHorStab(horStabInclination))
				.add(this.getLiftForceVerStab(verStabInclination));
			
		RealVector totalForce = this.getGravitationalForceEngine()
								.add(this.getGravitationalForceTail())
								.add(this.getGravitationalForceWing().mapMultiply(2)) 
								.add(liftForce)
								.add(this.transformationToWorldCoordinates(new ArrayRealVector(new double[] {0, 0, -thrust}, false)));
		
		for (Tyre tyres: this.getChildrenOfType(Tyre.class)) {
			float rearBrakeForce = 0;
			if(tyres instanceof RightRearWheel) { //TODO get rid of instanceof
				rearBrakeForce = rightRearBrakeForce;
			}
			else if(tyres instanceof LeftRearWheel) {
				rearBrakeForce = leftRearBrakeForce;
			}
			
			totalForce = totalForce.add(tyres.getTyreForce(this, frontBrakeForce, rearBrakeForce));
		}
		
		float totalMass = this.getEngineMass() + (2*this.getWingMass()) + this.getTailMass();
		
		return totalForce.mapMultiply(1/totalMass);
	}
	
	@Override
	public Rotation getRelativeRotation() {
		return new Rotation(this.getRotationMatrix().getData(), 0.0001);
	}
	
	/**
	 * Calculate the angular accelerations around this drone's center of mass, with the momentum equation.
	 * @param leftWingInclination
	 * 			The current inclination of the Left Wing.
	 * @param rightWingInclination
	 * 			The current inclination of the Right Wing.
	 * @param horStabInclination
	 * 			The current inclination of the Horizontal Stabilizer.
	 * @param verStabInclination
	 * 			The current inclination of the Vertical Stabilizer.
	 * @return  The angular accelerations around this drone's center of mass.
	 * 			The first element is the heading angular acceleration.
	 * 			The second element is the pitch angular acceleration.
	 * 			The third element is the roll angular acceleration.
	 */
	public float[] getAngularAccelerations(float leftWingInclination, float rightWingInclination, float horStabInclination, float verStabInclination,
			float frontBrakeForce, float leftRearBrakeForce, float rightRearBrakeForce) {
		float inertiaMatrixXX = (float) (this.getTailMass()*Math.pow(this.getTailSize(),2) + this.getEngineMass()*Math.pow(this.getEngineDistance(), 2));
		
		float inertiaMatrixZZ = (float) (2*(this.getWingMass()*Math.pow(this.getWingX(),2)));
		
		float inertiaMatrixYY = inertiaMatrixXX + inertiaMatrixZZ;
		
		RealVector totalAngularVelocityDroneCoordinates = this.transformationToDroneCoordinates(this.getHeadingAngularVelocityVector()
														  										.add(this.getPitchAngularVelocityVector())
														  										.add(this.getRollAngularVelocityVector()));
		
		RealMatrix inertiaMatrix = new Array2DRowRealMatrix(new double[][] {
				{inertiaMatrixXX, 0,                0},
				{0,               inertiaMatrixYY,  0}, 
				{0,               0,                inertiaMatrixZZ}
				}, false);
		RealVector angularMomentumDroneCoordinates = inertiaMatrix.operate(totalAngularVelocityDroneCoordinates);
		
		float pitch = this.getPitch();
		float roll = this.getRoll();
		RealMatrix coefficients = 
				new Array2DRowRealMatrix(new double[][] {
					{inertiaMatrixXX * Math.cos(pitch) * Math.sin(roll),    inertiaMatrixXX * Math.cos(roll),     0},
					{inertiaMatrixYY * Math.cos(pitch) * Math.cos(roll),    -inertiaMatrixYY * Math.sin(roll),    0}, 
					{-inertiaMatrixZZ * Math.sin(pitch),                    0,                                    inertiaMatrixZZ}
				}, false);
		
		DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
		
		RealVector momentOnLeftWing =   VectorMath.crossProduct(
										new ArrayRealVector(new double[] {-this.getWingX(), 0, 0}, false), //distance
										this.transformationToDroneCoordinates(this.getLiftForceLeftWing(leftWingInclination)) //forces
			    						);	
		RealVector momentOnRightWing =  VectorMath.crossProduct(
										new ArrayRealVector(new double[] {this.getWingX(), 0, 0}, false), //distance
										this.transformationToDroneCoordinates(this.getLiftForceRightWing(rightWingInclination)) //forces
										);
		RealVector momentOnTail =  	VectorMath.crossProduct(
							   new ArrayRealVector(new double[] {0, 0, this.getTailSize()}, false), //distance
							   this.transformationToDroneCoordinates(this.getLiftForceHorStab(horStabInclination).add(this.getLiftForceVerStab(verStabInclination))) //forces
							   );
		
		RealVector constants =  momentOnLeftWing
								.add(momentOnRightWing)
								.add(momentOnTail)
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
		
		for (Tyre tyres: this.getChildrenOfType(Tyre.class)) {
			float rearBrakeForce = 0;
			if(tyres instanceof RightRearWheel) { //TODO get rid of instanceof
				rearBrakeForce = rightRearBrakeForce;
			}
			else if(tyres instanceof LeftRearWheel) {
				rearBrakeForce = leftRearBrakeForce;
			}
			constants = constants.add(tyres.getTyreMoment(this, frontBrakeForce, rearBrakeForce));
		}
		
		RealVector solution = solver.solve(constants);
		
		return new float[] {(float)solution.getEntry(0), (float)solution.getEntry(1), (float)solution.getEntry(2)};
	}	
}