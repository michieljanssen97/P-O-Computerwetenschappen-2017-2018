package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.analysis.*;
import org.apache.commons.math3.analysis.solvers.*;
import org.apache.commons.math3.exception.*;
import org.apache.commons.math3.linear.*;
import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.autopilot.image.*;
import be.kuleuven.cs.robijn.common.math.*;
import be.kuleuven.cs.robijn.experiments.ExpPosition;
import interfaces.*;

/**
 * A class of autopilots.
 * 
 * @author Pieter Vandensande
 */
public class Autopilot extends WorldObject implements interfaces.Autopilot {
	private static boolean drawChartPositions = false;
	public static ExpPosition exppos = new ExpPosition();
	
	public AutopilotConfig getConfig() {
		return this.config;
	}
	
	public static boolean isValidConfig(AutopilotConfig config) {
		return (config != null);
	}
	
	private AutopilotConfig config;
	
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
	private final ImageRecognizer recognizer = new ImageRecognizer();
	
	public ImageRecognizer getImageRecognizer() {
		return this.recognizer;
	}
	
	/**
	 * Wel roll ten gevolge van verschillende snelheid van vleugels (door de rotaties). 
	 */
	public AutopilotOutputs timePassed(AutopilotInputs inputs) throws IllegalStateException {
		if (this.isFirstUpdate())
			this.firstUpdate = false;
		else {
			this.moveDrone(inputs.getElapsedTime() - this.getPreviousElapsedTime(), inputs);
		}
        this.setPreviousElapsedTime(inputs.getElapsedTime());  
        
        Drone drone = this.getFirstChildOfType(Drone.class);
        
        double relativeAccuracy = 1.0e-12;
		double absoluteAccuracy = 1.0e-8;
		int maxOrder = 5;
		UnivariateSolver solver = new BracketingNthOrderBrentSolver(relativeAccuracy, absoluteAccuracy, maxOrder);
		float turningTime = 0.5f;
		float xMovementTime = 2.0f;
		float maxRoll = (float) Math.toRadians(45.0);
		float maxHeadingAngularAcceleration = 2.0f;
		float correctionFactor = 3.0f;
		float correctionDistance = 30.0f;
		
        ImageRecognizer recognizer = this.getImageRecognizer();
        float horizontalAngleOfView = (float) Math.toDegrees(this.getConfig().getHorizontalAngleOfView());
      	float verticalAngleOfView = (float) Math.toDegrees(this.getConfig().getVerticalAngleOfView());
		Image image = recognizer.createImage(inputs.getImage(), this.getConfig().getNbRows(), this.getConfig().getNbColumns(),
				horizontalAngleOfView, verticalAngleOfView, drone.getWorldPosition(), drone.getHeading(), drone.getPitch(), drone.getRoll());
		
		RealVector target = new ArrayRealVector(new double[] {-40, 20, -100}, false);
		
//		if (recognizer.isFollowingPathCoordinates()) {
//			target = new ArrayRealVector(recognizer.getCurrentPathTarget(), false);
//		} else {
//			target = new ArrayRealVector(recognizer.searchForCubeInPathArea(image), false);
//		}
		
		float XRotation = (float) Math.atan((target.getEntry(1) - drone.getWorldPosition().getEntry(1))
				/(drone.getWorldPosition().getEntry(2) - target.getEntry(2)));
		float YRotation = (float) Math.atan((drone.getWorldPosition().getEntry(0) - target.getEntry(0))
				/(drone.getWorldPosition().getEntry(2) - target.getEntry(2)));

		float horStabInclinationTemp = 0;
		float verStabInclinationTemp = 0;
		float leftWingInclinationTemp = 0;
		float rightWingInclinationTemp = 0;

		float maxInclinationWing = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 1);
		float minInclinationWing = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 1);
		float maxInclinationHorStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 2);
		float minInclinationHorStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 2);
		float maxInclinationVerStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 3);
		float minInclinationVerStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 3);
		
		float inertiaMatrixXX = (float) (drone.getTailMass()*Math.pow(drone.getTailSize(),2) +drone.getEngineMass()*Math.pow(drone.getEngineDistance(), 2));
		float inertiaMatrixZZ = (float) (2*(drone.getWingMass()*Math.pow(drone.getWingX(),2)));
		float inertiaMatrixYY = inertiaMatrixXX + inertiaMatrixZZ;
		RealVector totalAngularVelocityDroneCoordinates = drone.transformationToDroneCoordinates(drone.getHeadingAngularVelocityVector()
				.add(drone.getRollAngularVelocityVector()));
		RealMatrix inertiaMatrix = new Array2DRowRealMatrix(new double[][] {
			{inertiaMatrixXX, 0,                0},
			{0,               inertiaMatrixYY,  0}, 
			{0,               0,                inertiaMatrixZZ}
			}, false);
		RealVector angularMomentumDroneCoordinates = inertiaMatrix.operate(totalAngularVelocityDroneCoordinates);
		
		float headingNew = drone.getHeading();
		if (headingNew > Math.PI)
			headingNew -= 2*Math.PI;
		float targetHeadingAngularVelocity = (YRotation - headingNew)/turningTime;
		float headingAngularVelocity = drone.getHeadingAngularVelocity();
		float headingAngularAccelerationTemp = (targetHeadingAngularVelocity - headingAngularVelocity)/turningTime;
		if (! Float.isNaN(this.getPreviousHeadingAngularAccelerationError()))
			headingAngularAccelerationTemp -= this.getPreviousHeadingAngularAccelerationError();
		if (headingAngularAccelerationTemp > maxHeadingAngularAcceleration)
			headingAngularAccelerationTemp = maxHeadingAngularAcceleration;
		else if (headingAngularAccelerationTemp < -maxHeadingAngularAcceleration)
			headingAngularAccelerationTemp = -maxHeadingAngularAcceleration;
		final float headingAngularAcceleration = headingAngularAccelerationTemp;
		
		float pitchNew = drone.getPitch();
		if (pitchNew > Math.PI)
			pitchNew -= 2*Math.PI;
		float targetPitchAngularVelocity = (XRotation - pitchNew)/turningTime;
		float pitchAngularVelocity = drone.getPitchAngularVelocity();
		float pitchAngularAccelerationTemp = (targetPitchAngularVelocity - pitchAngularVelocity)/turningTime;
		if (! Float.isNaN(this.getPreviousPitchAngularAccelerationError()))
			pitchAngularAccelerationTemp -= this.getPreviousPitchAngularAccelerationError();
		final float pitchAngularAcceleration = pitchAngularAccelerationTemp;
		
		UnivariateFunction function1 = (x)->{return drone.transformationToDroneCoordinates(drone.getLiftForceVerStab((float)x)).getEntry(0)*drone.getTailSize()
				- inertiaMatrixYY*Math.cos(drone.getRoll())*Math.cos(drone.getPitch())*headingAngularAcceleration
				+ inertiaMatrixYY*Math.sin(drone.getRoll())*pitchAngularAcceleration
				- VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(1)
				- inertiaMatrix.operate(drone.transformationToDroneCoordinates(
						VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
						.add(VectorMath.crossProduct(
							drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
							drone.getRollAngularVelocityVector()
						))
				 )).getEntry(1);};
		try {
			double solution1 = solver.solve(100, function1, minInclinationVerStab, maxInclinationVerStab);
			verStabInclinationTemp = (float) solution1;
		} catch (NoBracketingException exc) {
			if (Math.abs(function1.value(minInclinationVerStab)) < Math.abs(function1.value(maxInclinationVerStab)))
				verStabInclinationTemp = (float) (minInclinationVerStab + Math.toRadians(correctionFactor));
			else {
				verStabInclinationTemp = (float) (maxInclinationVerStab - Math.toRadians(correctionFactor));
			}
		}
		
		final float verStabInclination = verStabInclinationTemp;
		
		float targetYVelocity = (float) (-drone.getVelocity().getEntry(2)*Math.tan(XRotation));
		float yVelocity = (float)drone.getVelocity().getEntry(1);
		float yAccelerationTemp = (targetYVelocity - yVelocity)/turningTime;
		if (! Float.isNaN(this.getPreviousYAccelerationError()))
			yAccelerationTemp -= this.getPreviousYAccelerationError();
		final float yAcceleration = yAccelerationTemp;
		
		float wingInclinationTemp = 0;
		UnivariateFunction function2 = (x)->{return drone.getLiftForceLeftWing((float)x).getEntry(1)
				+ drone.getLiftForceRightWing((float)x).getEntry(1)
				+ drone.getGravitationalForceEngine().getEntry(1)
				+ drone.getGravitationalForceTail().getEntry(1)
				+ drone.getLiftForceVerStab(verStabInclination).getEntry(1)
				+ (2*drone.getGravitationalForceWing().getEntry(1))
				- ((drone.getEngineMass() + drone.getTailMass() + (2 * drone.getWingMass())) * yAcceleration)
				;};
		try {
			double solution2 = solver.solve(100, function2, minInclinationWing, maxInclinationWing);
			wingInclinationTemp = (float) solution2;
		} catch (NoBracketingException exc) {
			if (Math.abs(function2.value(minInclinationWing)) < Math.abs(function2.value(maxInclinationWing)))
				wingInclinationTemp = (float) (minInclinationWing + Math.toRadians(correctionFactor));
			else {
				wingInclinationTemp = (float) (maxInclinationWing - Math.toRadians(correctionFactor));
			}
		}
		
		final float wingInclination = wingInclinationTemp;
		
		float targetXVelocity = (float) (drone.getVelocity().getEntry(2)*Math.tan(YRotation));
		float xVelocity = (float)drone.getVelocity().getEntry(0);
		float xAcceleration = (targetXVelocity - xVelocity)/xMovementTime;
		if (! Float.isNaN(this.getPreviousXAccelerationError()))
			xAcceleration -= this.getPreviousXAccelerationError();
		
		float targetXForce = (drone.getTailMass() + drone.getEngineMass() + 2*drone.getWingMass())*xAcceleration;
		
		float targetRoll = 0;
		UnivariateFunction function3 = new UnivariateFunction() {
			public double value(double x) {
				RealMatrix inverseRollTransformation = new Array2DRowRealMatrix(new double[][] {
					{Math.cos(x),      -Math.sin(x),       0},
					{Math.sin(x),       Math.cos(x),       0}, 
					{0,                         0,                         1}
					}, false);
				RealMatrix inversePitchTransformation = new Array2DRowRealMatrix(new double[][] { 
					{1,       0,                        0},
					{0,       Math.cos(drone.getPitch()),    -Math.sin(drone.getPitch())},
					{0,       Math.sin(drone.getPitch()),     Math.cos(drone.getPitch())}
					}, false);
				RealMatrix inverseHeadingTransformation = new Array2DRowRealMatrix(new double[][] {
					{Math.cos(drone.getHeading()),     0,       Math.sin(drone.getHeading())}, 
					{0,                          1,       0}, 
					{-Math.sin(drone.getHeading()),    0,       Math.cos(drone.getHeading())}
					}, false);
				RealMatrix matrix = inverseHeadingTransformation.multiply(inversePitchTransformation).multiply(inverseRollTransformation);
				return matrix.operate(drone.transformationToDroneCoordinates(drone.getLiftForceLeftWing(wingInclination))).getEntry(0)
						+ matrix.operate(drone.transformationToDroneCoordinates(drone.getLiftForceRightWing(wingInclination))).getEntry(0)
						+ matrix.operate(drone.transformationToDroneCoordinates(drone.getLiftForceVerStab(verStabInclination))).getEntry(0)
						- targetXForce;
			}
        };
        try {
        	double solution3 = solver.solve(100, function3, -Math.PI/2, Math.PI/2);
        	targetRoll = (float) solution3;
        	if (targetRoll < 0)
        		targetRoll += (2*Math.PI);
    		if (targetRoll >= 2*Math.PI)
    			targetRoll = 0;
        } catch (NoBracketingException exc) {
        	targetRoll = drone.getRoll();
		}
        if ((targetRoll > maxRoll) && (targetRoll < Math.PI))
        	targetRoll = maxRoll;
        else if ((targetRoll > Math.PI) && (targetRoll < (2*Math.PI - maxRoll)))
        	targetRoll = (float) (2*Math.PI - maxRoll);
        
        float rollDifference = targetRoll - drone.getRoll();
		if (rollDifference > Math.PI)
			rollDifference -= 2*Math.PI;
		else if (rollDifference < -Math.PI)
			rollDifference += 2*Math.PI;
		
		float targetRollAngularVelocity = rollDifference/turningTime;
		float rollAngularVelocity = drone.getRollAngularVelocity();
		float rollAngularAccelerationTemp = (targetRollAngularVelocity - rollAngularVelocity)/turningTime;
		if (! Float.isNaN(this.getPreviousRollAngularAccelerationError()))
			rollAngularAccelerationTemp -= this.getPreviousRollAngularAccelerationError();
		final float rollAngularAcceleration = rollAngularAccelerationTemp;
		
		float rollInclinationTemp = 0;
		UnivariateFunction function4 = (x)->{return 
				(- drone.transformationToDroneCoordinates(drone.getLiftForceLeftWing(wingInclination - ((float)x))).getEntry(1)*drone.getWingX())
				+ drone.transformationToDroneCoordinates(drone.getLiftForceRightWing(wingInclination + ((float)x))).getEntry(1)*drone.getWingX()
				- inertiaMatrixZZ*rollAngularAcceleration
				+ inertiaMatrixZZ*Math.sin(drone.getPitch())*headingAngularAcceleration
				- VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(2)
				- inertiaMatrix.operate(drone.transformationToDroneCoordinates(
						VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
						.add(VectorMath.crossProduct(
							drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
							drone.getRollAngularVelocityVector()
						))
				 )).getEntry(2);};
		try {
			double solution4 = solver.solve(100, function4, Math.max(minInclinationWing - wingInclination, wingInclination - maxInclinationWing),
					Math.min(maxInclinationWing - wingInclination, wingInclination - minInclinationWing));
			rollInclinationTemp = (float) solution4;
		} catch (NoBracketingException exc) {
			float minRollInclination = Math.max(minInclinationWing - wingInclination, wingInclination - maxInclinationWing);
			float maxRollInclination = Math.min(maxInclinationWing - wingInclination, wingInclination - minInclinationWing);
			if (Math.abs(function4.value(minRollInclination)) < Math.abs(function4.value(maxRollInclination)))
				rollInclinationTemp = (float) (minRollInclination + Math.toRadians(correctionFactor));
			else {
				rollInclinationTemp = (float) (maxRollInclination - Math.toRadians(correctionFactor));
			}
		}
		
		final float rollInclination = rollInclinationTemp;
		
		UnivariateFunction function5 = (x)->{return drone.getLiftForceLeftWing(((float)x) - rollInclination).getEntry(1)
				+ drone.getLiftForceRightWing(((float)x) + rollInclination).getEntry(1)
				+ drone.getGravitationalForceEngine().getEntry(1)
				+ drone.getGravitationalForceTail().getEntry(1)
				+ drone.getLiftForceVerStab(verStabInclination).getEntry(1)
				+ (2*drone.getGravitationalForceWing().getEntry(1))
				- ((drone.getEngineMass() + drone.getTailMass() + (2 * drone.getWingMass())) * yAcceleration)
				;};
		try {
			double solution5 = solver.solve(100, function5, minInclinationWing + Math.abs(rollInclination), maxInclinationWing - Math.abs(rollInclination));
			leftWingInclinationTemp = ((float)solution5) - rollInclination;
			rightWingInclinationTemp = ((float)solution5) + rollInclination;
		} catch (NoBracketingException exc) {
			float minNewWingInclination = minInclinationWing + Math.abs(rollInclination);
			float maxNewWingInclination = maxInclinationWing - Math.abs(rollInclination);
			if (Math.abs(function5.value(minNewWingInclination)) < Math.abs(function5.value(maxNewWingInclination))) {
				leftWingInclinationTemp = (float) (minNewWingInclination + Math.toRadians(correctionFactor) - rollInclination);
				rightWingInclinationTemp = (float) (minNewWingInclination + Math.toRadians(correctionFactor) + rollInclination);
			}
			else {
				leftWingInclinationTemp = (float) (maxNewWingInclination + Math.toRadians(correctionFactor) - rollInclination);
				rightWingInclinationTemp = (float) (maxNewWingInclination + Math.toRadians(correctionFactor) + rollInclination);
			}
		}
        
		final float leftWingInclination = leftWingInclinationTemp;
		final float rightWingInclination = rightWingInclinationTemp;
		
		UnivariateFunction function6 = (x)->{return drone.transformationToDroneCoordinates(drone.getLiftForceHorStab((float)x)).getEntry(1)*drone.getTailSize()
				+ drone.transformationToDroneCoordinates(drone.getLiftForceVerStab(verStabInclination)).getEntry(1)*drone.getTailSize()
				+ inertiaMatrixXX*Math.cos(drone.getRoll())*pitchAngularAcceleration
				+ inertiaMatrixXX*Math.cos(drone.getPitch())*Math.sin(drone.getRoll())*headingAngularAcceleration
				+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(0)
				+ inertiaMatrix.operate(drone.transformationToDroneCoordinates(
						VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
						.add(VectorMath.crossProduct(
							drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
							drone.getRollAngularVelocityVector()
						))
				 )).getEntry(0);};
		try {
			double solution6 = solver.solve(100, function6, minInclinationHorStab, maxInclinationHorStab);
			horStabInclinationTemp = (float) solution6;
		} catch (NoBracketingException exc1) {
			if (Math.abs(function6.value(minInclinationHorStab)) < Math.abs(function6.value(maxInclinationHorStab)))
				horStabInclinationTemp = (float) (minInclinationHorStab + Math.toRadians(correctionFactor));
			else {
				horStabInclinationTemp = (float) (maxInclinationHorStab - Math.toRadians(correctionFactor));
			}
		}
		
		final float horStabInclination = horStabInclinationTemp;
		
		float targetVelocity = this.getInitialZVelocity();
		float zVelocity = (float) drone.transformationToDroneCoordinates(drone.getVelocity()).getEntry(2);
		final float acceleration = (targetVelocity - zVelocity)/turningTime;
		
		float thrustTemp = (float) (drone.transformationToDroneCoordinates(drone.getLiftForceHorStab(horStabInclination)).getEntry(2)
				+ drone.transformationToDroneCoordinates(drone.getLiftForceLeftWing(leftWingInclination)).getEntry(2)
				+ drone.transformationToDroneCoordinates(drone.getLiftForceRightWing(rightWingInclination)).getEntry(2)
				+ drone.transformationToDroneCoordinates(drone.getLiftForceVerStab(verStabInclination)).getEntry(2)
				- (drone.getTailMass() + drone.getEngineMass() + 2*drone.getWingMass())*acceleration);
		if (thrustTemp > drone.getMaxThrust())
			thrustTemp = drone.getMaxThrust();
		else if (thrustTemp < 0)
			thrustTemp = 0;
		final float thrust = thrustTemp;
		
		this.setPreviousHeadingAngularAccelerationError(
				drone.getAngularAccelerations(leftWingInclination, rightWingInclination, horStabInclination, verStabInclination)[0]
				-headingAngularAcceleration);
		this.setPreviousPitchAngularAccelerationError(
				drone.getAngularAccelerations(leftWingInclination, rightWingInclination, horStabInclination, verStabInclination)[1]
				-pitchAngularAcceleration);
		this.setPreviousRollAngularAccelerationError(
				drone.getAngularAccelerations(leftWingInclination, rightWingInclination, horStabInclination, verStabInclination)[2]
				-rollAngularAcceleration);
		this.setPreviousYAccelerationError(
				((float)drone.getAcceleration(thrust, leftWingInclination, rightWingInclination, horStabInclination, verStabInclination).getEntry(1))
				-yAcceleration);
		RealMatrix transformationMatrix = this.calculateTransformationMatrix(targetRoll, drone);
		this.setPreviousXAccelerationError(
				(float) ((transformationMatrix.operate(drone.transformationToDroneCoordinates(drone.getLiftForceHorStab(horStabInclination))).getEntry(0) 
						+ transformationMatrix.operate(new ArrayRealVector(new double[] {0, 0, -thrust}, false)).getEntry(0))
						/(drone.getTailMass() + drone.getEngineMass() + 2*drone.getWingMass())));
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

			@Override
			public float getFrontBrakeForce() {
				//TODO: implement
				throw new RuntimeException("Not implemented");
			}

			@Override
			public float getLeftBrakeForce() {
				//TODO: implement
				throw new RuntimeException("Not implemented");
			}

			@Override
			public float getRightBrakeForce() {
				//TODO: implement
				throw new RuntimeException("Not implemented");
			}
		};
        
        return output;
	}
	
	public RealMatrix calculateTransformationMatrix(float roll, Drone drone) {
		RealMatrix inverseRollTransformation = new Array2DRowRealMatrix(new double[][] {
			{Math.cos(roll),      -Math.sin(roll),       0},
			{Math.sin(roll),       Math.cos(roll),       0}, 
			{0,                         0,                         1}
			}, false);
		RealMatrix inversePitchTransformation = new Array2DRowRealMatrix(new double[][] { 
			{1,       0,                        0},
			{0,       Math.cos(drone.getPitch()),    -Math.sin(drone.getPitch())},
			{0,       Math.sin(drone.getPitch()),     Math.cos(drone.getPitch())}
			}, false);
		RealMatrix inverseHeadingTransformation = new Array2DRowRealMatrix(new double[][] {
			{Math.cos(drone.getHeading()),     0,       Math.sin(drone.getHeading())}, 
			{0,                          1,       0}, 
			{-Math.sin(drone.getHeading()),    0,       Math.cos(drone.getHeading())}
			}, false);
		RealMatrix matrix = inverseHeadingTransformation.multiply(inversePitchTransformation).multiply(inverseRollTransformation);
		return matrix;
	}
	
	private float previousYAccelerationError = Float.NaN;
	
	public float getPreviousYAccelerationError() {
		return this.previousYAccelerationError;
	}
	
	public static boolean isValidPreviousYAccelerationError(float previousYAccelerationError) {
		return ((Float.isNaN(previousYAccelerationError)) || (! Float.isInfinite(previousYAccelerationError)));
	}
	
	public void setPreviousYAccelerationError(float previousYAccelerationError) 
			throws IllegalArgumentException {
		if (! isValidPreviousYAccelerationError(previousYAccelerationError))
			throw new IllegalArgumentException();
		this.previousYAccelerationError = previousYAccelerationError;
	}
	
	private float previousXAccelerationError = Float.NaN;
	
	public float getPreviousXAccelerationError() {
		return this.previousXAccelerationError;
	}
	
	public static boolean isValidPreviousXAccelerationError(float previousXAccelerationError) {
		return ((Float.isNaN(previousXAccelerationError)) || (! Float.isInfinite(previousXAccelerationError)));
	}
	
	public void setPreviousXAccelerationError(float previousXAccelerationError) 
			throws IllegalArgumentException {
		if (! isValidPreviousXAccelerationError(previousXAccelerationError))
			throw new IllegalArgumentException();
		this.previousXAccelerationError = previousXAccelerationError;
	}
	
	private float previousHeadingAngularAccelerationError = Float.NaN;
	
	public float getPreviousHeadingAngularAccelerationError() {
		return this.previousHeadingAngularAccelerationError;
	}
	
	public static boolean isValidPreviousHeadingAngularAccelerationError(float previousHeadingAngularAccelerationError) {
		return ((Float.isNaN(previousHeadingAngularAccelerationError)) || (! Float.isInfinite(previousHeadingAngularAccelerationError)));
	}
	
	public void setPreviousHeadingAngularAccelerationError(float previousHeadingAngularAccelerationError) 
			throws IllegalArgumentException {
		if (! isValidPreviousHeadingAngularAccelerationError(previousHeadingAngularAccelerationError))
			throw new IllegalArgumentException();
		this.previousHeadingAngularAccelerationError = previousHeadingAngularAccelerationError;
	}
	
	private float previousPitchAngularAccelerationError = Float.NaN;
	
	public float getPreviousPitchAngularAccelerationError() {
		return this.previousPitchAngularAccelerationError;
	}
	
	public static boolean isValidPreviousPitchAngularAccelerationError(float previousPitchAngularAccelerationError) {
		return ((Float.isNaN(previousPitchAngularAccelerationError)) || (! Float.isInfinite(previousPitchAngularAccelerationError)));
	}
	
	public void setPreviousPitchAngularAccelerationError(float previousPitchAngularAccelerationError) 
			throws IllegalArgumentException {
		if (! isValidPreviousPitchAngularAccelerationError(previousPitchAngularAccelerationError))
			throw new IllegalArgumentException();
		this.previousPitchAngularAccelerationError = previousPitchAngularAccelerationError;
	}
	
	private float previousRollAngularAccelerationError = Float.NaN;
	
	public float getPreviousRollAngularAccelerationError() {
		return this.previousRollAngularAccelerationError;
	}
	
	public static boolean isValidPreviousRollAngularAccelerationError(float previousRollAngularAccelerationError) {
		return ((Float.isNaN(previousRollAngularAccelerationError)) || (! Float.isInfinite(previousRollAngularAccelerationError)));
	}
	
	public void setPreviousRollAngularAccelerationError(float previousRollAngularAccelerationError) 
			throws IllegalArgumentException {
		if (! isValidPreviousRollAngularAccelerationError(previousRollAngularAccelerationError))
			throw new IllegalArgumentException();
		this.previousRollAngularAccelerationError = previousRollAngularAccelerationError;
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
				inclination += accuracy;
				if (inclination > upperBound)
					throw new IllegalStateException("simulation failed!");
			}
		}
		return inclination;
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
	
    public static boolean isPositionDrawn() {
    	return drawChartPositions;
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
		if (newHeading < 0)
			newHeading += (2*Math.PI);
		if (newHeading >= 2*Math.PI)
			newHeading = 0;
		float newPitch = inputs.getPitch();
		if (newPitch < 0)
			newPitch += (2*Math.PI);
		if (newPitch >= 2*Math.PI)
			newPitch = 0;
		float newRoll = inputs.getRoll();
		if (newRoll < 0)
			newRoll += (2*Math.PI);
		if (newRoll >= 2*Math.PI)
			newRoll = 0;
		
		drone.setRelativePosition(newPosition);
		drone.setHeading(newHeading);
		drone.setPitch(newPitch);
		drone.setRoll(newRoll);
		
		RealVector newAverageVelocity = newPosition.subtract(this.getPreviousPosition()).mapMultiply(1/secondsSinceLastUpdate);
		RealVector newVelocity = newAverageVelocity.subtract(this.getPreviousVelocity()).mapMultiply(2).add(this.getPreviousVelocity());
		float headingDifference = newHeading - this.getPreviousHeading();
		if (headingDifference > Math.PI)
			headingDifference -= 2*Math.PI;
		else if (headingDifference < -Math.PI)
			headingDifference += 2*Math.PI;
		float newAverageHeadingAngularVelocity = headingDifference/secondsSinceLastUpdate;
		float newHeadingAngularVelocity = this.getPreviousHeadingAngularVelocity()
				+ 2*(newAverageHeadingAngularVelocity - this.getPreviousHeadingAngularVelocity());
		float pitchDifference = newPitch - this.getPreviousPitch();
		if (pitchDifference > Math.PI)
			pitchDifference -= 2*Math.PI;
		else if (pitchDifference < -Math.PI)
			pitchDifference += 2*Math.PI;
		float newAveragePitchAngularVelocity = pitchDifference/secondsSinceLastUpdate;
		float newPitchAngularVelocity = this.getPreviousPitchAngularVelocity()
				+ 2*(newAveragePitchAngularVelocity - this.getPreviousPitchAngularVelocity());
		float rollDifference = newRoll - this.getPreviousRoll();
		if (rollDifference > Math.PI)
			rollDifference -= 2*Math.PI;
		else if (rollDifference < -Math.PI)
			rollDifference += 2*Math.PI;
		float newAverageRollAngularVelocity = rollDifference/secondsSinceLastUpdate;
		float newRollAngularVelocity = this.getPreviousRollAngularVelocity()
				+ 2*(newAverageRollAngularVelocity - this.getPreviousRollAngularVelocity());
		
		drone.setVelocity(newVelocity);
		drone.setHeadingAngularVelocity(newHeadingAngularVelocity);
		drone.setPitchAngularVelocity(newPitchAngularVelocity);
		drone.setRollAngularVelocity(newRollAngularVelocity);
		
		this.setPreviousPosition(newPosition);
		this.setPreviousVelocity(newVelocity);
		this.setPreviousHeading(newHeading);
		this.setPreviousHeadingAngularVelocity(newHeadingAngularVelocity);
		this.setPreviousPitch(newPitch);
		this.setPreviousPitchAngularVelocity(newPitchAngularVelocity);
		this.setPreviousRoll(newRoll);
		this.setPreviousRollAngularVelocity(newRollAngularVelocity);
		
		if(isPositionDrawn()) {
			exppos.updateValuesToDrawForFloat(drone);
		}
	}
	
	private RealVector previousPosition;
	
	private RealVector previousVelocity;
	
	private float previousHeading;
	
	private float previousHeadingAngularVelocity;
	
	private float previousPitch;
	
	private float previousPitchAngularVelocity;
	
	private float previousRoll;
	
	private float previousRollAngularVelocity;
	
	private float initialZVelocity;
	
	public RealVector getPreviousPosition() {
		return this.previousPosition;
	}
	
	public RealVector getPreviousVelocity() {
		return this.previousVelocity;
	}
	
	public float getPreviousHeading(){
		return this.previousHeading;
	}
	
	public float getPreviousHeadingAngularVelocity() {
		return this.previousHeadingAngularVelocity;
	}
	
	public float getPreviousPitch() {
		return this.previousPitch;
	}
	
	public float getPreviousPitchAngularVelocity() {
		return this.previousPitchAngularVelocity;
	}
	
	public float getPreviousRoll() {
		return this.previousRoll;
	}
	
	public float getPreviousRollAngularVelocity() {
		return this.previousRollAngularVelocity;
	}
	
	public float getInitialZVelocity() {
		return this.initialZVelocity;
	}
	
	public static boolean isValidPreviousPosition(RealVector previousPosition) {
		return (previousPosition != null);
	}
	
	public static boolean isValidPreviousVelocity(RealVector previousVelocity) {
		return (previousVelocity != null);
	}
	
	public static boolean isValidPreviousHeading(float previousHeading) {
		return ((previousHeading >= 0) && (previousHeading < 2*Math.PI));
	}
	
	public static boolean isValidPreviousHeadingAngularVelocity(float previousHeadingAngularVelocity) {
		return ((! Float.isNaN(previousHeadingAngularVelocity)) & (Float.isFinite(previousHeadingAngularVelocity)));
	}
	
	public static boolean isValidPreviousPitch(float previousPitch) {
		return ((previousPitch >= 0) && (previousPitch < 2*Math.PI));
	}
	
	public static boolean isValidPreviousPitchAngularVelocity(float previousPitchAngularVelocity) {
		return ((! Float.isNaN(previousPitchAngularVelocity)) & (Float.isFinite(previousPitchAngularVelocity)));
	}
	
	public static boolean isValidPreviousRoll(float previousRoll) {
		return ((previousRoll >= 0) && (previousRoll < 2*Math.PI));
	}
	
	public static boolean isValidPreviousRollAngularVelocity(float previousRollAngularVelocity) {
		return ((! Float.isNaN(previousRollAngularVelocity)) & (Float.isFinite(previousRollAngularVelocity)));
	}
	
	public static boolean isValidInitialZVelocity(float initialZVelocity) {
		return ((initialZVelocity <= 0) && (Float.isFinite(initialZVelocity)));
	}
	
	public void setPreviousPosition(RealVector previousPosition) 
			throws IllegalArgumentException {
		if (!isValidPreviousPosition(previousPosition))
			throw new IllegalArgumentException();
		this.previousPosition = previousPosition;
	}
	
	public void setPreviousVelocity(RealVector previousVelocity) 
			throws IllegalArgumentException {
		if (!isValidPreviousVelocity(previousVelocity))
			throw new IllegalArgumentException();
		this.previousVelocity = previousVelocity;
	}
	
	public void setPreviousHeading(float previousHeading) 
			throws IllegalArgumentException {
		if (! isValidPreviousHeading(previousHeading))
			throw new IllegalArgumentException();
		this.previousHeading = previousHeading;
	}
	
	public void setPreviousHeadingAngularVelocity(float previousHeadingAngularVelocity) 
			throws IllegalArgumentException {
		if (! isValidPreviousHeadingAngularVelocity(previousHeadingAngularVelocity))
			throw new IllegalArgumentException();
		this.previousHeadingAngularVelocity = previousHeadingAngularVelocity;
	}
	
	public void setPreviousPitch(float previousPitch) 
			throws IllegalArgumentException {
		if (! isValidPreviousPitch(previousPitch))
			throw new IllegalArgumentException();
		this.previousPitch = previousPitch;
	}
	
	public void setPreviousPitchAngularVelocity(float previousPitchAngularVelocity) 
			throws IllegalArgumentException {
		if (! isValidPreviousPitchAngularVelocity(previousPitchAngularVelocity))
			throw new IllegalArgumentException();
		this.previousPitchAngularVelocity = previousPitchAngularVelocity;
	}
	
	public void setPreviousRoll(float previousRoll) 
			throws IllegalArgumentException {
		if (! isValidPreviousRoll(previousRoll))
			throw new IllegalArgumentException();
		this.previousRoll = previousRoll;
	}
	public void setPreviousRollAngularVelocity(float previousRollAngularVelocity)
			throws IllegalArgumentException {
		if (! isValidPreviousRollAngularVelocity(previousRollAngularVelocity))
			throw new IllegalArgumentException();
		this.previousRollAngularVelocity = previousRollAngularVelocity;
	}

	@Override
	public AutopilotOutputs simulationStarted(AutopilotConfig config, AutopilotInputs inputs) {

		if (! isValidConfig(config))
			throw new IllegalArgumentException();
		RealVector initialVelocity = new ArrayRealVector(new double[] {0, 0, -10.0}, false);
		Drone drone = new Drone(config, initialVelocity);
		this.addChild(drone);
		this.config = config;
		RealVector previousPosition = drone.getWorldPosition();
		if (!isValidPreviousPosition(previousPosition))
			throw new IllegalArgumentException();
		this.previousPosition = previousPosition;
		RealVector previousVelocity = drone.getVelocity();
		if (!isValidPreviousVelocity(previousVelocity))
			throw new IllegalArgumentException();
		this.previousVelocity = previousVelocity;
		float previousHeading = drone.getHeading();
		if (! isValidPreviousHeading(previousHeading))
			throw new IllegalArgumentException();
		this.previousHeading = previousHeading;
		float previousHeadingAngularVelocity = drone.getHeadingAngularVelocity();
		if (! isValidPreviousHeadingAngularVelocity(previousHeadingAngularVelocity))
			throw new IllegalArgumentException();
		this.previousHeadingAngularVelocity = previousHeadingAngularVelocity;
		float previousPitch = drone.getPitch();
		if (! isValidPreviousPitch(previousPitch))
			throw new IllegalArgumentException();
		this.previousPitch = previousPitch;
		float previousPitchAngularVelocity = drone.getPitchAngularVelocity();
		if (! isValidPreviousPitchAngularVelocity(previousPitchAngularVelocity))
			throw new IllegalArgumentException();
		this.previousPitchAngularVelocity = previousPitchAngularVelocity;
		float previousRoll = drone.getRoll();
		if (! isValidPreviousRoll(previousRoll))
			throw new IllegalArgumentException();
		this.previousRoll = previousRoll;
		float previousRollAngularVelocity = drone.getRollAngularVelocity();
		if (! isValidPreviousRollAngularVelocity(previousRollAngularVelocity))
			throw new IllegalArgumentException();
		this.previousRollAngularVelocity = previousRollAngularVelocity;
		float initialZVelocity = (float) drone.getVelocity().getEntry(2);
		if (! isValidInitialZVelocity(initialZVelocity))
			throw new IllegalArgumentException();
		this.initialZVelocity = initialZVelocity;

		return timePassed(inputs);
	}


	@Override
	public void setPath(Path path) {
		//TODO
		this.getImageRecognizer().setPath(new CubePath(path.getX(), path.getY(), path.getZ()));
	}

	@Override
	public void simulationEnded() {
		throw new IllegalArgumentException();
	}
}
