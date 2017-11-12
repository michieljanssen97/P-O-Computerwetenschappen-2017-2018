package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.analysis.*;
import org.apache.commons.math3.analysis.solvers.*;
import org.apache.commons.math3.exception.*;
import org.apache.commons.math3.linear.*;

import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.autopilot.image.*;
import be.kuleuven.cs.robijn.common.math.VectorMath;
import p_en_o_cw_2017.*;

/**
 * A class of autopilots.
 * 
 * @author Pieter Vandensande
 */
public class Autopilot extends WorldObject implements AutoPilot {
	
	public Autopilot(AutopilotConfig config, RealVector initialVelocity) throws IllegalArgumentException {
		if (! isValidConfig(config))
			throw new IllegalArgumentException();
		Drone drone = new Drone(config, initialVelocity);
		this.addChild(drone);
		this.config = config;
		RealVector previousPosition = drone.getWorldPosition();
		if (!isValidPreviousPosition(previousPosition))
			throw new IllegalArgumentException();
		this.previousPosition = previousPosition;
		float previousHeading = drone.getHeading();
		if (! isValidPreviousHeading(previousHeading))
			throw new IllegalArgumentException();
		this.previousHeading = previousHeading;
		float previousPitch = drone.getPitch();
		if (! isValidPreviousPitch(previousPitch))
			throw new IllegalArgumentException();
		this.previousPitch = previousPitch;
		float previousRoll = drone.getRoll();
		if (! isValidPreviousRoll(previousRoll))
			throw new IllegalArgumentException();
		this.previousRoll = previousRoll;
	}
	
	public AutopilotConfig getConfig() {
		return this.config;
	}
	
	public static boolean isValidConfig(AutopilotConfig config) {
		return (config != null);
	}
	
	private final AutopilotConfig config;
	
	public boolean isFirstUpdate() {
		return this.firstUpdate;
	}
	
	private boolean firstUpdate = true;
	
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
	public AutopilotOutputs update(AutopilotInputs inputs) throws IllegalStateException {
		if (this.isFirstUpdate())
			this.firstUpdate = false;
		else {
			this.moveDrone(inputs.getElapsedTime() - this.getPreviousElapsedTime(), inputs);
		}
        this.setPreviousElapsedTime(inputs.getElapsedTime());  
        
        Drone drone = this.getFirstChildOfType(Drone.class);
        
        ImageRecognizer imagerecognizer = new ImageRecognizer();
        Image image = imagerecognizer.createImage(inputs.getImage(), this.getConfig().getNbRows(), this.getConfig().getNbColumns(),
        		this.getConfig().getHorizontalAngleOfView(), this.getConfig().getVerticalAngleOfView());
		float[] necessaryRotation;
        try {
			necessaryRotation = image.getRotationToRedCube();
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
		
		double relativeAccuracy = 1.0e-12;
		double absoluteAccuracy = 1.0e-8;
		int maxOrder = 5;
		UnivariateSolver solver = new BracketingNthOrderBrentSolver(relativeAccuracy, absoluteAccuracy, maxOrder);
		
		//Case 1
//		float maxInclinationWing = this.minMaxInclination((float)(Math.PI/2), (float)0.0, true, (float)((1.0/360.0)*2*Math.PI), drone, 1);
//		float minInclinationWing = this.minMaxInclination((float)(Math.PI/2), (float)0.0, false, (float)((1.0/360.0)*2*Math.PI), drone, 1);
//		
//		final float horStabInclination = horStabInclinationTemp;
//		final float verStabInclination = verStabInclinationTemp;
//		
//		UnivariateFunction function = (x)->{return drone.getLiftForceLeftWing((float)x).getEntry(1)
//				+ drone.getLiftForceRightWing((float)x).getEntry(1)
//				+ drone.getGravitationalForceEngine().getEntry(1)
//				+ drone.getGravitationalForceTail().getEntry(1) 
//				+ (2*drone.getGravitationalForceWing().getEntry(1))
//				;};
//		try {
//			double solution = solver.solve(100, function, minInclinationWing, maxInclinationWing);
//			rightWingInclinationTemp = (float) solution;
//			leftWingInclinationTemp = (float) solution;
//		} catch (NoBracketingException exc) {
//			throw new IllegalStateException("simulation failed!");
//		}
//		
//		final float leftWingInclination = leftWingInclinationTemp;
//		final float rightWingInclination = rightWingInclinationTemp;
//		
//		float thrustTemp = (float) (drone.transformationToDroneCoordinates(drone.getLiftForceLeftWing(leftWingInclination)).getEntry(2)
//				+ drone.transformationToDroneCoordinates(drone.getLiftForceRightWing(rightWingInclination)).getEntry(2));
//		if (thrustTemp > drone.getMaxThrust())
//			thrustTemp = drone.getMaxThrust();
//		final float thrust = thrustTemp;
		
		//Case 2
//		float yAcceleration = 1.0f;
//		float maxYVelocity = 1.0f;
//		
//		float maxInclinationWing = this.minMaxInclination((float)(Math.PI/2), (float)0.0, true, (float)((1.0/360.0)*2*Math.PI), drone, 1);
//		float minInclinationWing = this.minMaxInclination((float)(Math.PI/2), (float)0.0, false, (float)((1.0/360.0)*2*Math.PI), drone, 1);
//		float maxInclinationHorStab = this.minMaxInclination((float)(Math.PI/2), (float)0.0, true, (float)((1.0/360.0)*2*Math.PI), drone, 2);
//		float minInclinationHorStab = this.minMaxInclination((float)(Math.PI/2), (float)0.0, false, (float)((1.0/360.0)*2*Math.PI), drone, 2);
//		
//		UnivariateFunction function1;
//		float yVelocity = (float)drone.getVelocity().getEntry(1);
//		if (((imageXRotation > minDegrees) && (yVelocity < maxYVelocity)) || (yVelocity < -maxYVelocity)) {
//			function1 = (x)->{return drone.getLiftForceLeftWing((float)x).getEntry(1)
//					+ drone.getLiftForceRightWing((float)x).getEntry(1)
//					+ drone.getGravitationalForceEngine().getEntry(1)
//					+ drone.getGravitationalForceTail().getEntry(1) 
//					+ (2*drone.getGravitationalForceWing().getEntry(1))
//					- ((drone.getEngineMass() + drone.getTailMass() + (2 * drone.getWingMass())) * yAcceleration)
//					;};
//		}
//		else if (((imageXRotation < -minDegrees) && (yVelocity > -maxYVelocity)) || (yVelocity > maxYVelocity)) {	
//			function1 = (x)->{return drone.getLiftForceLeftWing((float)x).getEntry(1)
//					+ drone.getLiftForceRightWing((float)x).getEntry(1)
//					+ drone.getGravitationalForceEngine().getEntry(1)
//					+ drone.getGravitationalForceTail().getEntry(1) 
//					+ (2*drone.getGravitationalForceWing().getEntry(1))
//					+ ((drone.getEngineMass() + drone.getTailMass() + (2 * drone.getWingMass())) * yAcceleration)
//					;};
//		}
//		else {
//			function1 = (x)->{return drone.getLiftForceLeftWing((float)x).getEntry(1)
//					+ drone.getLiftForceRightWing((float)x).getEntry(1)
//					+ drone.getGravitationalForceEngine().getEntry(1)
//					+ drone.getGravitationalForceTail().getEntry(1) 
//					+ (2*drone.getGravitationalForceWing().getEntry(1))
//					;};
//		}
//		try {
//			double solution1 = solver.solve(100, function1, minInclinationWing, maxInclinationWing);
//			rightWingInclinationTemp = (float) solution1;
//			leftWingInclinationTemp = (float) solution1;
//		} catch (NoBracketingException exc) {
//			throw new IllegalStateException("simulation failed!");
//		}
//		
//		final float leftWingInclination = leftWingInclinationTemp;
//		final float rightWingInclination = rightWingInclinationTemp;
//		
//		UnivariateFunction function2 = (x)->{return drone.calculateAOA(drone.getNormalHor((float)x), drone.getProjectedVelocityHorStab(),
//				drone.getAttackVectorHor((float)x));};
//		try {
//			double solution2 = solver.solve(100, function2, minInclinationHorStab, maxInclinationHorStab);
//			horStabInclinationTemp = (float) solution2;
//		} catch (NoBracketingException exc) {
//			throw new IllegalStateException("simulation failed!");
//		}
//		
//		final float horStabInclination = horStabInclinationTemp;
//		final float verStabInclination = verStabInclinationTemp;
//		
//		float thrustTemp = (float) (drone.transformationToDroneCoordinates(drone.getLiftForceLeftWing(leftWingInclination)).getEntry(2)
//				+ drone.transformationToDroneCoordinates(drone.getLiftForceRightWing(rightWingInclination)).getEntry(2));
//		if (thrustTemp > drone.getMaxThrust())
//			thrustTemp = drone.getMaxThrust();
//		final float thrust = thrustTemp;
		
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
	
	public float minMaxInclination(float upperBound, float lowerBound, boolean max, float accuracy, Drone drone, int airfoil) {
		float inclination;
		if (max == true) {
			inclination = upperBound;
		}
		else {
			inclination = lowerBound;
		}
		while (this.hasCrash(inclination, drone, airfoil)) {
			if (max == true) {
				inclination -= accuracy;
				if (inclination < lowerBound) {
					throw new IllegalStateException("simulation failed!");
				}
			}
			else {
				inclination += (1.0/360.0)*2*Math.PI;
				if (inclination > upperBound)
					throw new IllegalStateException("simulation failed!");
			}
		}
		return inclination;
	}
	
	public boolean hasCrash(float inclination, Drone drone) {
		boolean crash = false;
		float AOALeftWing = drone.calculateAOA(drone.getNormalHor(inclination),
				drone.getProjectedVelocityLeftWing(), drone.getAttackVectorHor(inclination));
		if (Float.isNaN(AOALeftWing))
			crash = true;
		if (crash == false) {
			float AOARightWing = drone.calculateAOA(drone.getNormalHor(inclination),
					drone.getProjectedVelocityRightWing(), drone.getAttackVectorHor(inclination));
			if (Float.isNaN(AOARightWing))
				crash = true;
		}
		if (crash == false) {
			float AOAHorStab = drone.calculateAOA(drone.getNormalHor(inclination),
					drone.getProjectedVelocityHorStab(), drone.getAttackVectorHor(inclination));
			if (Float.isNaN(AOAHorStab))
				crash = true;
		}
		if (crash == false) {
			float AOAVerStab = drone.calculateAOA(drone.getNormalVer(inclination),
					drone.getProjectedVelocityVerStab(), drone.getAttackVectorVer(inclination));
			if (Float.isNaN(AOAVerStab))
				crash = true;
		}
		return crash;	
	}
	
	public boolean hasCrash(float inclination, Drone drone, int airfoil) {
		boolean crash = false;
		if (airfoil == 1) {
			float AOALeftWing = drone.calculateAOA(drone.getNormalHor(inclination),
					drone.getProjectedVelocityLeftWing(), drone.getAttackVectorHor(inclination));
			if (Float.isNaN(AOALeftWing))
				crash = true;
		}
		if ((airfoil == 1) && (crash == false)) {
			float AOARightWing = drone.calculateAOA(drone.getNormalHor(inclination),
					drone.getProjectedVelocityRightWing(), drone.getAttackVectorHor(inclination));
			if (Float.isNaN(AOARightWing))
				crash = true;
		}
		if ((airfoil == 2) && (crash == false)) {
			float AOAHorStab = drone.calculateAOA(drone.getNormalHor(inclination),
					drone.getProjectedVelocityHorStab(), drone.getAttackVectorHor(inclination));
			if (Float.isNaN(AOAHorStab))
				crash = true;
		}
		if ((airfoil == 3) && (crash == false)) {
			float AOAVerStab = drone.calculateAOA(drone.getNormalVer(inclination),
					drone.getProjectedVelocityVerStab(), drone.getAttackVectorVer(inclination));
			if (Float.isNaN(AOAVerStab))
				crash = true;
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
	public void moveDrone(float secondsSinceLastUpdate, AutopilotInputs inputs) 
			throws IllegalArgumentException, IllegalStateException {
		if (secondsSinceLastUpdate < 0)
			throw new IllegalArgumentException();
		Drone drone = this.getFirstChildOfType(Drone.class);
		if (drone == null)
			throw new IllegalStateException("this virtual testbed has no drone");
		
		RealVector newPosition = new ArrayRealVector(new double[] {inputs.getX(), inputs.getY(), inputs.getZ()}, false);
		float newHeading = inputs.getHeading();
		float newPitch = inputs.getPitch();
		float newRoll = inputs.getRoll();
		
		drone.setRelativePosition(newPosition);
		drone.setHeading(newHeading);
		drone.setPitch(newPitch);
		drone.setRoll(newRoll);
		
		RealVector newVelocity = newPosition.subtract(this.getPreviousPosition()).mapMultiply(1/secondsSinceLastUpdate);
		float headingDifference = newHeading - this.getPreviousHeading();
		if (headingDifference > Math.PI)
			headingDifference -= 2*Math.PI;
		else if (headingDifference < -Math.PI)
			headingDifference += 2*Math.PI;
		float newHeadingAngularVelocity = headingDifference/secondsSinceLastUpdate;
		float pitchDifference = newPitch - this.getPreviousPitch();
		if (pitchDifference > Math.PI)
			pitchDifference -= 2*Math.PI;
		else if (pitchDifference < -Math.PI)
			pitchDifference += 2*Math.PI;
		float newPitchAngularVelocity = pitchDifference/secondsSinceLastUpdate;
		float rollDifference = newRoll - this.getPreviousRoll();
		if (rollDifference > Math.PI)
			rollDifference -= 2*Math.PI;
		else if (rollDifference < -Math.PI)
			rollDifference += 2*Math.PI;
		float newRollAngularVelocity = rollDifference/secondsSinceLastUpdate;
		
		drone.setVelocity(newVelocity);
		drone.setHeadingAngularVelocity(newHeadingAngularVelocity);
		drone.setPitchAngularVelocity(newPitchAngularVelocity);
		drone.setRollAngularVelocity(newRollAngularVelocity);
		
		this.setPreviousPosition(newPosition);
		this.setPreviousHeading(newHeading);
		this.setPreviousPitch(newPitch);
		this.setPreviousRoll(newRoll);
	}
	
	private RealVector previousPosition;
	
	private float previousHeading;
	
	private float previousPitch;
	
	private float previousRoll;
	
	public RealVector getPreviousPosition() {
		return this.previousPosition;
	}
	
	public float getPreviousHeading(){
		return this.previousHeading;
	}
	
	public float getPreviousPitch() {
		return this.previousPitch;
	}
	
	public float getPreviousRoll() {
		return this.previousRoll;
	}
	
	public static boolean isValidPreviousPosition(RealVector previousPosition) {
		return (previousPosition != null);
	}
	
	public static boolean isValidPreviousHeading(float previousHeading) {
		return ((previousHeading >= 0) && (previousHeading < 2*Math.PI));
	}
	
	public static boolean isValidPreviousPitch(float previousPitch) {
		return ((previousPitch >= 0) && (previousPitch < 2*Math.PI));
	}
	
	public static boolean isValidPreviousRoll(float previousRoll) {
		return ((previousRoll >= 0) && (previousRoll < 2*Math.PI));
	}
	
	public void setPreviousPosition(RealVector previousPosition) 
			throws IllegalArgumentException {
		if (!isValidPreviousPosition(previousPosition))
			throw new IllegalArgumentException();
		this.previousPosition = previousPosition;
	}
	
	public void setPreviousHeading(float previousHeading) 
			throws IllegalArgumentException {
		if (! isValidPreviousHeading(previousHeading))
			throw new IllegalArgumentException();
		this.previousHeading = previousHeading;
	}
	
	public void setPreviousPitch(float previousPitch) 
			throws IllegalArgumentException {
		if (! isValidPreviousPitch(previousPitch))
			throw new IllegalArgumentException();
		this.previousPitch = previousPitch;
	}
	
	public void setPreviousRoll(float previousRoll) 
			throws IllegalArgumentException {
		if (! isValidPreviousRoll(previousRoll))
			throw new IllegalArgumentException();
		this.previousRoll = previousRoll;
	}
}
