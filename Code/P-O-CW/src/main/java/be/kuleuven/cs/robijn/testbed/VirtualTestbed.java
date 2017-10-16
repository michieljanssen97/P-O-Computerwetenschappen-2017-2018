package be.kuleuven.cs.robijn.testbed;

import be.kuleuven.cs.robijn.common.Drone;
import be.kuleuven.cs.robijn.common.math.Vector3f;

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
	
	public Vector3f transformationToWorldCoordinates(Vector3f vector3f) 
			throws IllegalStateException {
		if (! this.hasDrone())
			throw new IllegalStateException("this virtual testbad has no drone");
		Matrix roltransformation = new Matrix({{(float)Math.cos(this.getDrone().getRoll()), -(float)Math.sin(this.getDrone().getRoll()), 0},
			{(float)Math.sin(this.getDrone().getRoll()), (float)Math.cos(this.getDrone().getRoll()), 0}, {0, 0, 1}});
		Matrix pitchtransformation = new Matrix({{1, 0, 0},
			{0, (float)Math.cos(this.getDrone().getPitch()), -(float)Math.sin(this.getDrone().getPitch())}, {0, sin(pitch), cos(pitch)}});
		Matrix headingtransformation = new Matrix({{(float)Math.cos(this.getDrone().getHeading()), 0, (float)Math.sin(this.getDrone().getHeading())},
			{0, 1, 0}, {-(float)Math.sin(this.getDrone().getHeading()), 0, (float)Math.cos(this.getDrone().getHeading())}});
		return headingtransformation*pitchtransformation*roltransformation*vector3f;
	}
}


/*



The drone consists of an engine and four airfoils (the two wings and the

horizontal and vertical stabilizers). In the drone coordinate system,

- the drone's center of gravity is at (0, 0, 0)

- the left wing is at (-wingX, 0, 0). (That is, its mass is modeled as a point

  mass at that location, and its lift force is modeled as a point force at that

  location.

- the right wing is at (wingX, 0, 0)

- the horizontal and vertical stabilizers are at (0, 0, tailSize)

- the engine is somewhere on the negative Z axis. (Its location is determined

  by the fact that the center of gravity is at (0, 0, 0).) Its mass, too, is

  modeled as a point mass and its thrust force as a point force at the same

  location.

The drone coordinate system is right-handed; (0, 1, 0) points towards the top

of the drone.



The drone's geometry is not completely fixed: each airfoil can be rotated

around an axis. We define each airfoil's axis vector as follows:

- The axis vector of both wings and of the horizontal stabilizer is (1, 0, 0).

- The axis vector of the vertical stabilizer is (0, 1, 0).



We define each airfoil's attack vector as follows:

- The left wing's attack vector is (0, sin(leftWingInclination), -cos(leftWingInclination)).

- The right wing's attack vector is (0, sin(rightWingInclination), -cos(rightWingInclination)).

- The horizontal stabilizer's attack vector is (0, sin(horStabInclination), -cos(horStabInclination)).

- The vertical stabilizer's attack vector is (-sin(verStabInclination), 0, -cos(verStabInclination)).



We define an airfoil's normal as the cross product of its axis vector and its

attack vector.



We define an airfoil's projected airspeed vector as its airspeed vector (its

velocity minus the wind velocity) projected onto the plane perpendicular to its

axis vector. We define its projected airspeed as the size of its projected

airspeed vector. We define its angle of attack as -atan2(S . N, S . A), where S

is the projected airspeed vector, N is the normal, and A is the attack vector.



The forces operating on the drone, in drone coordinates, are the following:

- Gravity applies to each of the four point masses

- The engine thrust force vector is (0, 0, -thrust)

- Each airfoil generates a lift force N . liftSlope . AOA . s^2, where N is the

  normal, AOA is the angle of attack, and s is the projected airspeed.  No

  other forces operate on the drone; in particular, there is no drag.



The drone has a camera located at (0, 0, 0) and looking towards (0, 0, -1).

(The drone (including the engine) is transparent to this camera; it does not block any part of its view.)



The direction of gravity is (0, -1, 0) in world coordinates. The world coordinate

system is right-handed.



All physical quantities are expressed in SI units; all angles are expressed in

radians.



*/



public datatype AutopilotConfig {

    /** The world's gravitational constant (in N/kg). */

    float gravity;

    /** Distance between the drone's center of gravity and the point where the wings' mass and lift are located. */

    float wingX;

    /** Distance between the drone's center of gravity and the point where the tail mass and the lift generated by the horizontal and vertical stabilizers is located. */

    float tailSize;

    /** Mass of the engine. The engine is located in front of the drone's center of gravity. */

    float engineMass;

    /** Mass of the left wing. Equals the mass of the right wing. Modeled as being located in a single point. */

    float wingMass;

    /** Mass of the tail. Modeled as being located in a single point. */

    float tailMass;

    /** Maximum forward engine thrust. (Minimum thrust is zero.) */

    float maxThrust;

    /** Maximum magnitude of the angle of attack of all four airfoils. If during a simulation an airfoil's angle of attack exceeds this value, the simulator may report an error and abort the simulation. */

    float maxAOA;

    /** The liftSlope value for computing the lift generated by a wing. */

    float wingLiftSlope;

    /** The liftSlope value for the horizontal stabilizer. */

    float horStabLiftSlope;

    /** The liftSlope value for the vertical stabilizer. */

    float verStabLiftSlope;

    /** The horizontal angle of view of the camera. */

    float horizontalAngleOfView;

    /** The vertical angle of view of the camera. */

    float verticalAngleOfView;

    /** The number of columns of pixels in the camera image. */

    int nbColumns;

    /** The number of rows of pixels in the camera image. */

    int nbRows;

}



public datatype AutopilotInputs {

    /** The camera image, top row first. Within a row, leftmost pixel first.

    Three bytes (R, G, B) per pixel. */

    byte[] image;

    /** X coordinate of the drone's center of gravity in world coordinates. */

    float x;

    /** Y coordinate of the drone's center of gravity in world coordinates. */

    float y;

    /** Z coordinate of the drone's center of gravity in world coordinates. */

    float z;

    /** atan2(H . (-1, 0, 0), H . (0, 0, -1)), where H is the drone's heading vector (which we define as the drone's forward vector ((0, 0, -1) in drone coordinates) projected onto the world XZ plane. */

    float heading;

    /** atan2(F . (0, 1, 0), F . H), where F is the drone's forward vector and H is the drone's heading vector. */

    float pitch;

    /** atan2(R . (0, 1, 0), R . R0), where R is the drone's right direction ((1, 0, 0) in drone coordinates) and R0 = H x (0, 1, 0). */

    float roll;

    /** The amount of simulated time elapsed since the start of the simulation. Need not bear any relationship to real time (other than increasing). */

    float elapsedTime;

}



public datatype AutopilotOutputs {

    float thrust;

    float leftWingInclination;

    float rightWingInclination;

    float horStabInclination;

    float verStabInclination;

}