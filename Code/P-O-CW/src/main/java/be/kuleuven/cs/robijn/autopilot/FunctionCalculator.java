package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.math.VectorMath;
import be.kuleuven.cs.robijn.worldObjects.Drone;

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
	
	public void setHeadingAngularAcceleration(float headingAngularAcceleration) {
		this.headingAngularAcceleration = headingAngularAcceleration;
	}
	
	private float pitchAngularAcceleration = 0;
	
	public void setPitchAngularAcceleration(float pitchAngularAcceleration) {
		this.pitchAngularAcceleration = pitchAngularAcceleration;
	}
	
	private float rollAngularAcceleration = 0;
	
	public void setRollAngularAcceleration(float rollAngularAcceleration) {
		this.rollAngularAcceleration = rollAngularAcceleration;
	}
	
	private float xAcceleration = 0;
	
	public void setXAcceleration(float xAcceleration) {
		this.xAcceleration = xAcceleration;
	}
	
	private float yAcceleration = 0;
	
	public void setYAcceleration(float yAcceleration) {
		this.yAcceleration = yAcceleration;
	}
	
	private Drone drone;
	
	public float getInertiaMatrixXX() {
		return (float) (drone.getTailMass()*Math.pow(drone.getTailSize(),2) +drone.getEngineMass()*Math.pow(drone.getEngineDistance(), 2));
	}
	
	public float getInertiaMatrixZZ() {
		return (float) (2*(drone.getWingMass()*Math.pow(drone.getWingX(),2)));
	}
	
	public float getInertiaMatrixYY() {
		return this.getInertiaMatrixXX() + this.getInertiaMatrixZZ();
	}
	
	public RealVector getTotalAngularVelocityDroneCoordinates() {
		return drone.transformationToDroneCoordinates(drone.getHeadingAngularVelocityVector()
				.add(drone.getRollAngularVelocityVector())
				.add(drone.getPitchAngularVelocityVector()));
	}
	
	public RealMatrix getInertiaMatrix() {
		return new Array2DRowRealMatrix(new double[][] {
			{this.getInertiaMatrixXX(), 0,                0},
			{0,               this.getInertiaMatrixYY(),  0}, 
			{0,               0,                this.getInertiaMatrixZZ()}
			}, false);
	}
	
	public RealVector getAngularMomentumDroneCoordinates() {
		return this.getInertiaMatrix().operate(this.getTotalAngularVelocityDroneCoordinates());
	}
	
	public UnivariateFunction functionForHeading() {
		return (x)->{return drone.transformationToDroneCoordinates(drone.getLiftForceVerStab((float)x)).getEntry(0)*drone.getTailSize()
				- getInertiaMatrixYY()*Math.cos(drone.getRoll())*Math.cos(drone.getPitch())*headingAngularAcceleration
				+ getInertiaMatrixYY()*Math.sin(drone.getRoll())*pitchAngularAcceleration
				- VectorMath.crossProduct(getTotalAngularVelocityDroneCoordinates(), getAngularMomentumDroneCoordinates()).getEntry(1)
				- getInertiaMatrix().operate(drone.transformationToDroneCoordinates(
						VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
						.add(VectorMath.crossProduct(
							drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
							drone.getRollAngularVelocityVector()
						))
				 )).getEntry(1);};
	}
	
	public UnivariateFunction functionForYVelocity(float verStabInclination) {
		return (x)->{return drone.transformationToDroneWithoutRollCoordinates(drone.getLiftForceLeftWing((float)x)).getEntry(1)
				+ drone.transformationToDroneWithoutRollCoordinates(drone.getLiftForceRightWing((float)x)).getEntry(1)
				+ drone.transformationToDroneWithoutRollCoordinates(drone.getTotalGravitationalForce()).getEntry(1)
				+ drone.transformationToDroneWithoutRollCoordinates(drone.getLiftForceVerStab(verStabInclination)).getEntry(1)
				- (drone.getTotalMass() * yAcceleration)
				;};
	}
	
	public UnivariateFunction functionForYVelocityWorldCoordinates(float verStabInclination) {
		return (x)->{return drone.getLiftForceLeftWing((float)x).getEntry(1)
				+ drone.getLiftForceRightWing((float)x).getEntry(1)
				+ drone.getTotalGravitationalForce().getEntry(1)
				+ drone.getLiftForceVerStab(verStabInclination).getEntry(1)
				- (drone.getTotalMass() * yAcceleration)
				;};
	}
	
	public UnivariateFunction functionForXVelocity(float verStabInclination, float wingInclination) {
		float targetXForce = drone.getTotalMass()*xAcceleration;
		
		return new UnivariateFunction() {
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
				return drone.transformationToDroneWithoutRollCoordinates(
						matrix.operate(drone.transformationToDroneCoordinates(drone.getLiftForceLeftWing(wingInclination)))).getEntry(0)
						+ drone.transformationToDroneWithoutRollCoordinates(
								matrix.operate(drone.transformationToDroneCoordinates(drone.getLiftForceRightWing(wingInclination)))).getEntry(0)
						+ drone.transformationToDroneWithoutRollCoordinates(
								matrix.operate(drone.transformationToDroneCoordinates(drone.getLiftForceVerStab(verStabInclination)))).getEntry(0)
						- targetXForce;
			}
        };
	}
	
	public UnivariateFunction functionForRoll(float wingInclination) {
		return (x)->{return 
				(- drone.transformationToDroneCoordinates(drone.getLiftForceLeftWing(wingInclination - ((float)x))).getEntry(1)*drone.getWingX())
				+ drone.transformationToDroneCoordinates(drone.getLiftForceRightWing(wingInclination + ((float)x))).getEntry(1)*drone.getWingX()
				- getInertiaMatrixZZ()*rollAngularAcceleration
				+ getInertiaMatrixZZ()*Math.sin(drone.getPitch())*headingAngularAcceleration
				- VectorMath.crossProduct(getTotalAngularVelocityDroneCoordinates(), getAngularMomentumDroneCoordinates()).getEntry(2)
				- getInertiaMatrix().operate(drone.transformationToDroneCoordinates(
						VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
						.add(VectorMath.crossProduct(
							drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
							drone.getRollAngularVelocityVector()
						))
				 )).getEntry(2);};
	}
	
	public UnivariateFunction functionForYVelocityWithRoll(float verStabInclination, float rollInclination) {
		return (x)->{return drone.transformationToDroneWithoutRollCoordinates(drone.getLiftForceLeftWing(((float)x) - rollInclination)).getEntry(1)
				+ drone.transformationToDroneWithoutRollCoordinates(drone.getLiftForceRightWing(((float)x) + rollInclination)).getEntry(1)
				+ drone.transformationToDroneWithoutRollCoordinates(drone.getTotalGravitationalForce()).getEntry(1)
				+ drone.transformationToDroneWithoutRollCoordinates(drone.getLiftForceVerStab(verStabInclination)).getEntry(1)
				- (drone.getTotalMass() * yAcceleration)
				;};
	}
	
	public UnivariateFunction functionForYVelocityWithRollWorldCoordinates(float verStabInclination, float rollInclination) {
		return (x)->{return drone.getLiftForceLeftWing(((float)x) - rollInclination).getEntry(1)
				+ drone.getLiftForceRightWing(((float)x) + rollInclination).getEntry(1)
				+ drone.getTotalGravitationalForce().getEntry(1)
				+ drone.getLiftForceVerStab(verStabInclination).getEntry(1)
				- (drone.getTotalMass() * yAcceleration)
				;};
	}
	
	public UnivariateFunction functionForPitch() {
		return (x)->{return drone.transformationToDroneCoordinates(drone.getLiftForceHorStab((float)x)).getEntry(1)*drone.getTailSize()
				+ getInertiaMatrixXX()*Math.cos(drone.getRoll())*pitchAngularAcceleration
				+ getInertiaMatrixXX()*Math.cos(drone.getPitch())*Math.sin(drone.getRoll())*headingAngularAcceleration
				+ VectorMath.crossProduct(getTotalAngularVelocityDroneCoordinates(), getAngularMomentumDroneCoordinates()).getEntry(0)
				+ getInertiaMatrix().operate(drone.transformationToDroneCoordinates(
						VectorMath.crossProduct(drone.getHeadingAngularVelocityVector(), drone.getPitchAngularVelocityVector())
						.add(VectorMath.crossProduct(
							drone.getHeadingAngularVelocityVector().add(drone.getPitchAngularVelocityVector()),
							drone.getRollAngularVelocityVector()
						))
				 )).getEntry(0);};
	}
	
	public UnivariateFunction functionforPitchSimplified() {
		return (x)->{return drone.transformationToDroneCoordinates(drone.getLiftForceHorStab((float)x)).getEntry(1)*drone.getTailSize()
				+ getInertiaMatrixXX()*Math.cos(drone.getRoll())*pitchAngularAcceleration
				+ VectorMath.crossProduct(getTotalAngularVelocityDroneCoordinates(), getAngularMomentumDroneCoordinates()).getEntry(0);};
	}
}
