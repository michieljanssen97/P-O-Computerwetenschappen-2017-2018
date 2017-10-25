package be.kuleuven.cs.robijn.testbed;

import org.apache.commons.math3.linear.*;
import be.kuleuven.cs.robijn.common.*;
import p_en_o_cw_2017.*;

public class VirtualTestbed extends WorldObject {
	
	public VirtualTestbed() {
	}
	
	/**
	 * Method to move the drone of this virtual testbed.
	 * The position and the velocity of the center of mass, the heading, the pitch, the roll
	 * and the angular velocities get updated
	 * using the outputs from the autopilot (thrust, leftWingInclination,
	 * rightWingInclination, horStabInclination, verStabInclination).
	 * 
	 * @param  dt
	 * 		   Time duration (in seconds) to move this drone.
	 * @throws IllegalArgumentException
	 * 		   The given time duration is negative.
	 * 		 | dt < 0
	 * @throws IllegalStateException
	 *         This virtual testbed has no drone.
	 *         drone == null
	 */
	public void moveDrone(float dt, AutopilotOutputs inputs) throws IllegalArgumentException, IllegalStateException {
		if (dt < 0)
			throw new IllegalArgumentException();
		Drone drone = this.getFirstChildOfType(Drone.class);
		if (drone == null)
			throw new IllegalStateException("this virtual testbed has no drone");
		RealVector position = drone.getWorldPosition();
		RealVector velocity = drone.getVelocity();
		RealVector acceleration = drone.getAcceleration(inputs.getThrust(),
				inputs.getLeftWingInclination(), inputs.getRightWingInclination(), inputs.getRightWingInclination(), inputs.getVerStabInclination());
		
		drone.setRelativePosition(position.add(velocity.mapMultiply(dt)).add(acceleration.mapMultiply(Math.pow(dt, 2)/2)));
		drone.setVelocity(velocity.add(acceleration.mapMultiply(dt)));
		
		float[] angularAccelerations = drone.getAngularAccelerations(inputs.getLeftWingInclination(),
				inputs.getRightWingInclination(), inputs.getRightWingInclination(), inputs.getVerStabInclination(), inputs.getThrust());
		float heading = drone.getHeading();
		float headingAngularVelocity = drone.getHeadingAngularVelocity();
		float headingAngularAcceleration = angularAccelerations[0];
		float pitch = drone.getPitch();
		float pitchAngularVelocity = drone.getPitchAngularVelocity();
		float pitchAngularAcceleration = angularAccelerations[1];
		float roll = drone.getRoll();
		float rollAngularVelocity = drone.getRollAngularVelocity();
		float rollAngularAcceleration = angularAccelerations[2];
		
		drone.setHeading((float) ((heading + headingAngularVelocity*dt + headingAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI)));
		drone.setPitch((float) ((pitch + pitchAngularVelocity*dt + pitchAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI)));
		drone.setRoll((float) ((roll + rollAngularVelocity*dt + rollAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI)));
		
		drone.setHeadingAngularVelocity(headingAngularVelocity + headingAngularAcceleration*dt);
		drone.setPitchAngularVelocity(pitchAngularVelocity + pitchAngularAcceleration*dt);
		drone.setRollAngularVelocity(rollAngularVelocity + rollAngularAcceleration*dt);
	}
}