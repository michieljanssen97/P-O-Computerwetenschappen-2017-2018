package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.Drone;
import be.kuleuven.cs.robijn.common.math.VectorMath;

public class FunctionCalculator {
	
	public FunctionCalculator(Drone drone) {
		this.drone = drone;
	}
	
	public FunctionCalculator(Drone drone, float headingAngularAcceleration, float pitchAngularAcceleration, float rollAngularAcceleration,
			float xAcceleration, float yAcceleration) {
		this.headingAngularAcceleration = headingAngularAcceleration;
		this.pitchAngularAcceleration = pitchAngularAcceleration;
		this.rollAngularAcceleration = rollAngularAcceleration;
		this.xAcceleration = xAcceleration;
		this.yAcceleration = yAcceleration;
		this.drone = drone;
	}
	
	private float headingAngularAcceleration = 0;
	
	private float pitchAngularAcceleration = 0;
	
	private float rollAngularAcceleration = 0;
	
	private float xAcceleration = 0;
	
	private float yAcceleration = 0;
	
	private Drone drone;
	
	public float getInertiaMatrixXX() {
		return (float) (drone.getTailMass()*Math.pow(drone.getTailSize(),2) +drone.getEngineMass()*Math.pow(drone.getEngineDistance(), 2));
	}
	
	public float getInertiaMatrixZZ() {
		return (float) (2*(drone.getWingMass()*Math.pow(drone.getWingX(),2)));
	}
	
	public float getInertiaMatrixYY() {
		return inertiaMatrixXX + inertiaMatrixZZ;
	}
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
	
	public UnivariateFunction functionForHeading() {
		return (x)->{return drone.transformationToDroneCoordinates(drone.getLiftForceVerStab((float)x)).getEntry(0)*drone.getTailSize()
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
	}
}
