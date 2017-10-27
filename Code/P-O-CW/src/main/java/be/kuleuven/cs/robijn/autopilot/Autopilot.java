package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.analysis.*;
import org.apache.commons.math3.analysis.solvers.*;
import org.apache.commons.math3.exception.*;
import org.apache.commons.math3.linear.*;
import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.common.image.*;
import p_en_o_cw_2017.*;

public class Autopilot extends WorldObject implements AutoPilot {
	
	public Autopilot(AutopilotConfig config) throws IllegalArgumentException {
		if (! isValidConfig(config))
			throw new IllegalArgumentException();
		Drone drone = new Drone(config, new ArrayRealVector(new double[] {0, 0, -933.0/3.6}, false));
		this.addChild(drone);
		this.config = config;
	}
	
	public AutopilotConfig getConfig() {
		return this.config;
	}
	
	public static boolean isValidConfig(AutopilotConfig config) {
		return (config != null);
	}
	
	private final AutopilotConfig config;
	
	public AutopilotOutputs getPreviousOutput() {
		return this.previousOutput;
	}
	
	public static boolean isValidPreviousOutput(AutopilotOutputs previousOutput) {
		return (previousOutput != null);
	}
	
	public void setPreviousOutput(AutopilotOutputs previousOutput) throws IllegalArgumentException {
		if (! isValidPreviousOutput(previousOutput))
			throw new IllegalArgumentException();
		this.previousOutput = previousOutput;
	}
	
	private AutopilotOutputs previousOutput = null;
	
	public float getPreviousElapsedTime() {
		return previousElapsedTime;
	}
	
	public static boolean isValidPreviousElapsedTime(float previousElapsedTime) {
		return ((previousElapsedTime >= 0) & (previousElapsedTime <= Float.MAX_VALUE));
	}
	
	private float previousElapsedTime = 0;
	
	public void setPreviousElapsedTime(float previousElapsedTime) throws IllegalArgumentException {
		if (! isValidPreviousElapsedTime(previousElapsedTime))
			throw new IllegalArgumentException();
		this.previousElapsedTime = previousElapsedTime;
	}
	
	/**
	 * Wel roll ten gevolge van verschillende snelheid van vleugels (door de rotaties).
	 */
	public AutopilotOutputs update(AutopilotInputs inputs) {
		Drone drone = this.getFirstChildOfType(Drone.class);
		if (this.getPreviousOutput() != null)
			this.moveDrone(inputs.getElapsedTime()-this.getPreviousElapsedTime(), this.getPreviousOutput());
        this.setPreviousElapsedTime(inputs.getElapsedTime());
        
		ImageRecognizer imagerecognizer = new ImageRecognizer();
		Image image = imagerecognizer.createImage(inputs.getImage(), this.getConfig().getNbRows(), this.getConfig().getNbColumns(),
				this.getConfig().getHorizontalAngleOfView(), this.getConfig().getVerticalAngleOfView());
		float [] necessaryRotation = image.getRotationToRedCube();
		float imageYRotation = necessaryRotation[0];
		float imageXRotation = necessaryRotation[1];
		
		float horStabInclinationTemp = 0;
		float verStabInclinationTemp = 0;
		float leftWingInclinationTemp = 0;
		float rightWingInclinationTemp = 0;
		float thrustTemp = drone.getMaxThrust();
		float minDegrees = 1;
		float bestInclination = 0.86f;
		float maxInclination = this.preventCrash(drone.getMaxAOA(), drone);
		if (bestInclination > maxInclination) {
			bestInclination = maxInclination;
		}
		if (imageYRotation > minDegrees)
			verStabInclinationTemp =  -bestInclination;
		else if (imageYRotation < -minDegrees)
			verStabInclinationTemp = bestInclination;
		if (imageXRotation > minDegrees)
			horStabInclinationTemp = -bestInclination;
		else if (imageXRotation < -minDegrees)
			horStabInclinationTemp = bestInclination;
		final float horStabInclination = horStabInclinationTemp;
		final float verStabInclination = verStabInclinationTemp;
		
		double projectedVelocityLeftWing = drone.getProjectedVelocityLeftWing().getNorm();
		double projectedVelocityRightWing = drone.getProjectedVelocityRightWing().getNorm();
		
		UnivariateFunction function = (x)->{return Math.cos(x)*x - ((drone.transformationToDroneCoordinates(drone.getGravitationalForceEngine()).getEntry(1)
				+ drone.transformationToDroneCoordinates(drone.getGravitationalForceTail()).getEntry(1) 
				+ (2*drone.transformationToDroneCoordinates(drone.getGravitationalForceWing()).getEntry(1))
				+ drone.transformationToDroneCoordinates(drone.getLiftForceHorStab(horStabInclination)).getEntry(1)
				+ drone.transformationToDroneCoordinates(drone.getLiftForceVerStab(verStabInclination)).getEntry(1))
				/(drone.getWingLiftSlope()*(Math.pow(projectedVelocityLeftWing, 2)
						+Math.pow(projectedVelocityRightWing, 2))));};
		double relativeAccuracy = 1.0e-12;
		double absoluteAccuracy = 1.0e-8;
		int maxOrder = 5;
		UnivariateSolver solver = new BracketingNthOrderBrentSolver(relativeAccuracy, absoluteAccuracy, maxOrder);
		try {
			double solution = solver.solve(100, function, (1.0/360.0)*2*Math.PI, (49.0/360.0)*2*Math.PI);
			rightWingInclinationTemp = (float) solution;
			leftWingInclinationTemp = (float) solution;
		} catch (NoBracketingException exc) {
			leftWingInclinationTemp = bestInclination;
			rightWingInclinationTemp = bestInclination;
		}
		if ((drone.getRoll()*(360/(2*Math.PI))) > minDegrees) {
			rightWingInclinationTemp -= (1.0/360.0)*2*Math.PI;
		}
		if ((drone.getRoll()*(360/(2*Math.PI))) < -minDegrees) {
			leftWingInclinationTemp -= (1.0/360.0)*2*Math.PI;
		}
		if (drone.getVelocity().getNorm() > (1000.0/3.6))
			thrustTemp = 0;
		final float thrust = thrustTemp;
		final float leftWingInclination = leftWingInclinationTemp;
		final float rightWingInclination = rightWingInclinationTemp;
		
		AutopilotOutputs output = new AutopilotOutputs() {
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
        
        this.setPreviousOutput(output);
        return output;
	}
	
	@SuppressWarnings("unused")
	public float preventCrash(float inclination, Drone drone) {
		float newInclination = inclination;
		boolean crash = true;
		while (crash == true) {
			crash = false;
			try {
				float AOALeftWing = drone.calculateAOA(drone.getNormalHor(newInclination),
						drone.getProjectedVelocityLeftWing(), drone.getAttackVectorHor(newInclination));
			} catch (IllegalArgumentException exc) {
				crash = true;
			}
			if (crash == false) {
				try {
					float AOARightWing = drone.calculateAOA(drone.getNormalHor(newInclination),
							drone.getProjectedVelocityRightWing(), drone.getAttackVectorHor(newInclination));
				} catch (IllegalArgumentException exc) {
					crash = true;
				}
			}
			if (crash == false) {
				try {
					float AOAHorStab = drone.calculateAOA(drone.getNormalHor(newInclination),
							drone.getProjectedVelocityHorStab(), drone.getAttackVectorHor(newInclination));
				} catch (IllegalArgumentException exc) {
					crash = true;
				}
			}
			if (crash == false) {
				try {
					float AOAVerStab = drone.calculateAOA(drone.getNormalVer(newInclination),
							drone.getProjectedVelocityVerStab(), drone.getAttackVectorVer(newInclination));
				} catch (IllegalArgumentException exc) {
					crash = true;
				}
			}
			if (crash == true)
				newInclination -= (1.0/360.0)*2*Math.PI;
		}
		return newInclination;
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
