package be.kuleuven.cs.robijn.common.math;

import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.ode.*;

import be.kuleuven.cs.robijn.worldObjects.Drone;
import interfaces.*;

/**
 * A class to solve a system of 12 differential equations with position, velocity, 
 * heading, angular velocity of the heading, pitch, angular velocity of the pitch, roll and angular velocity of the roll.
 * 
 * @author Pieter Vandensande
 *
 */
public class SystemDifferentialEquations implements FirstOrderDifferentialEquations {
	
	public SystemDifferentialEquations(Drone drone, AutopilotOutputs autopilotOutputs) throws IllegalArgumentException {
		if (! isValidDrone(drone))
			throw new IllegalArgumentException();
		this.drone = drone;
		this.autopilotOutputs = autopilotOutputs;
	}
	
	private final Drone drone;
	
	public Drone getDrone() {
		return this.drone;
	}
	
	public static boolean isValidDrone(Drone drone) {
		return (drone != null);
	}
	
	private final AutopilotOutputs autopilotOutputs;
	
	public AutopilotOutputs getAutopilotOutputs() {
		return this.autopilotOutputs;
	}

	public int getDimension() {
		return 12;
	}

	public void computeDerivatives(double t, double[] y, double[] yDot) {
		Drone drone = this.getDrone();
		drone.setRelativePosition(new ArrayRealVector(new double[] {y[0], y[2], y[4]}, false));
		drone.setVelocity(new ArrayRealVector(new double[] {y[1], y[3], y[5]}, false));
		float newHeading = (float) y[6];
		if (newHeading < 0)
			newHeading += (2*Math.PI);
		if (newHeading >= 2*Math.PI)
			newHeading = 0;
		drone.setHeading(newHeading);
		drone.setHeadingAngularVelocity((float) y[7]);
		float newPitch = (float) y[8];
		if (newPitch < 0)
			newPitch += (2*Math.PI);
		if (newPitch >= 2*Math.PI)
			newPitch = 0;
		drone.setPitch(newPitch);
		drone.setPitchAngularVelocity((float) y[9]);
		float newRoll = (float) y[10];
		if (newRoll < 0)
			newRoll += (2*Math.PI);
		if (newRoll >= 2*Math.PI)
			newRoll = 0;
		drone.setRoll(newRoll);
		drone.setRollAngularVelocity((float) y[11]);
		
//		for (Tyre tyres: drone.getChildrenOfType(Tyre.class)) {
//			@SuppressWarnings("unused")
//			float d = tyres.getD(drone);
//		}
		
		RealVector acceleration = this.getDrone().getAcceleration(this.getAutopilotOutputs().getThrust(),
				this.getAutopilotOutputs().getLeftWingInclination(), this.getAutopilotOutputs().getRightWingInclination(),
				this.getAutopilotOutputs().getHorStabInclination(), this.getAutopilotOutputs().getVerStabInclination(),
				this.getAutopilotOutputs().getFrontBrakeForce(), this.getAutopilotOutputs().getLeftBrakeForce(),
				this.getAutopilotOutputs().getRightBrakeForce());
		float[] angularAccelerations = this.getDrone().getAngularAccelerations(this.getAutopilotOutputs().getLeftWingInclination(),
				this.getAutopilotOutputs().getRightWingInclination(), this.getAutopilotOutputs().getHorStabInclination(), 
				this.getAutopilotOutputs().getVerStabInclination(),
				this.getAutopilotOutputs().getFrontBrakeForce(), this.getAutopilotOutputs().getLeftBrakeForce(),
				this.getAutopilotOutputs().getRightBrakeForce());
		float headingAngularAcceleration = angularAccelerations[0];
		float pitchAngularAcceleration = angularAccelerations[1];
		float rollAngularAcceleration = angularAccelerations[2];
		
		yDot[0] = y[1];
		yDot[1] = acceleration.getEntry(0);
		yDot[2] = y[3];
		yDot[3] = acceleration.getEntry(1);
		yDot[4] = y[5];
		yDot[5] = acceleration.getEntry(2);
		yDot[6] = y[7];
		yDot[7] = headingAngularAcceleration;
		yDot[8] = y[9];
		yDot[9] = pitchAngularAcceleration;
		yDot[10] = y[11];
		yDot[11] = rollAngularAcceleration;	
	}
}
