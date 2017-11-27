package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.analysis.*;
import org.apache.commons.math3.analysis.solvers.*;
import org.apache.commons.math3.exception.*;
import org.apache.commons.math3.linear.*;
import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.autopilot.image.*;
import be.kuleuven.cs.robijn.common.math.VectorMath;
import p_en_o_cw_2017.*;

public class Autopilot extends WorldObject implements AutoPilot {
	
	public Autopilot(AutopilotConfig config) throws IllegalArgumentException {
		if (! isValidConfig(config))
			throw new IllegalArgumentException();
		Drone drone = new Drone(config, new ArrayRealVector(new double[] {0, 0, -6.667}, false));
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
		if (this.getPreviousOutput() != null)
			this.moveDrone(inputs.getElapsedTime()-this.getPreviousElapsedTime(), this.getPreviousOutput());
        this.setPreviousElapsedTime(inputs.getElapsedTime());  
        
        Drone drone = this.getFirstChildOfType(Drone.class);
        
        ImageRecognizer imagerecognizer = new ImageRecognizer();
        Image image = imagerecognizer.createImage(inputs.getImage(), this.getConfig().getNbRows(), this.getConfig().getNbColumns(),
        		this.getConfig().getHorizontalAngleOfView(), this.getConfig().getVerticalAngleOfView(), drone.getWorldPosition(), drone.getWorldRotation());
		float[] necessaryRotation;
        try {
			necessaryRotation = imagerecognizer.getNecessaryRotation(image, 0.0f, 1.0f);
		}
		catch (IllegalStateException ex){
        	//No red cube found, just fly forward
			necessaryRotation = new float[2];
		}
        float imageYRotation = necessaryRotation[0];
		float imageXRotation = necessaryRotation[1];
		
		float horStabInclinationTemp = 0;
		float verStabInclinationTemp = 0;
		float leftWingInclinationTemp = 0;
		float rightWingInclinationTemp = 0;
		float minDegrees = 1;
		float bestInclinationPositive;
		if (! hasCrash(0.86f, drone))
			bestInclinationPositive = 0.86f;
		else {
			bestInclinationPositive = this.preventCrash(drone.getMaxAOA(), true, drone);
		}
		float bestInclinationNegative;
		if (! hasCrash(-0.86f, drone))
			bestInclinationNegative = -0.86f;
		else {
			bestInclinationNegative = this.preventCrash(-drone.getMaxAOA(), false, drone);
		}
		double headingAngularAcceleration = (1.0/360.0)*6*Math.PI;
		double maxHeadingAngularVelocity = (1.0/360.0)*12*Math.PI;
		double pitchAngularAcceleration = (1.0/360.0)*48*Math.PI;
		
		double relativeAccuracy = 1.0e-12;
		double absoluteAccuracy = 1.0e-8;
		int maxOrder = 5;
		UnivariateSolver solver = new BracketingNthOrderBrentSolver(relativeAccuracy, absoluteAccuracy, maxOrder);
		
//		if (imageYRotation > minDegrees)
//			verStabInclinationTemp =  -bestInclination;
//		else if (imageYRotation < -minDegrees)
//			verStabInclinationTemp = bestInclination;
//		if (imageXRotation > minDegrees)
//			horStabInclinationTemp = -bestInclination;
//		else if (imageXRotation < -minDegrees)
//			horStabInclinationTemp = bestInclination;
		float inertiaMatrixXX = (float) (drone.getTailMass()*Math.pow(drone.getTailSize(),2) +drone.getEngineMass()*Math.pow(drone.getEngineDistance(), 2));
		float inertiaMatrixZZ = (float) (2*(drone.getWingMass()*Math.pow(drone.getWingX(),2)));
		float inertiaMatrixYY = inertiaMatrixXX + inertiaMatrixZZ;
		double projectedVelocityHorStab = drone.getProjectedVelocityHorStab().getNorm();
		double projectedVelocityVerStab = drone.getProjectedVelocityVerStab().getNorm();
		RealVector totalAngularVelocityDroneCoordinates = drone.transformationToDroneCoordinates(drone.getHeadingAngularVelocityVector()
					.add(drone.getPitchAngularVelocityVector())
					.add(drone.getRollAngularVelocityVector()));
		RealMatrix inertiaMatrix = new Array2DRowRealMatrix(new double[][] {
			{inertiaMatrixXX, 0,                0},
			{0,               inertiaMatrixYY,  0}, 
			{0,               0,                inertiaMatrixZZ}
			}, false);
		RealVector angularMomentumDroneCoordinates = inertiaMatrix.operate(totalAngularVelocityDroneCoordinates);
		
		if ((imageXRotation > minDegrees) && (imageYRotation > minDegrees) && (drone.getHeadingAngularVelocity() < maxHeadingAngularVelocity)) {
			try {
				UnivariateFunction function1 = (x)->{return Math.cos(x)*x + ((inertiaMatrixXX*Math.cos(drone.getRoll())*pitchAngularAcceleration
						+ inertiaMatrixXX*Math.cos(drone.getPitch())*Math.sin(drone.getRoll())*headingAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(0)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(0))
						/(drone.getHorStabLiftSlope()*Math.pow(projectedVelocityHorStab, 2)));};
				double solution1 = solver.solve(100, function1, bestInclinationNegative, 0);
				horStabInclinationTemp = (float) solution1;
			} catch (NoBracketingException exc1) {
				horStabInclinationTemp = bestInclinationNegative;
			} catch (NumberIsTooLargeException exc2) {
				horStabInclinationTemp = bestInclinationNegative;
			}
			try {
				UnivariateFunction function2 = (x)->{return Math.cos(x)*x + ((inertiaMatrixYY*Math.cos(drone.getPitch())*Math.cos(drone.getRoll())
							*headingAngularAcceleration
						- inertiaMatrixYY*Math.sin(drone.getRoll())*pitchAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(1)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(1))
						/(drone.getVerStabLiftSlope()*Math.pow(projectedVelocityVerStab, 2)));};
				double solution2 = solver.solve(100, function2, bestInclinationNegative, 0);
				verStabInclinationTemp = (float) solution2;
			} catch (NoBracketingException exc1) {
				verStabInclinationTemp = bestInclinationNegative;
			} catch (NumberIsTooLargeException exc2) {
				verStabInclinationTemp = bestInclinationNegative;
			}
		}
		else if ((imageXRotation > minDegrees) && (imageYRotation < -minDegrees) && (drone.getHeadingAngularVelocity() > -maxHeadingAngularVelocity)) {
			try {
				UnivariateFunction function1 = (x)->{return Math.cos(x)*x + ((inertiaMatrixXX*Math.cos(drone.getRoll())*pitchAngularAcceleration
						- inertiaMatrixXX*Math.cos(drone.getPitch())*Math.sin(drone.getRoll())*headingAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(0)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(0))
						/(drone.getHorStabLiftSlope()*Math.pow(projectedVelocityHorStab, 2)));};
				double solution1 = solver.solve(100, function1, bestInclinationNegative, 0);
				horStabInclinationTemp = (float) solution1;
			} catch (NoBracketingException exc1) {
				horStabInclinationTemp = bestInclinationNegative;
			} catch (NumberIsTooLargeException exc2) {
				horStabInclinationTemp = bestInclinationNegative;
			}
			try {
				UnivariateFunction function2 = (x)->{return Math.cos(x)*x + ((-inertiaMatrixYY*Math.cos(drone.getPitch())*Math.cos(drone.getRoll())
							*headingAngularAcceleration
						- inertiaMatrixYY*Math.sin(drone.getRoll())*pitchAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(1)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(1))
						/(drone.getVerStabLiftSlope()*Math.pow(projectedVelocityVerStab, 2)));};
				double solution2 = solver.solve(100, function2, 0, bestInclinationPositive);
				verStabInclinationTemp = (float) solution2;
			} catch (NoBracketingException exc1) {
				verStabInclinationTemp = bestInclinationPositive;
			} catch (NumberIsTooLargeException exc2) {
				verStabInclinationTemp = bestInclinationPositive;
			}
		}
		else if ((imageXRotation < -minDegrees) && (imageYRotation > minDegrees) && (drone.getHeadingAngularVelocity() < maxHeadingAngularVelocity)) {
			try {
				UnivariateFunction function1 = (x)->{return Math.cos(x)*x + ((-inertiaMatrixXX*Math.cos(drone.getRoll())*pitchAngularAcceleration
						+ inertiaMatrixXX*Math.cos(drone.getPitch())*Math.sin(drone.getRoll())*headingAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(0)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(0))
						/(drone.getHorStabLiftSlope()*Math.pow(projectedVelocityHorStab, 2)));};
				double solution1 = solver.solve(100, function1, 0, bestInclinationPositive);
				horStabInclinationTemp = (float) solution1;
			} catch (NoBracketingException exc1) {
				horStabInclinationTemp = bestInclinationPositive;
			} catch (NumberIsTooLargeException exc2) {
				horStabInclinationTemp = bestInclinationPositive;
			}
			try {
				UnivariateFunction function2 = (x)->{return Math.cos(x)*x + ((inertiaMatrixYY*Math.cos(drone.getPitch())*Math.cos(drone.getRoll())
							*headingAngularAcceleration
						+ inertiaMatrixYY*Math.sin(drone.getRoll())*pitchAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(1)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(1))
						/(drone.getVerStabLiftSlope()*Math.pow(projectedVelocityVerStab, 2)));};
				double solution2 = solver.solve(100, function2, bestInclinationNegative, 0);
				verStabInclinationTemp = (float) solution2;
			} catch (NoBracketingException exc1) {
				verStabInclinationTemp = bestInclinationNegative;
			} catch (NumberIsTooLargeException exc2) {
				verStabInclinationTemp = bestInclinationNegative;
			}
		}
		else if ((imageXRotation < -minDegrees) && (imageYRotation < -minDegrees) && (drone.getHeadingAngularVelocity() > -maxHeadingAngularVelocity)) {
			try {
				UnivariateFunction function1 = (x)->{return Math.cos(x)*x + ((-inertiaMatrixXX*Math.cos(drone.getRoll())*pitchAngularAcceleration
						- inertiaMatrixXX*Math.cos(drone.getPitch())*Math.sin(drone.getRoll())*headingAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(0)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(0))
						/(drone.getHorStabLiftSlope()*Math.pow(projectedVelocityHorStab, 2)));};
				double solution1 = solver.solve(100, function1, 0, bestInclinationPositive);
				horStabInclinationTemp = (float) solution1;
			} catch (NoBracketingException exc1) {
				horStabInclinationTemp = bestInclinationPositive;
			} catch (NumberIsTooLargeException exc2) {
				horStabInclinationTemp = bestInclinationPositive;
			}
			try {
				UnivariateFunction function2 = (x)->{return Math.cos(x)*x + ((-inertiaMatrixYY*Math.cos(drone.getPitch())*Math.cos(drone.getRoll())
							*headingAngularAcceleration
						+ inertiaMatrixYY*Math.sin(drone.getRoll())*pitchAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(1)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(1))
						/(drone.getVerStabLiftSlope()*Math.pow(projectedVelocityVerStab, 2)));};
				double solution2 = solver.solve(100, function2, 0, bestInclinationPositive);
				verStabInclinationTemp = (float) solution2;
			} catch (NoBracketingException exc1) {
				verStabInclinationTemp = bestInclinationPositive;
			} catch (NumberIsTooLargeException exc2) {
				verStabInclinationTemp = bestInclinationPositive;
			}
		}
		else if (imageXRotation > minDegrees) {
			try {
				UnivariateFunction function1 = (x)->{return Math.cos(x)*x + ((inertiaMatrixXX*Math.cos(drone.getRoll())*pitchAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(0)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(0))
						/(drone.getHorStabLiftSlope()*Math.pow(projectedVelocityHorStab, 2)));};
				double solution1 = solver.solve(100, function1, bestInclinationNegative, 0);
				horStabInclinationTemp = (float) solution1;
			} catch (NoBracketingException exc1) {
				horStabInclinationTemp = bestInclinationNegative;
			} catch (NumberIsTooLargeException exc2) {
				horStabInclinationTemp = bestInclinationNegative;
			}
		}
		else if (imageXRotation < -minDegrees) {
			try {
				UnivariateFunction function1 = (x)->{return Math.cos(x)*x + ((-inertiaMatrixXX*Math.cos(drone.getRoll())*pitchAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(0)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(0))
						/(drone.getHorStabLiftSlope()*Math.pow(projectedVelocityHorStab, 2)));};
				double solution1 = solver.solve(100, function1, 0, bestInclinationPositive);
				horStabInclinationTemp = (float) solution1;
			} catch (NoBracketingException exc1) {
				horStabInclinationTemp = bestInclinationPositive;
			} catch (NumberIsTooLargeException exc2) {
				horStabInclinationTemp = bestInclinationPositive;
			}
		}
		else if ((imageYRotation > minDegrees) && (drone.getHeadingAngularVelocity() < maxHeadingAngularVelocity)) {
			try {
				UnivariateFunction function2 = (x)->{return Math.cos(x)*x + ((inertiaMatrixYY*Math.cos(drone.getPitch())*Math.cos(drone.getRoll())
							*headingAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(1)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(1))
						/(drone.getVerStabLiftSlope()*Math.pow(projectedVelocityVerStab, 2)));};
				double solution2 = solver.solve(100, function2, bestInclinationNegative, 0);
				verStabInclinationTemp = (float) solution2;
			} catch (NoBracketingException exc1) {
				verStabInclinationTemp = bestInclinationNegative;
			} catch (NumberIsTooLargeException exc2) {
				verStabInclinationTemp = bestInclinationNegative;
			}
		}
		else if ((imageYRotation < -minDegrees) && (drone.getHeadingAngularVelocity() > -maxHeadingAngularVelocity)) {
			try {
				UnivariateFunction function2 = (x)->{return Math.cos(x)*x + ((-inertiaMatrixYY*Math.cos(drone.getPitch())*Math.cos(drone.getRoll())
							*headingAngularAcceleration
						+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(1)
						+ inertiaMatrix.operate(
								drone.transformationToDroneCoordinates(
										VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
										.add(VectorMath.crossProduct(
											drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
											drone.getRollAngularVelocityVector()
										))
									)).getEntry(1))
						/(drone.getVerStabLiftSlope()*Math.pow(projectedVelocityVerStab, 2)));};
				double solution2 = solver.solve(100, function2, 0, bestInclinationPositive);
				verStabInclinationTemp = (float) solution2;
			} catch (NoBracketingException exc1) {
				verStabInclinationTemp = bestInclinationPositive;
			} catch (NumberIsTooLargeException exc2) {
				verStabInclinationTemp = bestInclinationPositive;
			}
		}
		
		final float horStabInclination = horStabInclinationTemp;
		final float verStabInclination = verStabInclinationTemp;
		
		double projectedVelocityLeftWing = drone.getProjectedVelocityLeftWing().getNorm();
		double projectedVelocityRightWing = drone.getProjectedVelocityRightWing().getNorm();
		
		UnivariateFunction function3 = (x)->{return Math.cos(x)*x + ((drone.transformationToDroneCoordinates(drone.getGravitationalForceEngine()).getEntry(1)
				+ drone.transformationToDroneCoordinates(drone.getGravitationalForceTail()).getEntry(1) 
				+ (2*drone.transformationToDroneCoordinates(drone.getGravitationalForceWing()).getEntry(1))
				+ drone.transformationToDroneCoordinates(drone.getLiftForceHorStab(horStabInclination)).getEntry(1)
				+ drone.transformationToDroneCoordinates(drone.getLiftForceVerStab(verStabInclination)).getEntry(1))
				/(drone.getWingLiftSlope()*(Math.pow(projectedVelocityLeftWing, 2)
						+Math.pow(projectedVelocityRightWing, 2))));};
		try {
			double solution3 = solver.solve(100, function3, 0, bestInclinationPositive);
			rightWingInclinationTemp = (float) solution3;
			leftWingInclinationTemp = (float) solution3;
		} catch (NoBracketingException exc1) {
			leftWingInclinationTemp = bestInclinationPositive;
			rightWingInclinationTemp = bestInclinationPositive;
		} catch (NumberIsTooLargeException exc2) {
			leftWingInclinationTemp = bestInclinationPositive;
			rightWingInclinationTemp = bestInclinationPositive;
		}
//		if ((drone.getRoll()*(360/(2*Math.PI))) > minDegrees) {
//			rightWingInclinationTemp -= (1.0/360.0)*2*Math.PI;
//		}
//		if ((drone.getRoll()*(360/(2*Math.PI))) < -minDegrees) {
//			leftWingInclinationTemp -= (1.0/360.0)*2*Math.PI;
//		}
		final float leftWingInclination = leftWingInclinationTemp;
		final float rightWingInclination = rightWingInclinationTemp;
		
		float thrustTemp = (float) (drone.transformationToDroneCoordinates(drone.getGravitationalForceEngine()).getEntry(2)
				+ drone.transformationToDroneCoordinates(drone.getGravitationalForceTail()).getEntry(2) 
				+ (2*drone.transformationToDroneCoordinates(drone.getGravitationalForceWing()).getEntry(2))
				+ drone.transformationToDroneCoordinates(drone.getLiftForceHorStab(horStabInclination)).getEntry(2)
				+ drone.transformationToDroneCoordinates(drone.getLiftForceVerStab(verStabInclination)).getEntry(2)
				+ drone.transformationToDroneCoordinates(drone.getLiftForceLeftWing(leftWingInclination)).getEntry(2)
				+ drone.transformationToDroneCoordinates(drone.getLiftForceRightWing(rightWingInclination)).getEntry(2));
		if (thrustTemp > drone.getMaxThrust())
			thrustTemp = drone.getMaxThrust();
		final float thrust = thrustTemp;
		
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
	
	public float preventCrash(float inclination, boolean positive, Drone drone) {
		float newInclination = inclination;
		boolean crash = true;
		while (crash == true) {
			crash = this.hasCrash(newInclination, drone);
			if (crash == true) {
				if (positive == true) {
					newInclination -= (1.0/360.0)*2*Math.PI;
					if (newInclination < -drone.getMaxAOA()) {
						throw new IllegalStateException("simulation failed!");
					}
				}
				if (positive == false) {
					newInclination += (1.0/360.0)*2*Math.PI;
					if (newInclination > drone.getMaxAOA())
						throw new IllegalStateException("simulation failed!");
				}
			}
		}
		return newInclination;
	}
	
	@SuppressWarnings("unused")
	public boolean hasCrash(float inclination, Drone drone) {
		boolean crash = false;
		try {
			float AOALeftWing = drone.calculateAOA(drone.getNormalHor(inclination),
					drone.getProjectedVelocityLeftWing(), drone.getAttackVectorHor(inclination));
		} catch (IllegalArgumentException exc) {
			crash = true;
		}
		if (crash == false) {
			try {
				float AOARightWing = drone.calculateAOA(drone.getNormalHor(inclination),
						drone.getProjectedVelocityRightWing(), drone.getAttackVectorHor(inclination));
			} catch (IllegalArgumentException exc) {
				crash = true;
			}
		}
		if (crash == false) {
			try {
				float AOAHorStab = drone.calculateAOA(drone.getNormalHor(inclination),
						drone.getProjectedVelocityHorStab(), drone.getAttackVectorHor(inclination));
			} catch (IllegalArgumentException exc) {
				crash = true;
			}
		}
		if (crash == false) {
			try {
				float AOAVerStab = drone.calculateAOA(drone.getNormalVer(inclination),
						drone.getProjectedVelocityVerStab(), drone.getAttackVectorVer(inclination));
			} catch (IllegalArgumentException exc) {
				crash = true;
			}
		}
		return crash;	
	}
	
	/**
	 * Method to move the drone of this autopilot,
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
	 *         This autopilot has no drone.
	 *         drone == null
	 */
	public void moveDrone(float dt, AutopilotOutputs inputs) throws IllegalArgumentException, IllegalStateException {
		if (dt < 0)
			throw new IllegalArgumentException();
		Drone drone = this.getFirstChildOfType(Drone.class);
		if (drone == null)
			throw new IllegalStateException("this autopilot has no drone");
		RealVector position = drone.getWorldPosition();
		RealVector velocity = drone.getVelocity();
		RealVector acceleration = drone.getAcceleration(inputs.getThrust(),
				inputs.getLeftWingInclination(), inputs.getRightWingInclination(), inputs.getHorStabInclination(), inputs.getVerStabInclination());
		
		float[] angularAccelerations = drone.getAngularAccelerations(inputs.getLeftWingInclination(),
				inputs.getRightWingInclination(), inputs.getHorStabInclination(), inputs.getVerStabInclination());
		float heading = drone.getHeading();
		float headingAngularVelocity = drone.getHeadingAngularVelocity();
		float headingAngularAcceleration = angularAccelerations[0];
		float pitch = drone.getPitch();
		float pitchAngularVelocity = drone.getPitchAngularVelocity();
		float pitchAngularAcceleration = angularAccelerations[1];
		float roll = drone.getRoll();
		float rollAngularVelocity = drone.getRollAngularVelocity();
		float rollAngularAcceleration = angularAccelerations[2];
		
		drone.setRelativePosition(position.add(velocity.mapMultiply(dt)).add(acceleration.mapMultiply(Math.pow(dt, 2)/2)));
		drone.setVelocity(velocity.add(acceleration.mapMultiply(dt)));
		
		float newHeading = (float) ((heading + headingAngularVelocity*dt + headingAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI));
		if ((Math.abs(newHeading - 0) < 0.0001) || (Math.abs(newHeading - 2*Math.PI) < 0.0001))
			newHeading = 0;
		else if (newHeading < 0)
			newHeading += (2*Math.PI);
		float newPitch = (float) ((pitch + pitchAngularVelocity*dt + pitchAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI));
		if ((Math.abs(newPitch - 0) < 0.0001) || (Math.abs(newPitch - 2*Math.PI) < 0.0001))
			newPitch = 0;
		else if (newPitch < 0)
			newPitch += (2*Math.PI);
		float newRoll = (float) ((roll + rollAngularVelocity*dt + rollAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI));
		if ((Math.abs(newRoll - 0) < 0.0001) || (Math.abs(newRoll - 2*Math.PI) < 0.0001))
			newRoll = 0;
		else if (newRoll < 0)
			newRoll += (2*Math.PI);
		drone.setHeading(newHeading);
		drone.setPitch(newPitch);
		drone.setRoll(newRoll);
		
		drone.setHeadingAngularVelocity(headingAngularVelocity + headingAngularAcceleration*dt);
		drone.setPitchAngularVelocity(pitchAngularVelocity + pitchAngularAcceleration*dt);
		drone.setRollAngularVelocity(rollAngularVelocity + rollAngularAcceleration*dt);
	}
}
