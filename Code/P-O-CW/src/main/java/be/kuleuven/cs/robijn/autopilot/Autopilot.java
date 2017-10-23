package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.analysis.*;
import org.apache.commons.math3.analysis.solvers.*;
import org.apache.commons.math3.exception.*;
import org.apache.commons.math3.linear.*;
import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.testbed.*;
import p_en_o_cw_2017.*;

public class Autopilot extends WorldObject implements AutoPilot {
	
	public Autopilot() {
	}
	
	/**
	 * Wel roll ten gevolge van verschillende snelheid van vleugels (door de rotaties).
	 */
	public AutopilotOutputs update(AutopilotInputs input) {
		Drone drone = this.getFirstChildOfType(Drone.class);
		float horStabInclinationTemp = 0;
		float verStabInclination = 0;
		float leftWingInclination = 0;
		float rightWingInclination = 0;
		float thrust = 0;
		float imageYRotation = 0;
		float imageXRotation = 0;
		float minDegrees = 1;
		float bestInclination = 0.86f;
		if (bestInclination > drone.getMaxAOA())
			bestInclination = drone.getMaxAOA();
		if (imageYRotation > minDegrees)
			verStabInclination =  bestInclination;
		else if (imageYRotation < -minDegrees)
			verStabInclination = -bestInclination;
		if (imageXRotation > minDegrees)
			horStabInclinationTemp = bestInclination;
		else if (imageXRotation < -minDegrees)
			horStabInclinationTemp = -bestInclination;
		final float horStabInclination = horStabInclinationTemp;
		
		RealVector distance1 = drone.transformationToWorldCoordinates(new ArrayRealVector(new double[] {drone.getWingX(), 0, 0}, false));
		RealVector velocityWorldCoordinates1 = drone.calculateVelocityWorldCo(drone.getVelocity(), drone.getHeadingAngularVelocityVector(), 
				   drone.getPitchAngularVelocityVector(), drone.getRollAngularVelocityVector(), distance1);

		RealVector velocityDroneCoordinates1 = drone.transformationToDroneCoordinates(velocityWorldCoordinates1);
		velocityDroneCoordinates1.setEntry(0, 0);
		double projectedVelocityLeftWing = drone.transformationToWorldCoordinates(velocityDroneCoordinates1).getNorm();
		
		RealVector distance2 = drone.transformationToWorldCoordinates(new ArrayRealVector(new double[] {drone.getWingX(), 0, 0}, false));
		RealVector velocityWorldCoordinates2 = drone.calculateVelocityWorldCo(drone.getVelocity(), drone.getHeadingAngularVelocityVector(), 
				   drone.getPitchAngularVelocityVector(), drone.getRollAngularVelocityVector(), distance2);

		RealVector velocityDroneCoordinates2 = drone.transformationToDroneCoordinates(velocityWorldCoordinates2);
		velocityDroneCoordinates1.setEntry(0, 0);
		double projectedVelocityRightWing = drone.transformationToWorldCoordinates(velocityDroneCoordinates2).getNorm();
		
		UnivariateFunction function = (x)->{return Math.cos(x)*x - ((drone.getGravitationalForceEngine().getNorm()
				+ drone.getGravitationalForceTail().getNorm() + (2*drone.getGravitationalForceWing().getNorm())
				+ drone.getLiftForceHorStab(horStabInclination).getEntry(1))/(drone.getWingLiftSlope()*(Math.pow(projectedVelocityLeftWing, 2)
						+Math.pow(projectedVelocityRightWing, 2))));};
		double relativeAccuracy = 1.0e-12;
		double absoluteAccuracy = 1.0e-8;
		int maxOrder = 5;
		UnivariateSolver solver = new BracketingNthOrderBrentSolver(relativeAccuracy, absoluteAccuracy, maxOrder);
		try {
			double solution = solver.solve(100, function, 0.0, Math.PI/2);
			rightWingInclination = (float) solution;
			leftWingInclination = (float) solution;
		} catch (TooManyEvaluationsException exc) {
			leftWingInclination = bestInclination;
			rightWingInclination = bestInclination;
		}
		if (drone.getVelocity().getNorm() > )
			thrust = 0;
		return new AutopilotOutputs() {
			public float getThrust() {
				return thrust;
			}
			public float getLeftWingInclination() {
				return leftWingInclination;
			}
			public float getRightWingInclination() {
				return rightWingInclination;
			}
			public float getHorStabInclination() {
				return horStabInclination;
			}
			public float getVerStabInclination() {
				return verStabInclination;
			}
        };
	}
	
	/**
	 * Method to move the drone of this virtual testbed,
	 * the position, velocity and acceleration get updated,
	 * using the outputs from the autopilot (thrust, leftWingInclination,
	 * rightWingInclination, horStabInclination, verStabInclination)
	 * 
	 * @param  dt
	 * 		   Time duration (in seconds) to move the drone.
	 * @throws IllegalArgumentException
	 * 		   The given time duration is negative.
	 * 		   dt < 0
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
		
		drone.setHeading((float)(heading + headingAngularVelocity*dt + headingAngularAcceleration*(Math.pow(dt, 2)/2)));
		drone.setPitch((float)(pitch + pitchAngularVelocity*dt + pitchAngularAcceleration*(Math.pow(dt, 2)/2)));
		drone.setRoll((float)(roll + rollAngularVelocity*dt + rollAngularAcceleration*(Math.pow(dt, 2)/2)));
		
		drone.setHeadingAngularVelocity(headingAngularVelocity + headingAngularAcceleration*dt);
		drone.setPitchAngularVelocity(pitchAngularVelocity + pitchAngularAcceleration*dt);
		drone.setRollAngularVelocity(rollAngularVelocity + rollAngularAcceleration*dt);
	}

}
