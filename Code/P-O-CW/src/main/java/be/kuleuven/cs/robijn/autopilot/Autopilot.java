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
	public FlightMode currentFlightMode = FlightMode.TAXI;
	
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
        
        float horStabInclination = 0;
		float verStabInclination = 0;
		float leftWingInclination = 0;
		float rightWingInclination = 0;
		float thrust = 0;
		float leftBrakeForce = 0;
		float rightBrakeForce = 0;
		float frontBrakeForce = 0;
		
		double relativeAccuracy = 1.0e-12;
		double absoluteAccuracy = 1.0e-8;
		int maxOrder = 5;
		UnivariateSolver solver = new BracketingNthOrderBrentSolver(relativeAccuracy, absoluteAccuracy, maxOrder);
		float turningTime = 0.5f;
		float xMovementTime = 3f;
		float maxRoll = (float) Math.toRadians(45.0);
		float maxHeadingAngularAcceleration = Float.MAX_VALUE;
		float maxPitchAngularAcceleration =Float.MAX_VALUE;
		float maxRollAngularAcceleration = Float.MAX_VALUE;
		float maxXAcceleration = Float.MAX_VALUE;
		float maxYAcceleration = Float.MAX_VALUE;
		float correctionFactor = 3.0f;
		float correctionDistance = 30.0f;
		float pitchTakeOff = (float) Math.toRadians(10.0);
		float targetVelocity = -43f;
		float hight = 25;
		


		double[] targetPositionCoordinates = {5528,1.4,2371};
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
			
		if (this.getFlightMode() == FlightMode.ASCEND) { //ascend

			float maxInclinationWing = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 1);
			float minInclinationWing = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 1);
			float maxInclinationHorStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 2);
			float minInclinationHorStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 2);
			float maxInclinationVerStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 3);
			float minInclinationVerStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 3);

			thrust = this.getConfig().getMaxThrust();
			
			float takeOffSpeed = (float) Math.sqrt((-drone.getTotalGravitationalForce().getEntry(1))
					/(2*this.getConfig().getMaxAOA()*this.getConfig().getWingLiftSlope()*Math.cos(this.getConfig().getMaxAOA())));
			
			float maxPitch = (float) Math.asin(drone.getWorldPosition().getEntry(1)/this.getConfig().getTailSize());
			maxPitch = (float) (maxPitch - Math.toRadians(correctionFactor));
			if ((maxPitch > pitchTakeOff) || (Float.isNaN(maxPitch)))
				maxPitch = pitchTakeOff;
			
			float pitchNew = drone.getPitch();
			if (pitchNew > Math.PI)
				pitchNew -= 2*Math.PI;
			float pitchAngularVelocity = drone.getPitchAngularVelocity();
			float pitchAngularAccelerationTemp = (maxPitch - pitchAngularVelocity)/turningTime;
			if (! Float.isNaN(this.getPreviousPitchAngularAccelerationError()))
				pitchAngularAccelerationTemp -= this.getPreviousPitchAngularAccelerationError();
			final float pitchAngularAcceleration = pitchAngularAccelerationTemp;
			
			UnivariateFunction function1 = (x)->{return drone.transformationToDroneCoordinates(drone.getLiftForceHorStab((float)x)).getEntry(1)*drone.getTailSize()
					+ inertiaMatrixXX*Math.cos(drone.getRoll())*pitchAngularAcceleration
					+ VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(0);};
			
			if (Math.abs(drone.transformationToDroneCoordinates(drone.getVelocity()).getEntry(2)) > takeOffSpeed) {
				leftWingInclination = (float) (maxInclinationWing - Math.toRadians(correctionFactor));
				rightWingInclination = (float) (maxInclinationWing - Math.toRadians(correctionFactor));
				
				try {
					double solution6 = solver.solve(100, function1, minInclinationHorStab, maxInclinationHorStab);
					horStabInclination = (float) solution6;
				} catch (NoBracketingException exc1) {
					if (Math.abs(function1.value(minInclinationHorStab)) < Math.abs(function1.value(maxInclinationHorStab)))
						horStabInclination = (float) (minInclinationHorStab + Math.toRadians(correctionFactor));
					else {
						horStabInclination = (float) (maxInclinationHorStab - Math.toRadians(correctionFactor));
					}
				}
				this.setPreviousPitchAngularAccelerationError(
						drone.getAngularAccelerations(leftWingInclination, rightWingInclination, horStabInclination, verStabInclination,
								frontBrakeForce, leftBrakeForce, rightBrakeForce)[1]
						-pitchAngularAcceleration);
			}
			
			if (drone.getWorldPosition().getEntry(1) > this.getConfig().getTailSize())
				this.setFlightMode(FlightMode.FULL_FLIGHT);
		}

		else if ((this.getFlightMode() == FlightMode.FULL_FLIGHT) || (this.getFlightMode() == FlightMode.LAND)) {
			float maxInclinationWing = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 1);
			float minInclinationWing = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 1);
			float maxInclinationHorStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 2);
			float minInclinationHorStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 2);
			float maxInclinationVerStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 3);
			float minInclinationVerStab = this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 3);

			if ((-this.getConfig().getWheelY() + this.getConfig().getTyreRadius()) >= drone.getWorldPosition().getEntry(1)) {
				this.setFlightMode(FlightMode.TAXI);
			}
			else {
		        ImageRecognizer recognizer = this.getImageRecognizer();
		        float[] necessaryRotation;
		        float horizontalAngleOfView = (float) Math.toDegrees(this.getConfig().getHorizontalAngleOfView());
		        float verticalAngleOfView = (float) Math.toDegrees(this.getConfig().getVerticalAngleOfView());
				
				float distanceToCube;
				try{
					Image image = recognizer.createImage(inputs.getImage(), this.getConfig().getNbRows(), this.getConfig().getNbColumns(),
							horizontalAngleOfView, verticalAngleOfView, drone.getWorldPosition(), drone.getHeading(), drone.getPitch(), drone.getRoll());
					ImageRecognizerCube closestCube = recognizer.getClosestCubeInWorld(image);
					necessaryRotation = recognizer.getNecessaryRotation(image, closestCube.getHue(), closestCube.getSaturation());
					distanceToCube = recognizer.getDistanceToCube(image, closestCube.getHue(), closestCube.getSaturation());
				} catch (NullPointerException exc1) {
					necessaryRotation = new float[2];
					distanceToCube = 0;
				} catch (IllegalArgumentException exc2) {
					Image image = recognizer.createImage(inputs.getImage(), this.getConfig().getNbRows(), this.getConfig().getNbColumns(),
							horizontalAngleOfView, verticalAngleOfView, drone.getWorldPosition(), drone.getHeading(), drone.getPitch(), drone.getRoll());
					ImageRecognizerCube closestCube = recognizer.getClosestCubeInWorld(image);
					necessaryRotation = recognizer.getNecessaryRotation(image, closestCube.getHue(), closestCube.getSaturation());
					distanceToCube = 0;

		
	//	else if (this.getMode() == 1) {
			
	//        ImageRecognizer recognizer = this.getImageRecognizer();
	        
	//        if (recognizer.getPath().getPathSize() == 0) {
	        	//TODO switch to landing mode
	//        }
	        
	//        float[] necessaryRotation;
	//        float horizontalAngleOfView = (float) Math.toDegrees(this.getConfig().getHorizontalAngleOfView());
	 //       float verticalAngleOfView = (float) Math.toDegrees(this.getConfig().getVerticalAngleOfView());
			
	//        Image image = recognizer.createImage(inputs.getImage(), this.getConfig().getNbRows(), this.getConfig().getNbColumns(),
	//				horizontalAngleOfView, verticalAngleOfView, drone.getWorldPosition(), drone.getHeading(), drone.getPitch(), drone.getRoll());
//			float distanceToCube;
//			try{
//				ImageRecognizerCube closestCube = recognizer.getClosestCubeInWorld(image);
//				if (closestCube == null)
//					this.simulationEnded();
//				necessaryRotation = recognizer.getNecessaryRotation(image, closestCube.getHue(), closestCube.getSaturation());
//				distanceToCube = recognizer.getDistanceToCube(image, closestCube.getHue(), closestCube.getSaturation());
//			} catch (NullPointerException exc1) {
//				necessaryRotation = new float[2];
//				distanceToCube = 0;
//			} catch (IllegalArgumentException exc2) {
////				Image image = recognizer.createImage(inputs.getImage(), this.getConfig().getNbRows(), this.getConfig().getNbColumns(),
////						horizontalAngleOfView, verticalAngleOfView, drone.getWorldPosition(), drone.getHeading(), drone.getPitch(), drone.getRoll());
//				ImageRecognizerCube closestCube = recognizer.getClosestCubeInWorld(image);
//				necessaryRotation = recognizer.getNecessaryRotation(image, closestCube.getHue(), closestCube.getSaturation());
//				distanceToCube = 0;
//			}

//			float imageYRotation = (float) Math.toRadians(necessaryRotation[0]);
//			float imageXRotation = (float) Math.toRadians(necessaryRotation[1]);
//			
//			float angleXYPlane;
//			if ((imageYRotation == 0.0) && (imageXRotation >= 0.0))
//				angleXYPlane = (float) (Math.PI/2);
//			else if ((imageYRotation == 0.0) && (imageXRotation < 0.0))
//				angleXYPlane = (float) (-Math.PI/2);
//			else {
//				angleXYPlane = (float) Math.atan(Math.tan(imageXRotation)/Math.tan(imageYRotation));
//			}
//			float lengthXYPlane = (float) Math.sqrt(Math.pow(Math.tan(imageXRotation),2) + Math.pow(Math.tan(imageYRotation), 2));
//			float newImageYRotation = (float) Math.atan(lengthXYPlane * Math.cos(angleXYPlane - drone.getRoll()));
//			float newImageXRotation = (float) Math.atan(lengthXYPlane * Math.sin(angleXYPlane - drone.getRoll()));
//			if (imageYRotation >= 0.0) {
//				imageYRotation = newImageYRotation;
//				imageXRotation = newImageXRotation;
//			}
//			else {
//				imageYRotation = -newImageYRotation;
//				imageXRotation = -newImageXRotation;
//			}
//			float pitch = drone.getPitch();
//			if (pitch > Math.PI)
//				pitch -= 2*Math.PI;
//			float heading = drone.getHeading();
//			if (heading > Math.PI)
//				heading -= 2*Math.PI;
//			if ((distanceToCube*Math.cos(heading + imageYRotation)*Math.cos(pitch + imageXRotation)) > correctionDistance) {
//				imageXRotation = (float) ((3.0/2.0)*imageXRotation + (1.0/2.0)*pitch);
//				imageYRotation = (float) ((5.0/4.0)*imageYRotation + (1.0/4.0)*heading);
//			}
			
	//		RealVector target = new ArrayRealVector(new double[] {0, 20, -1000}, false);
	//		if (recognizer.isFollowingPathCoordinates()) {
	//			target = recognizer.getCurrentPathTarget();
	//		} else {
	//			target = recognizer.searchForCubeInPathArea(image);
	//		}
			
			
			
	//		float XRotation = (float) Math.atan((target.getEntry(1) - drone.getWorldPosition().getEntry(1))
	//				/(drone.getWorldPosition().getEntry(2) - target.getEntry(2)));
	//		float YRotation = (float) Math.atan((drone.getWorldPosition().getEntry(0) - target.getEntry(0))
	//				/(drone.getWorldPosition().getEntry(2) - target.getEntry(2)));
	//		
	//		float headingNew = drone.getHeading();
	//		if (headingNew > Math.PI)
	//			headingNew -= 2*Math.PI;
	//		float targetHeadingAngularVelocity = (YRotation - headingNew)/turningTime;
	//		float headingAngularVelocity = drone.getHeadingAngularVelocity();
	//		float headingAngularAccelerationTemp = (targetHeadingAngularVelocity - headingAngularVelocity)/turningTime;
	//		if (! Float.isNaN(this.getPreviousHeadingAngularAccelerationError()))
	//			headingAngularAccelerationTemp -= this.getPreviousHeadingAngularAccelerationError();
	//		if (headingAngularAccelerationTemp > maxHeadingAngularAcceleration)
	//			headingAngularAccelerationTemp = maxHeadingAngularAcceleration;
	//		else if (headingAngularAccelerationTemp < -maxHeadingAngularAcceleration)
	//			headingAngularAccelerationTemp = -maxHeadingAngularAcceleration;
	//		final float headingAngularAcceleration = headingAngularAccelerationTemp;
			
	//		float pitchNew = drone.getPitch();
	//		if (pitchNew > Math.PI)
	//			pitchNew -= 2*Math.PI;
	//		float targetPitchAngularVelocity = (XRotation - pitchNew)/turningTime;
	//		float pitchAngularVelocity = drone.getPitchAngularVelocity();
	//		float pitchAngularAccelerationTemp = (targetPitchAngularVelocity - pitchAngularVelocity)/turningTime;
	//		if (! Float.isNaN(this.getPreviousPitchAngularAccelerationError()))
	//			pitchAngularAccelerationTemp -= this.getPreviousPitchAngularAccelerationError();
	//		final float pitchAngularAcceleration = pitchAngularAccelerationTemp;
			
	//		UnivariateFunction function1 = (x)->{return drone.transformationToDroneCoordinates(drone.getLiftForceVerStab((float)x)).getEntry(0)*drone.getTailSize()
	//				- inertiaMatrixYY*Math.cos(drone.getRoll())*Math.cos(drone.getPitch())*headingAngularAcceleration
	//				+ inertiaMatrixYY*Math.sin(drone.getRoll())*pitchAngularAcceleration
	//				- VectorMath.crossProduct(totalAngularVelocityDroneCoordinates, angularMomentumDroneCoordinates).getEntry(1)
	//				- inertiaMatrix.operate(drone.transformationToDroneCoordinates(
	//						VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
	//						.add(VectorMath.crossProduct(
	//							drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
		//						drone.getRollAngularVelocityVector()
	//						))
		//			 )).getEntry(1);};
	//		try {
	//			double solution1 = solver.solve(100, function1, minInclinationVerStab, maxInclinationVerStab);
	//			verStabInclination = (float) solution1;
	//		} catch (NoBracketingException exc) {
	//			if (Math.abs(function1.value(minInclinationVerStab)) < Math.abs(function1.value(maxInclinationVerStab)))
	//				verStabInclination = (float) (minInclinationVerStab + Math.toRadians(correctionFactor));
	//			else {
	//				verStabInclination = (float) (maxInclinationVerStab - Math.toRadians(correctionFactor));

				}
	
				float imageYRotation = (float) Math.toRadians(necessaryRotation[0]);
				float imageXRotation = (float) Math.toRadians(necessaryRotation[1]);
				
				float angleXYPlane;
				if ((imageYRotation == 0.0) && (imageXRotation >= 0.0))
					angleXYPlane = (float) (Math.PI/2);
				else if ((imageYRotation == 0.0) && (imageXRotation < 0.0))
					angleXYPlane = (float) (-Math.PI/2);
				else {
					angleXYPlane = (float) Math.atan(Math.tan(imageXRotation)/Math.tan(imageYRotation));
				}
				float lengthXYPlane = (float) Math.sqrt(Math.pow(Math.tan(imageXRotation),2) + Math.pow(Math.tan(imageYRotation), 2));
				float newImageYRotation = (float) Math.atan(lengthXYPlane * Math.cos(angleXYPlane - drone.getRoll()));
				float newImageXRotation = (float) Math.atan(lengthXYPlane * Math.sin(angleXYPlane - drone.getRoll()));
				if (imageYRotation >= 0.0) {
					imageYRotation = newImageYRotation;
					imageXRotation = newImageXRotation;
				}
				else {
					imageYRotation = -newImageYRotation;
					imageXRotation = -newImageXRotation;
				}
				float pitch = drone.getPitch();
				if (pitch > Math.PI)
					pitch -= 2*Math.PI;
				float heading = drone.getHeading();
				if (heading > Math.PI)
					heading -= 2*Math.PI;
				if ((distanceToCube*Math.cos(heading + imageYRotation)*Math.cos(pitch + imageXRotation)) > correctionDistance) {
					imageXRotation = (float) ((3.0/2.0)*imageXRotation + (1.0/2.0)*pitch);
					imageYRotation = (float) ((5.0/4.0)*imageYRotation + (1.0/4.0)*heading);
				}
				
				if (this.getFlightMode() == FlightMode.LAND) {
					if (drone.getWorldPosition().getEntry(2) < (this.getTarget().getEntry(2)+1)) {
						if (drone.getWorldPosition().getEntry(1) > 2*hight) {
							this.setTarget(new ArrayRealVector(new double[] {this.getTarget().getEntry(0),
									this.getTarget().getEntry(1) - hight, this.getTarget().getEntry(2) - 5*hight}, false));
						}
						else if ((hight < drone.getWorldPosition().getEntry(1)) && (drone.getWorldPosition().getEntry(1) < 2*hight)) {
							this.setTarget(new ArrayRealVector(new double[] {this.getTarget().getEntry(0),
									0.8*hight, this.getTarget().getEntry(2) - 10*hight}, false));
						}
						else {
							this.setTarget(new ArrayRealVector(new double[] {this.getTarget().getEntry(0),
									-this.getConfig().getWheelY() + this.getConfig().getTyreRadius(), this.getTarget().getEntry(2) - 20*hight}, false));
						}
					}
				} else {
					this.setTarget(new ArrayRealVector(new double[] {50, 100, -1000}, false));
				}
				float XRotation = (float) Math.atan((this.getTarget().getEntry(1) - drone.getWorldPosition().getEntry(1))
						/(drone.getWorldPosition().getEntry(2) - this.getTarget().getEntry(2)));
				float YRotation = (float) Math.atan((drone.getWorldPosition().getEntry(0) - this.getTarget().getEntry(0))
						/(drone.getWorldPosition().getEntry(2) - this.getTarget().getEntry(2)));
				
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
				if (pitchAngularAccelerationTemp > maxPitchAngularAcceleration)
					pitchAngularAccelerationTemp = maxPitchAngularAcceleration;
				else if (pitchAngularAccelerationTemp < -maxPitchAngularAcceleration)
					pitchAngularAccelerationTemp = -maxPitchAngularAcceleration;
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
					verStabInclination = (float) solution1;
				} catch (NoBracketingException exc) {
					if (Math.abs(function1.value(minInclinationVerStab)) < Math.abs(function1.value(maxInclinationVerStab)))
						verStabInclination = (float) (minInclinationVerStab + Math.toRadians(correctionFactor));
					else {
						verStabInclination = (float) (maxInclinationVerStab - Math.toRadians(correctionFactor));
					}
				}
				
				final float verStabInclinationTemp = verStabInclination;
				
				float targetYVelocity = (float) (-drone.getVelocity().getEntry(2)*Math.tan(XRotation));
				float yVelocity = (float)drone.getVelocity().getEntry(1);
				float yAccelerationTemp = (targetYVelocity - yVelocity)/turningTime;
				if (! Float.isNaN(this.getPreviousYAccelerationError()))
					yAccelerationTemp -= this.getPreviousYAccelerationError();
				if (yAccelerationTemp > maxYAcceleration)
					yAccelerationTemp = maxYAcceleration;
				else if (yAccelerationTemp < -maxYAcceleration)
					yAccelerationTemp = -maxYAcceleration;
				final float yAcceleration = yAccelerationTemp;
				
				float wingInclinationTemp = 0;
				UnivariateFunction function2 = (x)->{return drone.getLiftForceLeftWing((float)x).getEntry(1)
						+ drone.getLiftForceRightWing((float)x).getEntry(1)
						+ drone.getTotalGravitationalForce().getEntry(1)
						+ drone.getLiftForceVerStab(verStabInclinationTemp).getEntry(1)
						- (drone.getTotalMass() * yAcceleration)
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
				if (xAcceleration > maxXAcceleration)
					xAcceleration = maxXAcceleration;
				else if (xAcceleration < -maxXAcceleration)
					xAcceleration = -maxXAcceleration;
				
				float targetXForce = drone.getTotalMass()*xAcceleration;
				
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
								+ matrix.operate(drone.transformationToDroneCoordinates(drone.getLiftForceVerStab(verStabInclinationTemp))).getEntry(0)
								- targetXForce;
					}
		        };
		        try {
		        	double solution3 = solver.solve(100, function3, -Math.PI/2, Math.PI/2);
		        	targetRoll = (float) solution3;
		        } catch (NoBracketingException exc) {
		        	if (xAcceleration > 0)
		        		targetRoll = -maxRoll;
		        	else if (xAcceleration < 0)
		        		targetRoll = maxRoll;
		        	else {
		        		targetRoll = 0;
		        	}
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
				if (rollAngularAccelerationTemp > maxRollAngularAcceleration)
					rollAngularAccelerationTemp = maxRollAngularAcceleration;
				else if (rollAngularAccelerationTemp < -maxRollAngularAcceleration)
					rollAngularAccelerationTemp = -maxRollAngularAcceleration;
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
						+ drone.getTotalGravitationalForce().getEntry(1)
						+ drone.getLiftForceVerStab(verStabInclinationTemp).getEntry(1)
						- (drone.getTotalMass() * yAcceleration)
						;};
				try {
					double solution5 = solver.solve(100, function5, minInclinationWing + Math.abs(rollInclination), maxInclinationWing - Math.abs(rollInclination));
					leftWingInclination = ((float)solution5) - rollInclination;
					rightWingInclination = ((float)solution5) + rollInclination;
				} catch (NoBracketingException exc) {
					float minNewWingInclination = minInclinationWing + Math.abs(rollInclination);
					float maxNewWingInclination = maxInclinationWing - Math.abs(rollInclination);
					if (Math.abs(function5.value(minNewWingInclination)) < Math.abs(function5.value(maxNewWingInclination))) {
						leftWingInclination = (float) (minNewWingInclination + Math.toRadians(correctionFactor) - rollInclination);
						rightWingInclination = (float) (minNewWingInclination + Math.toRadians(correctionFactor) + rollInclination);
					}
					else {
						leftWingInclination = (float) (maxNewWingInclination + Math.toRadians(correctionFactor) - rollInclination);
						rightWingInclination = (float) (maxNewWingInclination + Math.toRadians(correctionFactor) + rollInclination);
					}
				}
				
				UnivariateFunction function6 = (x)->{return drone.transformationToDroneCoordinates(drone.getLiftForceHorStab((float)x)).getEntry(1)*drone.getTailSize()
						+ drone.transformationToDroneCoordinates(drone.getLiftForceVerStab(verStabInclinationTemp)).getEntry(1)*drone.getTailSize()
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
					horStabInclination = (float) solution6;
				} catch (NoBracketingException exc1) {
					if (Math.abs(function6.value(minInclinationHorStab)) < Math.abs(function6.value(maxInclinationHorStab)))
						horStabInclination = (float) (minInclinationHorStab + Math.toRadians(correctionFactor));
					else {
						horStabInclination = (float) (maxInclinationHorStab - Math.toRadians(correctionFactor));
					}
				}
				
				float zVelocity = (float) drone.transformationToDroneCoordinates(drone.getVelocity()).getEntry(2);
				final float acceleration = (targetVelocity - zVelocity)/turningTime;
				
				thrust = (float) (drone.transformationToDroneCoordinates(drone.getLiftForceHorStab(horStabInclination)).getEntry(2)
						+ drone.transformationToDroneCoordinates(drone.getLiftForceLeftWing(leftWingInclination)).getEntry(2)
						+ drone.transformationToDroneCoordinates(drone.getLiftForceRightWing(rightWingInclination)).getEntry(2)
						+ drone.transformationToDroneCoordinates(drone.getLiftForceVerStab(verStabInclination)).getEntry(2)
						+ drone.transformationToDroneCoordinates(drone.getTotalGravitationalForce()).getEntry(2)
						- drone.getTotalMass()*acceleration);
				if (thrust > drone.getMaxThrust())
					thrust = drone.getMaxThrust();
				else if (thrust < 0)
					thrust = 0;
				
				this.setPreviousHeadingAngularAccelerationError(
						drone.getAngularAccelerations(leftWingInclination, rightWingInclination, horStabInclination, verStabInclination,
								frontBrakeForce, leftBrakeForce, rightBrakeForce)[0]
						-headingAngularAcceleration);
				this.setPreviousPitchAngularAccelerationError(
						drone.getAngularAccelerations(leftWingInclination, rightWingInclination, horStabInclination, verStabInclination,
								frontBrakeForce, leftBrakeForce, rightBrakeForce)[1]
						-pitchAngularAcceleration);
				this.setPreviousRollAngularAccelerationError(
						drone.getAngularAccelerations(leftWingInclination, rightWingInclination, horStabInclination, verStabInclination,
								frontBrakeForce, leftBrakeForce, rightBrakeForce)[2]
						-rollAngularAcceleration);
				this.setPreviousYAccelerationError(
						((float)drone.getAcceleration(thrust, leftWingInclination, rightWingInclination, horStabInclination, verStabInclination,
								frontBrakeForce, leftBrakeForce, rightBrakeForce).getEntry(1))
						-yAcceleration);
				RealMatrix transformationMatrix = this.calculateTransformationMatrix(targetRoll, drone);
				this.setPreviousXAccelerationError(
						(float) ((transformationMatrix.operate(drone.transformationToDroneCoordinates(drone.getLiftForceHorStab(horStabInclination))).getEntry(0) 
								+ transformationMatrix.operate(new ArrayRealVector(new double[] {0, 0, -thrust}, false)).getEntry(0))
								/drone.getTotalMass()));
				
				if ((this.getFlightMode() == FlightMode.FULL_FLIGHT) && (drone.getWorldPosition().getEntry(2) < (this.getTarget().getEntry(2)+1)))
					this.setFlightMode(FlightMode.LAND);
			}
        } if (this.getFlightMode() == FlightMode.TAXI) {		
			RealVector targetPosition = new ArrayRealVector(targetPositionCoordinates);
			RealVector targetPositionDroneCoordinates = drone.transformationToDroneCoordinates(targetPosition.subtract(drone.getWorldPosition()));
			double droneVelocity = drone.getVelocity().getNorm();
			float targetHeading = (float) Math.atan(targetPositionDroneCoordinates.getEntry(0)/targetPositionDroneCoordinates.getEntry(2));
			if (targetPositionDroneCoordinates.getEntry(2) > 0 && targetHeading > 0) {
				targetHeading -= Math.PI;
			}
			if (targetPositionDroneCoordinates.getEntry(2) > 0 && targetHeading < 0) {
				targetHeading += Math.PI;
			}
			if (targetPositionDroneCoordinates.getEntry(0) == 0 && targetPositionDroneCoordinates.getEntry(2) < 0) {
				thrust = 200;
			}
			else if ((- targetPositionDroneCoordinates.getEntry(2) < Math.tan(Math.PI/3) * Math.abs(targetPositionDroneCoordinates.getEntry(0)) && targetPositionDroneCoordinates.getEntry(2) <= 0) || targetPositionDroneCoordinates.getEntry(2) > 0){
//				System.out.println("SCHERPE BOCHT");
				if (drone.getVelocity().getNorm() > 3)
					thrust = 0;
				else
					thrust = 1500;
				if (targetPositionDroneCoordinates.getEntry(0) < 0)
					leftBrakeForce = 500;
				else
					rightBrakeForce = 500;
			}else {
//				System.out.println("GEEN SCHERPE BOCHT");
				float rotationNecessary = targetHeading;
				if (rotationNecessary > 2* Math.PI)
					rotationNecessary -= 2* Math.PI;
				if (rotationNecessary > 0 && rotationNecessary < Math.PI) {
//					System.out.println("BOCHT NAAR LINKS");
					if (rotationNecessary > Math.PI/18) {
						leftBrakeForce = (float) Math.abs(500*targetPositionDroneCoordinates.getEntry(0)/targetPositionDroneCoordinates.getEntry(2));
						rightBrakeForce = 0;
						thrust = Math.min(2000,2.5f * drone.getAngularAccelerations(0, 0, 0, 0, 0, Math.min(4300,leftBrakeForce), 0)[0] * drone.getTotalMass() + leftBrakeForce + 200);
					}
					else if (rotationNecessary < Math.PI/18 && drone.getHeadingAngularVelocity() > rotationNecessary/10) {
						leftBrakeForce = 0;
						rightBrakeForce = 0;
						thrust = 0;
					}
					else if (rotationNecessary < Math.PI/18 && drone.getHeadingAngularVelocity() < rotationNecessary/10) {
						leftBrakeForce = (float) Math.abs(500*targetPositionDroneCoordinates.getEntry(0)/targetPositionDroneCoordinates.getEntry(2));
						rightBrakeForce = 0;
						thrust = Math.min(2000,2.5f * drone.getAngularAccelerations(0, 0, 0, 0, 0, Math.min(4300,leftBrakeForce), 0)[0] * drone.getTotalMass() + leftBrakeForce + 200);
					}
				}
				else if (rotationNecessary > - Math.PI && rotationNecessary < 0) {
//					System.out.println("BOCHT NAAR RECHTS");
					if (rotationNecessary > - Math.PI/18 && drone.getHeadingAngularVelocity() < rotationNecessary/10) {
						rightBrakeForce = 0;
						leftBrakeForce = 0;
						thrust = 0;
					}
					else if (rotationNecessary > - Math.PI/18 && drone.getHeadingAngularVelocity() < Math.abs((double) rotationNecessary)/10) {
						rightBrakeForce = (float) Math.abs(1000*targetPositionDroneCoordinates.getEntry(0)/targetPositionDroneCoordinates.getEntry(2));
						leftBrakeForce = 0;
						thrust = Math.min(2000,2.5f * drone.getAngularAccelerations(0, 0, 0, 0, 0, 0, Math.min(4300,rightBrakeForce))[0] * drone.getTotalMass() + rightBrakeForce + 500);
					}
					else if (rotationNecessary < - Math.PI/18){
						rightBrakeForce = (float) Math.abs(1000*targetPositionDroneCoordinates.getEntry(0)/targetPositionDroneCoordinates.getEntry(2));
						leftBrakeForce = 0;
						thrust = Math.min(2000,2.5f*drone.getAngularAccelerations(0, 0, 0, 0, 0, 0, Math.min(4300,rightBrakeForce))[0]*drone.getTotalMass() + rightBrakeForce + 500);
					}
				}
				int maxVelocity;
				if (targetPositionDroneCoordinates.getNorm() > 1500)
					maxVelocity = 30;
				else if (targetPositionDroneCoordinates.getNorm() > 500)
					maxVelocity = 20;
				else if (targetPositionDroneCoordinates.getNorm() > 200)
					maxVelocity = 10;
				else if (targetPositionDroneCoordinates.getNorm() > 100)
					maxVelocity = 5;
				else
					maxVelocity = 3;
				if (drone.getVelocity().getNorm() > maxVelocity) {
					thrust = 0;
				}
			}
			int factor;
			if (droneVelocity < 10)
				factor = 1;
			else if (droneVelocity < 20)
				factor = 2;
			else if (droneVelocity < 30)
				factor = 3;
			else
				factor = 4;
			if ((targetPositionDroneCoordinates.getNorm() <= 20 + factor * drone.getVelocity().getNorm())){
				thrust = 0;
				if (droneVelocity >= 3) {
					float breakforce = Math.min( 8600, (float) (droneVelocity*droneVelocity*480)/(2*(float)targetPositionDroneCoordinates.getNorm()));
					rightBrakeForce += breakforce/2f;
					rightBrakeForce = Math.min(rightBrakeForce, 4300);
					leftBrakeForce += breakforce/2f;
					leftBrakeForce = Math.min(leftBrakeForce, 4300);
				}
				if (targetPositionDroneCoordinates.getNorm() <= 3) {
					if (droneVelocity >= 1) {
						float breakforce = Math.min( 8600, (float) (droneVelocity*droneVelocity*480)/(2*(float)targetPositionDroneCoordinates.getNorm()));
						rightBrakeForce += breakforce/2f;
						rightBrakeForce = Math.min(rightBrakeForce, 4300);
						leftBrakeForce += breakforce/2f;
						leftBrakeForce = Math.min(leftBrakeForce, 4300);
					}
					if (droneVelocity < 1) {
						thrust = Math.min(2000,2.5f * drone.getAngularAccelerations(0, 0, 0, 0, 0, Math.min(4300,Math.abs(leftBrakeForce - rightBrakeForce)), 0)[0] * drone.getTotalMass() + leftBrakeForce + rightBrakeForce);
						if (targetPositionDroneCoordinates.getNorm() < 2)
							System.out.println("SUCCES");
					}
				}
				if (droneVelocity < 1) {
					thrust = Math.min(2000,2.5f * drone.getAngularAccelerations(0, 0, 0, 0, 0, Math.min(4300,leftBrakeForce), Math.min(4300, rightBrakeForce))[0] * drone.getTotalMass() + leftBrakeForce + rightBrakeForce);
//					rightBrakeForce = 0;
//					leftBrakeForce = 0;
//					if (targetPositionDroneCoordinates.getNorm() < 5)
//						System.out.println("SUCCES");
				}
			}
			thrust = Math.max(0, thrust);
//			System.out.println("");
        }

        final float thrustOutput = thrust;
        final float leftWingInclinationOutput = leftWingInclination;
        final float rightWingInclinationOutput = rightWingInclination;
        final float horStabInclinationOutput = horStabInclination;
        final float verStabInclinationOutput = verStabInclination;
        final float leftBrakeForceOutput = leftBrakeForce;
        final float rightBrakeForceOutput = rightBrakeForce;
        final float frontBrakeForceOutput = frontBrakeForce;
        
		AutopilotOutputs output = new AutopilotOutputs() {
			public float getThrust() {
				return thrustOutput;
			}
			public float getLeftWingInclination() {
				return leftWingInclinationOutput;
			}
			public float getRightWingInclination() {
				return rightWingInclinationOutput;
			}
			public float getHorStabInclination() {
				return horStabInclinationOutput;
			}
			public float getVerStabInclination() {
				return verStabInclinationOutput;
			}

			public float getFrontBrakeForce() {
				return frontBrakeForceOutput;
			}

			public float getLeftBrakeForce() {
				return leftBrakeForceOutput;
			}

			public float getRightBrakeForce() {
				return rightBrakeForceOutput;
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

	public FlightMode getFlightMode() {
		return this.currentFlightMode;
	}
	
	public void setFlightMode(FlightMode mode) throws IllegalArgumentException {
		this.currentFlightMode = mode;
	}
	

	private int mode = 3;
	private RealVector target = null;

	
	public RealVector getTarget() {
		return this.target;
	}
	
	public static boolean isValidTarget(RealVector target) {
		return (target != null);
	}
	
	public void setTarget(RealVector target) throws IllegalArgumentException {
		if (! isValidTarget(target))
			throw new IllegalArgumentException();
		this.target = target;
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
			if ((AOALeftWing > this.getConfig().getMaxAOA()) || (AOALeftWing < -this.getConfig().getMaxAOA()))
				crash = true;
		}
		if ((airfoil == 1) && (crash == false)) {
			float AOARightWing = drone.calculateAOA(drone.getNormalHor(inclination),
					drone.getProjectedVelocityRightWing(), drone.getAttackVectorHor(inclination));
			if ((AOARightWing > this.getConfig().getMaxAOA()) || (AOARightWing < -this.getConfig().getMaxAOA()))
				crash = true;
		}
		if ((airfoil == 2) && (crash == false)) {
			float AOAHorStab = drone.calculateAOA(drone.getNormalHor(inclination),
					drone.getProjectedVelocityHorStab(), drone.getAttackVectorHor(inclination));
			if ((AOAHorStab > this.getConfig().getMaxAOA()) || (AOAHorStab < -this.getConfig().getMaxAOA()))
				crash = true;
		}
		if ((airfoil == 3) && (crash == false)) {
			float AOAVerStab = drone.calculateAOA(drone.getNormalVer(inclination),
					drone.getProjectedVelocityVerStab(), drone.getAttackVectorVer(inclination));
			if ((AOAVerStab > this.getConfig().getMaxAOA()) || (AOAVerStab < -this.getConfig().getMaxAOA()))
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
		RealVector initialVelocity = new ArrayRealVector(new double[] {0, 0, 0}, false);
		Drone drone = new Drone(config, initialVelocity);
		drone.setRelativePosition(new ArrayRealVector(new double[] {0, -config.getWheelY() + config.getTyreRadius(), 0}, false));
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
