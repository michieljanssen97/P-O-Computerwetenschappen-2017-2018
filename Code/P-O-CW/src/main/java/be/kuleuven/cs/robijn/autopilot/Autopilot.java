package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.linear.*;

import be.kuleuven.cs.robijn.autopilot.Autopilot;
import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.common.exceptions.FinishedException;
import be.kuleuven.cs.robijn.common.math.Angle;
import be.kuleuven.cs.robijn.common.math.Angle.Type;
import be.kuleuven.cs.robijn.common.math.ScalarMath;
import be.kuleuven.cs.robijn.experiments.ExpPosition;
import be.kuleuven.cs.robijn.tyres.FrontWheel;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import interfaces.*;
import java.util.*;

/**
 * A class of autopilots.
 * 
 * @author Pieter Vandensande
 */
public class Autopilot {
	private static boolean drawChartPositions = false;
	public static ExpPosition exppos = new ExpPosition();
	public FlightMode currentFlightMode = FlightMode.READY;
	
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
	
	private Targets targets = new Targets();
	
	public void setTargets(RealVector[] targets) {
		this.targets.setTargets(targets);
	}
	
	private RealVector targetPosition = null;
	
	public RealVector getTargetPosition() {
		return targetPosition;
	}
	
	public void setTargetPosition(RealVector targetPosition) {
		this.targetPosition = targetPosition;
	}
	
	private float targetHeading = 0;
	
	public float getTargetHeading() {
		return targetHeading;
	}
	
	public void setTargetHeading(float targetHeading) {
		this.targetHeading = targetHeading;
	}
	
	private WorldObject world;
	private List<Drone> drones = new ArrayList<>();


	public Drone[] calculateFirstDroneCollision(){
		world = drone.getParent();
		drones = world.getChildrenOfType(Drone.class);
		Map<Drone[], Double> collisionDroneMap = new HashMap<>();
		Double collisionTime;

		for(int i = 0; i < drones.size(); i++){
			for(int j = i+1; j < drones.size(); j++){

				double deltaPosX = drones.get(i).getWorldPosition().getEntry(0)-drones.get(j).getWorldPosition().getEntry(0);
				double deltaPosY = drones.get(i).getWorldPosition().getEntry(1)-drones.get(j).getWorldPosition().getEntry(1);
				double deltaPosZ = drones.get(i).getWorldPosition().getEntry(2)-drones.get(j).getWorldPosition().getEntry(2);


				double deltaVelX = drones.get(i).getVelocity().getEntry(0)-drones.get(j).getVelocity().getEntry(0);
				double deltaVelY = drones.get(i).getVelocity().getEntry(1)-drones.get(j).getVelocity().getEntry(1);
				double deltaVelZ = drones.get(i).getVelocity().getEntry(2)-drones.get(j).getVelocity().getEntry(2);

				double deltaRR = Math.pow(deltaPosX, 2) + Math.pow(deltaPosY, 2) + Math.pow(deltaPosZ, 2);
				double deltaVV = Math.pow(deltaVelX, 2) + Math.pow(deltaVelY, 2) + Math.pow(deltaVelZ, 2);
				double deltaVR = (deltaVelX*deltaPosX)  + (deltaVelY*deltaPosY) +  (deltaVelZ*deltaPosZ);
				double d = Math.pow(deltaVR, 2) - (deltaVV)*(deltaRR - Math.pow(5, 2));


				if (deltaVR >= 0){
					collisionTime =  Double.POSITIVE_INFINITY;
				} else if (d < 0){
					collisionTime = Double.POSITIVE_INFINITY;
				} else {
					collisionTime =  - ((deltaVR + Math.sqrt(d))/(deltaVV));
				}
				if (collisionTime != Double.POSITIVE_INFINITY) {
					Drone[] collisionArray = {drones.get(i), drones.get(j)};
					collisionDroneMap.put(collisionArray, collisionTime);
				}
			}
		}
		if (collisionDroneMap.isEmpty()) {
			return null;
		} else {
			Drone[] firstCollisionDroneArray = Collections.min(collisionDroneMap.entrySet(), Map.Entry.comparingByValue()).getKey();
			return firstCollisionDroneArray;
		}
	}
	
	/**
	 * Wel roll ten gevolge van verschillende snelheid van vleugels (door de rotaties). 
	 */
	public AutopilotOutputs timePassed(AutopilotInputs inputs) throws IllegalStateException, FinishedException {
		if (this.isFirstUpdate())
			this.firstUpdate = false;
		else {
			this.moveDrone(inputs.getElapsedTime() - this.getPreviousElapsedTime(), inputs);
		}
        this.setPreviousElapsedTime(inputs.getElapsedTime());  
        float horStabInclination = 0;
		float verStabInclination = 0;
		float leftWingInclination = 0;
		float rightWingInclination = 0;
		float thrust = 0;
		float leftBrakeForce = 0;
		float rightBrakeForce = 0;
		float frontBrakeForce = 0;
		
		AutopilotSettings settings = new AutopilotSettings();
		if (this.getFlightMode() == FlightMode.ASCEND) { //ascend
			
			thrust = this.getConfig().getMaxThrust();
			
			float takeOffSpeed = (float) Math.sqrt((-drone.getTotalGravitationalForce().getEntry(1))
					/(2*this.getConfig().getMaxAOA()*this.getConfig().getWingLiftSlope()*Math.cos(this.getConfig().getMaxAOA())));
			
			float maxPitch = (float) Math.asin(drone.getWorldPosition().getEntry(1)/this.getConfig().getTailSize());
			maxPitch = ScalarMath.upperBoundary(
					(float) (maxPitch - Math.toRadians(settings.getCorrectionFactor())),
					settings.getPitchTakeOff(),
					true
					);
			
			Angle XRotationVel = Angle.getXRotation(drone.getVelocity().add(drone.getWorldPosition()), drone.getWorldPosition());
			XRotationVel = ScalarMath.upperBoundary(XRotationVel, maxPitch, false);
			XRotationVel = Angle.add(XRotationVel, -drone.getPitch());
			
			float targetPitchAngularVelocity = ScalarMath.betweenBoundaries(
					XRotationVel.getOrientation()/settings.getTurningTime(),
					settings.getMaxPitchAngularVelocity()
					);
			float pitchAngularVelocity = drone.getPitchAngularVelocity();
			float pitchAngularAcceleration = ScalarMath.betweenBoundaries(
					(targetPitchAngularVelocity - pitchAngularVelocity)/settings.getTurningTime(),
					settings.getMaxPitchAngularAcceleration()
					);
			if (! Float.isNaN(this.getPreviousPitchAngularAccelerationError()))
				pitchAngularAcceleration -= this.getPreviousPitchAngularAccelerationError();
			
			FunctionCalculator functionCalculator = new FunctionCalculator(drone, 0, pitchAngularAcceleration, 0, 0, 0);
			EquationSolver solver = new EquationSolver(drone, this.getConfig(), functionCalculator);
			
			if (Math.abs(drone.transformationToDroneCoordinates(drone.getVelocity()).getEntry(2)) > takeOffSpeed) {
				leftWingInclination = (float) (solver.getMaxInclinationWing() - Math.toRadians(settings.getCorrectionFactor()));
				rightWingInclination = (float) (solver.getMaxInclinationWing() - Math.toRadians(settings.getCorrectionFactor()));
				
				horStabInclination = solver.solverForPitch(true);
				
				this.setPreviousPitchAngularAccelerationError(
						drone.getAngularAccelerations(leftWingInclination, rightWingInclination, horStabInclination, verStabInclination,
								frontBrakeForce, leftBrakeForce, rightBrakeForce)[1]
						-pitchAngularAcceleration);
			}
			
			if (drone.getWorldPosition().getEntry(1) > this.getConfig().getTailSize()) {
				drone.setTookOff();
				this.setFlightMode(FlightMode.FULL_FLIGHT);
			}
		}

		else if ((this.getFlightMode() == FlightMode.FULL_FLIGHT) || (this.getFlightMode() == FlightMode.LAND)) {
				
			FrontWheel wheel = drone.getFirstChildOfType(FrontWheel.class);
			if (this.getConfig().getTyreRadius() >= wheel.getPosition(drone).getEntry(1)) {
				this.setFlightMode(FlightMode.BRAKE);
			}
			else {
				boolean finished = false;
				int iterations = 1;
				while ((! finished) && (iterations < 5)) {
					iterations += 1;
					finished = true;
					
					//RealVector target = new ArrayRealVector(new double[] {0, 50, -1000}, false);
					RealVector target = null;
					if (this.getFlightMode() == FlightMode.FULL_FLIGHT) {
						this.targets.setDronePosition(drone.getWorldPosition());
						target = this.targets.getFirstTarget(drone);
					}
					if (target == null)
						this.setFlightMode(FlightMode.LAND);
					
//					Angle angle = new Angle((float) Math.atan(drone.getWorldPosition().getEntry(2)/(drone.getWorldPosition().getEntry(0)-500)));
//					System.out.println(angle.getOrientation());
//					target = new ArrayRealVector(new double[] {Math.sin(angle.getOrientation()), 0, -Math.cos(angle.getOrientation())}, false);
//					if (drone.getWorldPosition().getEntry(0) > 500)
//						target = new ArrayRealVector(new double[] {-Math.sin(angle.getOrientation()), 0, Math.cos(angle.getOrientation())}, false);
//					float cor = 0;
//					if (new ArrayRealVector(new double[] {drone.getWorldPosition().getEntry(0), 0, drone.getWorldPosition().getEntry(2)}, false)
//							.getDistance(new ArrayRealVector(new double[] {500, 0, 0}, false)) > 500)
//						cor = (float) Math.toRadians(0);
//					else if (new ArrayRealVector(new double[] {drone.getWorldPosition().getEntry(0), 0, drone.getWorldPosition().getEntry(2)}, false)
//							.getDistance(new ArrayRealVector(new double[] {500, 0, 0}, false)) < 500)
//						cor = (float) Math.toRadians(0);
//					target = new ArrayRealVector(new double[] {target.getEntry(0)*Math.cos(cor) - target.getEntry(2)*Math.sin(cor),
//								100, target.getEntry(2)*Math.cos(cor) + target.getEntry(0)*Math.sin(cor)}, false);
				
					if (this.getFlightMode() == FlightMode.LAND) {
						if (drone.getWorldPosition().getEntry(1) >= 5*settings.getHeight()) {
							RealVector vector = drone.getVelocity();
							vector.setEntry(1, 0);
							float length = (float) vector.getNorm();
							vector = drone.getVelocity();
							vector.setEntry(1, -Math.tan(Math.toRadians(10))*length);
							target = drone.getWorldPosition().add(vector);
						}
						else if ((settings.getHeight() <= drone.getWorldPosition().getEntry(1)) && (drone.getWorldPosition().getEntry(1) < 5*settings.getHeight())) {
							RealVector vector = drone.getVelocity();
							vector.setEntry(1, 0);
							float length = (float) vector.getNorm();
							vector = drone.getVelocity();
							vector.setEntry(1, -Math.tan(Math.toRadians(5))*length);
							target = drone.getWorldPosition().add(vector);
						}
						else {
							RealVector vector = drone.getVelocity();
							vector.setEntry(1, 0);
							float length = (float) vector.getNorm();
							vector = drone.getVelocity();
							vector.setEntry(1, -Math.tan(Math.toRadians(1))*length);
							target = drone.getWorldPosition().add(vector);
						}
					}
					
					Angle XRotation = Angle.getXRotation(target, drone.getWorldPosition());
					Angle YRotation = Angle.getYRotation(target, drone.getWorldPosition());
					
					XRotation = Angle.add(XRotation, - drone.getPitch());
					YRotation = Angle.add(YRotation, - drone.getHeading());

					RealVector vel = drone.getVelocity().add(drone.getWorldPosition());
					Angle YRotationVel = Angle.getYRotation(vel, drone.getWorldPosition());
					
					YRotationVel = Angle.add(YRotationVel, - drone.getHeading());
					
//					if ((Math.abs(YRotation.getOrientation(Type.DEGREES)) > 30) && (drone.getWorldPosition().getDistance(target) < 500)
//							&& (Math.abs(drone.getRoll()) < Math.toRadians(3))) {
//						System.out.println("test2");
//						RealVector newTarget = drone.getVelocity().add(drone.getWorldPosition());
//						if (newTarget.getNorm() != 0)
//							newTarget = newTarget.mapMultiply(1/newTarget.getNorm());
//						newTarget.mapMultiply(750);
//						newTarget.setEntry(1, target.getEntry(1));
//						target = newTarget;
//						RealVector[] tars = this.targets.getTargets();
//						RealVector[] newTars = new RealVector[tars.length+1];
//						for (int i = 0; i < tars.length; i++) {
//							newTars[i+1] = tars[i];
//							if (i == 0)
//								newTars[i] = target;
//						}
//							
//						XRotation = Angle.getXRotation(target, drone.getWorldPosition());
//						YRotation = Angle.getYRotation(target, drone.getWorldPosition());
//						
//						XRotation = Angle.add(XRotation, - drone.getPitch());
//						YRotation = Angle.add(YRotation, - drone.getHeading());
//					}
					
					float targetHeadingAngularVelocity = ScalarMath.betweenBoundaries(
							YRotationVel.getOrientation()/settings.getTurningTime(),
							settings.getMaxHeadingAngularVelocity()
							);
					float headingAngularVelocity = drone.getHeadingAngularVelocity();
					float headingAngularAcceleration = ScalarMath.betweenBoundaries(
							(targetHeadingAngularVelocity - headingAngularVelocity)/settings.getTurningTime(),
							settings.getMaxHeadingAngularAcceleration()
							);
					if (! Float.isNaN(this.getPreviousHeadingAngularAccelerationError()))
						headingAngularAcceleration -= this.getPreviousHeadingAngularAccelerationError();
					
					float targetPitchAngularVelocity = ScalarMath.betweenBoundaries(
							XRotation.getOrientation()/settings.getTurningTime(),
							settings.getMaxPitchAngularVelocity()
							);
					float pitchAngularVelocity = drone.getPitchAngularVelocity();
					float pitchAngularAcceleration = ScalarMath.betweenBoundaries(
							(targetPitchAngularVelocity - pitchAngularVelocity)/settings.getTurningTime(),
							settings.getMaxPitchAngularAcceleration()
							);
					if (! Float.isNaN(this.getPreviousPitchAngularAccelerationError()))
						pitchAngularAcceleration -= this.getPreviousPitchAngularAccelerationError();
					
					float targetYVelocity = ScalarMath.betweenBoundaries(
							(float) (-drone.transformationToDroneWithoutRollCoordinates(drone.getVelocity()).getEntry(2)*Math.tan(XRotation.getOrientation())),
							settings.getMaxYVelocity()
							);
					float yVelocity = (float) drone.transformationToDroneWithoutRollCoordinates(drone.getVelocity()).getEntry(1);
					float yAcceleration = ScalarMath.betweenBoundaries(
							(targetYVelocity - yVelocity)/settings.getTurningTime(),
							settings.getMaxYAcceleration()
							);
					if (! Float.isNaN(this.getPreviousYAccelerationError()))
						yAcceleration -= this.getPreviousYAccelerationError();
					
					float targetXVelocity = ScalarMath.betweenBoundaries(
							(float) (drone.transformationToDroneWithoutRollCoordinates(drone.getVelocity()).getEntry(2)*Math.tan(YRotation.getOrientation())),
							settings.getMaxXVelocity()
							);
					float xVelocity = (float)drone.transformationToDroneWithoutRollCoordinates(drone.getVelocity()).getEntry(0);
					float xAcceleration = ScalarMath.betweenBoundaries(
							(targetXVelocity - xVelocity)/settings.getXMovementTime(),
							settings.getMaxXAcceleration()
							);
					if (! Float.isNaN(this.getPreviousXAccelerationError()))
						xAcceleration -= this.getPreviousXAccelerationError();
					
					FunctionCalculator functionCalculator = new FunctionCalculator(drone, headingAngularAcceleration, pitchAngularAcceleration, 0,
							xAcceleration, yAcceleration);
					EquationSolver solver = new EquationSolver(drone, this.getConfig(), functionCalculator);
					
					verStabInclination = solver.solverForHeading();
					if (Float.isNaN(verStabInclination)) {
						finished = false;
						settings.setTurningtime(settings.getTurningTime() + 0.5f);
						settings.setXMovementTime(settings.getXMovementTime() + 0.5f);
						if (iterations != 5)
							continue;
						if (Math.abs(functionCalculator.functionForHeading().value(solver.getMinInclinationVerStab())) 
								< Math.abs(functionCalculator.functionForHeading().value(solver.getMaxInclinationVerStab())))
							verStabInclination = (float) (solver.getMinInclinationVerStab() + Math.toRadians(settings.getCorrectionFactor()));
						else {
							verStabInclination = (float) (solver.getMaxInclinationVerStab() - Math.toRadians(settings.getCorrectionFactor()));
						}
					}
					
					float wingInclination = solver.solverForYVelocity(verStabInclination, false);
					if (Float.isNaN(wingInclination)) {
						finished = false;
						settings.setTurningtime(settings.getTurningTime() + 0.5f);
						settings.setXMovementTime(settings.getXMovementTime() + 0.5f);
						if (iterations != 5)
							continue;
						if (Math.abs(functionCalculator.functionForYVelocity(verStabInclination).value(solver.getMinInclinationWing())) 
								< Math.abs(functionCalculator.functionForYVelocity(verStabInclination).value(solver.getMaxInclinationWing())))
							wingInclination = (float) (solver.getMinInclinationWing() + Math.toRadians(settings.getCorrectionFactor()));
						else {
							wingInclination = (float) (solver.getMaxInclinationWing() - Math.toRadians(settings.getCorrectionFactor()));
						}
					}
					
					Angle targetRoll;
					Angle roll = new Angle(drone.getRoll());
//					if ((Math.abs(YRotation.getOrientation(Type.DEGREES)) > 30) && (drone.getWorldPosition().getDistance(target) < 500)) {
//						System.out.println("test");
//						targetRoll = new Angle(0, Type.DEGREES);
//					}
					if ((YRotation.getOrientation(Type.DEGREES) > 30) && (roll.getOrientation(Type.DEGREES) < 0))
						targetRoll = new Angle(0, Type.DEGREES);
					else if ((YRotation.getOrientation(Type.DEGREES) > 30) && (roll.getOrientation(Type.DEGREES) < 20))
						targetRoll = new Angle(20, Type.DEGREES);
					else if (YRotation.getOrientation(Type.DEGREES) > 30)
						targetRoll = new Angle(40, Type.DEGREES);
					else if ((YRotation.getOrientation(Type.DEGREES) < -30) && (roll.getOrientation(Type.DEGREES) > 0))
						targetRoll = new Angle(0, Type.DEGREES);
					else if ((YRotation.getOrientation(Type.DEGREES) < -30) && (roll.getOrientation(Type.DEGREES) > -20))
						targetRoll = new Angle(-20, Type.DEGREES);
					else if (YRotation.getOrientation(Type.DEGREES) < -30)
						targetRoll = new Angle(-40, Type.DEGREES);
					else if (YRotation.getOrientation(Type.DEGREES) > 20)
						targetRoll = new Angle(20, Type.DEGREES);
					else if (YRotation.getOrientation(Type.DEGREES) < -20)
						targetRoll = new Angle(-20, Type.DEGREES);
					else {
						targetRoll = new Angle(solver.solverForXVelocity(verStabInclination, wingInclination));
						if (Float.isNaN(targetRoll.getOrientation())) {
							finished = false;
							settings.setTurningtime(settings.getTurningTime() + 0.5f);
							settings.setXMovementTime(settings.getXMovementTime() + 0.5f);
							if (iterations != 5)
								continue;
							targetRoll = new Angle(drone.getRoll());
						}
				        if ((targetRoll.getAngle() > settings.getMaxRoll()) && (targetRoll.getAngle() < Math.PI))
				        	targetRoll = new Angle(settings.getMaxRoll());
				        else if ((targetRoll.getAngle() > Math.PI) && (targetRoll.getAngle() < (2*Math.PI - settings.getMaxRoll())))
				        	targetRoll = new Angle((float) (2*Math.PI - settings.getMaxRoll()));
				        targetRoll = new Angle(ScalarMath.betweenBoundaries(targetRoll.getOrientation(Type.DEGREES), 10), Type.DEGREES);
					}
					
			        Angle rollDifference = new Angle(targetRoll.getOrientation() - drone.getRoll());
			        rollDifference = new Angle(ScalarMath.betweenBoundaries(rollDifference.getOrientation(Type.DEGREES), 20), Type.DEGREES);
					
					float targetRollAngularVelocity = ScalarMath.betweenBoundaries(
							rollDifference.getOrientation()/settings.getTurningTime(),
							settings.getMaxRollAngularVelocity()
							);
					float rollAngularVelocity = drone.getRollAngularVelocity();
					float rollAngularAcceleration = ScalarMath.betweenBoundaries(
							(targetRollAngularVelocity - rollAngularVelocity)/settings.getTurningTime(),
							settings.getMaxRollAngularAcceleration()
							);
					if (! Float.isNaN(this.getPreviousRollAngularAccelerationError()))
						rollAngularAcceleration -= this.getPreviousRollAngularAccelerationError();
					
					functionCalculator.setRollAngularAcceleration(rollAngularAcceleration);
					solver = new EquationSolver(drone, this.getConfig(), functionCalculator);
					
					Angle rollInclination = new Angle(solver.solverForRoll(wingInclination));
					if (Float.isNaN(rollInclination.getOrientation())) {
						finished = false;
						settings.setTurningtime(settings.getTurningTime() + 0.5f);
						settings.setXMovementTime(settings.getXMovementTime() + 0.5f);
						if (iterations != 5)
							continue;
						float minRollInclination = Math.max(solver.getMinInclinationWing() - wingInclination, wingInclination - solver.getMaxInclinationWing());
						float maxRollInclination = Math.min(solver.getMaxInclinationWing() - wingInclination, wingInclination - solver.getMinInclinationWing());
						if (Math.abs(functionCalculator.functionForRoll(wingInclination).value(minRollInclination)) 
								< Math.abs(functionCalculator.functionForRoll(wingInclination).value(maxRollInclination)))
							rollInclination = new Angle(-3, Type.DEGREES);
						else {
							rollInclination = new Angle(3, Type.DEGREES);
						}
					}
					
					float solution = solver.solverForYVelocityWithRoll(verStabInclination, rollInclination.getOrientation(), false);
					if (Float.isNaN(solution)) {
						finished = false;
						settings.setTurningtime(settings.getTurningTime() + 0.5f);
						settings.setXMovementTime(settings.getXMovementTime() + 0.5f);
						if (iterations != 5)
							continue;
						float minNewWingInclination = solver.getMinInclinationWing() + Math.abs(rollInclination.getOrientation());
						float maxNewWingInclination = solver.getMaxInclinationWing() - Math.abs(rollInclination.getOrientation());
						if (Math.abs(functionCalculator.functionForYVelocityWithRoll(verStabInclination, rollInclination.getOrientation())
								.value(minNewWingInclination)) 
								< Math.abs(functionCalculator.functionForYVelocityWithRoll(verStabInclination, rollInclination.getOrientation())
										.value(maxNewWingInclination))) {
							solution = (float) (minNewWingInclination + Math.toRadians(settings.getCorrectionFactor()));
						}
						else {
							solution = (float) (maxNewWingInclination - Math.toRadians(settings.getCorrectionFactor()));
						}
					}
					leftWingInclination = solution - rollInclination.getOrientation();
					rightWingInclination = solution + rollInclination.getOrientation();
					
					horStabInclination = solver.solverForPitch(false);
					if (Float.isNaN(horStabInclination)) {
						finished = false;
						settings.setTurningtime(settings.getTurningTime() + 0.5f);
						settings.setXMovementTime(settings.getXMovementTime() + 0.5f);
						if (iterations != 5)
							continue;
						if (Math.abs(functionCalculator.functionForPitch().value(solver.getMinInclinationHorStab())) 
								< Math.abs(functionCalculator.functionForPitch().value(solver.getMaxInclinationHorStab())))
							horStabInclination = (float) (solver.getMinInclinationHorStab() + Math.toRadians(settings.getCorrectionFactor()));
						else {
							horStabInclination = (float) (solver.getMaxInclinationHorStab() - Math.toRadians(settings.getCorrectionFactor()));
						}
					}
					
					float zVelocity = (float) drone.transformationToDroneCoordinates(drone.getVelocity()).getEntry(2);
					final float acceleration = (settings.getTargetVelocity() - zVelocity)/settings.getTurningTime();
					
					thrust = (float) (drone.transformationToDroneCoordinates(drone.getLiftForceHorStab(horStabInclination)).getEntry(2)
							+ drone.transformationToDroneCoordinates(drone.getLiftForceLeftWing(leftWingInclination)).getEntry(2)
							+ drone.transformationToDroneCoordinates(drone.getLiftForceRightWing(rightWingInclination)).getEntry(2)
							+ drone.transformationToDroneCoordinates(drone.getLiftForceVerStab(verStabInclination)).getEntry(2)
							+ drone.transformationToDroneCoordinates(drone.getTotalGravitationalForce()).getEntry(2)
							- drone.getTotalMass()*acceleration);
					thrust = ScalarMath.upperBoundary(thrust, drone.getMaxThrust(), false);
					thrust = ScalarMath.lowerBoundary(thrust, 0, false);
					
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
							((float)drone.transformationToDroneWithoutRollCoordinates(
									drone.getAcceleration(thrust, leftWingInclination, rightWingInclination, horStabInclination, verStabInclination,
									frontBrakeForce, leftBrakeForce, rightBrakeForce)).getEntry(1))
							-yAcceleration);
					RealMatrix transformationMatrix = this.calculateTransformationMatrix(targetRoll.getOrientation(), drone);
					this.setPreviousXAccelerationError(
							(float) ((drone.transformationToDroneWithoutRollCoordinates(
									transformationMatrix.operate(drone.transformationToDroneCoordinates(drone.getLiftForceHorStab(horStabInclination)))).getEntry(0) 
									+ drone.transformationToDroneCoordinates(transformationMatrix.operate(new ArrayRealVector(new double[] {0, 0, -thrust}, false))).getEntry(0))
									/drone.getTotalMass()));
				}
			}
        } if (this.getFlightMode() == FlightMode.TAXI) {
			RealVector targetPosition = this.getTargetPosition();
			RealVector targetPositionDroneCoordinates = drone.transformationToDroneCoordinates(targetPosition.subtract(drone.getWorldPosition()));
			double droneVelocity = drone.getVelocity().getNorm();
			float necessaryHeading = (float) Math.atan(targetPositionDroneCoordinates.getEntry(0)/targetPositionDroneCoordinates.getEntry(2));
			if (targetPositionDroneCoordinates.getEntry(2) > 0 && necessaryHeading > 0) {
				necessaryHeading -= Math.PI;

			}
			if (targetPositionDroneCoordinates.getEntry(2) > 0 && necessaryHeading < 0) {
				necessaryHeading += Math.PI;
			}
			if (targetPositionDroneCoordinates.getEntry(0) == 0 && targetPositionDroneCoordinates.getEntry(2) < 0) {
				thrust = 200;
			}
			else if ((- targetPositionDroneCoordinates.getEntry(2) < Math.tan(Math.PI/3) * Math.abs(targetPositionDroneCoordinates.getEntry(0)) && targetPositionDroneCoordinates.getEntry(2) <= 0) || targetPositionDroneCoordinates.getEntry(2) > 0){
				if (drone.getVelocity().getNorm() > 3) {
					thrust = 0;
				}
				else
					thrust = 1500;
				if (targetPositionDroneCoordinates.getEntry(0) < 0)
					leftBrakeForce = 500;
				else
					rightBrakeForce = 500;
			}else {
				float rotationNecessary = necessaryHeading;
				if (rotationNecessary > 2* Math.PI)
					rotationNecessary -= 2* Math.PI;
				if (rotationNecessary > 0 && rotationNecessary < Math.PI) {
					if (rotationNecessary > Math.PI/18) {
						leftBrakeForce = (float) Math.abs(1000*targetPositionDroneCoordinates.getEntry(0)/targetPositionDroneCoordinates.getEntry(2));
						rightBrakeForce = 0;
						thrust = Math.min(2000,2.5f * drone.getAngularAccelerations(0, 0, 0, 0, 0, Math.min(4300,leftBrakeForce), 0)[0] * drone.getTotalMass() + leftBrakeForce + 500);
					}
					else if (rotationNecessary < Math.PI/18 && drone.getHeadingAngularVelocity() > rotationNecessary/10) {
						leftBrakeForce = 0;
						rightBrakeForce = 0;
						thrust = 0;
					}
					else if (rotationNecessary < Math.PI/18 && drone.getHeadingAngularVelocity() < rotationNecessary/10) {
						leftBrakeForce = (float) Math.abs(1000*targetPositionDroneCoordinates.getEntry(0)/targetPositionDroneCoordinates.getEntry(2));
						rightBrakeForce = 0;
						thrust = Math.min(2000,2.5f * drone.getAngularAccelerations(0, 0, 0, 0, 0, Math.min(4300,leftBrakeForce), 0)[0] * drone.getTotalMass() + leftBrakeForce + 500);
					}
				}
				else if (rotationNecessary > - Math.PI && rotationNecessary < 0) {
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
				factor = 2;
			else if (droneVelocity < 20)
				factor = 4;
			else if (droneVelocity < 30)
				factor = 6;
			else
				factor = 8;
			if ((targetPositionDroneCoordinates.getNorm() <= factor * drone.getVelocity().getNorm())){
				thrust = 0;
				if (droneVelocity >= 3) {
					float breakforce = Math.min( 8600, (float) (droneVelocity*droneVelocity*480)/(2*(float)targetPositionDroneCoordinates.getNorm()));
					rightBrakeForce += breakforce/2f;
					rightBrakeForce = Math.min(rightBrakeForce, 4300);
					leftBrakeForce += breakforce/2f;
					leftBrakeForce = Math.min(leftBrakeForce, 4300);
				}
				if (targetPositionDroneCoordinates.getNorm() <= 5) {
					this.setFlightMode(FlightMode.BRAKE);
//					if (droneVelocity >= 1) {
//						float breakforce = Math.min( 8600, (float) (droneVelocity*droneVelocity*480)/(2*(float)targetPositionDroneCoordinates.getNorm()));
//						rightBrakeForce += breakforce/2f;
//						rightBrakeForce = Math.min(rightBrakeForce, 4300);
//						leftBrakeForce += breakforce/2f;
//						leftBrakeForce = Math.min(leftBrakeForce, 4300);
//					}
//					if (droneVelocity < 1) {
//						thrust = Math.min(2000,2.5f * drone.getAngularAccelerations(0, 0, 0, 0, 0, Math.min(4300,Math.abs(leftBrakeForce - rightBrakeForce)), 0)[0] * drone.getTotalMass() + leftBrakeForce + rightBrakeForce);
//						if (targetPositionDroneCoordinates.getNorm() < 2) {
//							throw new FinishedException();
//						}
//					}
				}
				if (droneVelocity < 1) {
					thrust = Math.min(2000,2.5f * drone.getAngularAccelerations(0, 0, 0, 0, 0, Math.min(4300,leftBrakeForce), Math.min(4300, rightBrakeForce))[0] * drone.getTotalMass() + leftBrakeForce + rightBrakeForce);
				}
			}
			thrust = Math.max(0, thrust);
        }
        if (this.getFlightMode() == FlightMode.BRAKE) {
        	System.out.println("brake");
        	this.drone.setArrived();
			if (drone.getVelocity().getNorm() > 0.001) {
				float targetVelocity;
				RealVector targetPosition = this.getTargetPosition();
				RealVector targetPositionDroneCoordinates = drone.transformationToDroneCoordinates(targetPosition.subtract(drone.getWorldPosition()));
				if (targetPositionDroneCoordinates.getNorm() > 200)
					targetVelocity = 10;
				if (targetPositionDroneCoordinates.getNorm() > 50)
					targetVelocity = 5;
				if (targetPositionDroneCoordinates.getNorm() > 20)
					targetVelocity = 2;
				if (targetPositionDroneCoordinates.getNorm() > 5)
					targetVelocity = 1;
				else
					targetVelocity = 1;
				if (drone.getVelocity().getNorm() > targetVelocity) {
					float distance = (float) targetPositionDroneCoordinates.getNorm();
					float velocity = (float) drone.getVelocity().getNorm();
					float force = 10f * drone.getTotalMass()* (float) Math.pow(velocity, 2) / (2*distance);
					leftBrakeForce = Math.max(0, Math.min(4300, force/2));
					rightBrakeForce = Math.max(0, Math.min(4300, force/2));
				}
				if (targetPositionDroneCoordinates.getNorm() < 5) {
					this.setFlightMode(FlightMode.STOP);
				}
			}	
		}
        if (this.getFlightMode() == FlightMode.TURN) {
        	System.out.println("turn");
        	float targetHeading = this.getTargetHeading();
        	if (drone.getVelocity().getNorm() < 1) {
        		thrust = 20;
        	}
        	if (drone.getHeading() < targetHeading) {
        		if (targetHeading - drone.getHeading() < Math.PI/90) {
//        			leftBrakeForce = 2000;
//        			rightBrakeForce = 2000;
//        			thrust = 0;
        			this.setFlightMode(FlightMode.STOP);
        		}
        		else if (drone.getHeadingAngularVelocity() > ((targetHeading - drone.getHeading()) / 20)){
        			leftBrakeForce = 0;
        			rightBrakeForce = 0;
        			thrust = 0;
        		}
        		else {
        			leftBrakeForce = 9000*drone.getHeadingAngularVelocity()*(float)drone.getVelocity().getNorm();
        			rightBrakeForce = 0;
        		}
        	}
        	else if (drone.getHeading() > targetHeading) {
        		if (targetHeading - drone.getHeading() > - Math.PI/90) {
        			this.setFlightMode(FlightMode.STOP);
        		}
        		else if (Math.abs(drone.getHeadingAngularVelocity()) > Math.abs(((targetHeading - drone.getHeading()) / 20))) {
        			leftBrakeForce = 0;
        			rightBrakeForce = 0;
        			thrust = 0;
        		}
        		else {
        			leftBrakeForce = 0;
        			rightBrakeForce = 9000*drone.getHeadingAngularVelocity()*(float)drone.getVelocity().getNorm();
        		}
        	}
        }
        if (this.getFlightMode() == FlightMode.STOP){
        	System.out.println("stop");
        	float targetHeading = (float) (Math.PI);
        	if (Math.abs(drone.getHeading()-targetHeading) > Math.PI/180) {
        		if (drone.getHeading() < targetHeading) {
            		thrust = 0;
                	leftBrakeForce = 500;
                	rightBrakeForce = 1000;
            	}
            	else {
            		thrust = 0;
                	leftBrakeForce = 1000;
                	rightBrakeForce = 500;
            	}
        	}
        	else {
            	if (drone.getHeading() < targetHeading) {
            		thrust = 0;
                	leftBrakeForce = 100;
                	rightBrakeForce = 100+18000*Math.abs(drone.getHeadingAngularVelocity()); //3600*Math.abs(drone.getHeadingAngularVelocity());
            	}
            	else {
            		thrust = 0;
                	leftBrakeForce = 100+18000*Math.abs(drone.getHeadingAngularVelocity());
                	rightBrakeForce = 100;
            	}
        	}
        	if(droneOfPackage != null && fromGate != null && toGate != null) {
	        	this.flyRoute(droneOfPackage, fromGate, toGate);
        	}
        	else {
        		drone.setCanBeAssigned(true);
        	}
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
	
	private Drone droneOfPackage = null;
	private Gate fromGate = null;
	private Gate toGate = null;
	
	public void setFlyAfterPackagePicked(Drone drone, Gate fromGate, Gate toGate) {
		this.droneOfPackage = drone;
		this.fromGate = fromGate;
		this.toGate = toGate;
	}
	
	private void resetFlyAfterPackagePicked() {
		this.droneOfPackage = null;
		this.fromGate = null;
		this.toGate = null;
	}
	
    public void flyRoute(Drone drone, Gate fromGate, Gate toGate) {     
    	RealVector[] route = routeCalculator.calculateRoute(drone, fromGate, toGate, drone.getHeight());
    	this.setTargets(route);
    	this.setTargetPosition(toGate.getWorldPosition());
    	RealVector vector = route[route.length-1].subtract(route[route.length-2]);
    	vector.setEntry(1, 0);
    	Angle angle = Angle.getYRotation(vector, new ArrayRealVector(new double[] {0,0,0}, false));
    	this.setTargetHeading(angle.getAngle());
    	this.setFlightMode(FlightMode.ASCEND);
    	this.resetFlyAfterPackagePicked();
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
		return (Float.isFinite(initialZVelocity));
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
	
	public void initialise(AutopilotConfig config, Drone drone) {
		if (! isValidConfig(config))
			throw new IllegalArgumentException();
		this.drone = drone;
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
	}

	public void simulationEnded() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}
	
	private Drone drone;
}
