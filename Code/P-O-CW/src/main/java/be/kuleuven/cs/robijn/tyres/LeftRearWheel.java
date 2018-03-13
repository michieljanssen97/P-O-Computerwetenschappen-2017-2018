package be.kuleuven.cs.robijn.tyres;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.Drone;
import interfaces.AutopilotConfig;

public class LeftRearWheel extends Tyre{

	public LeftRearWheel(AutopilotConfig config) throws IllegalArgumentException {
		super(config);
		if(! isValidWheelZ(config.getRearWheelZ())) {
			throw new IllegalArgumentException();
		}
		this.wheelZ = config.getRearWheelZ();
		
		if(! isValidWheelX(config.getRearWheelX(), config.getWingX())) {
			throw new IllegalArgumentException();
		}
		this.wheelX = - config.getRearWheelX();
		
	}
	
	
	@Override
	public boolean isValidWheelX(float wheelX, float wingX) {
		
		return (wheelX > 0) && (wheelX <= wingX);
	}
	
	public RealVector getTyreForce(Drone drone, float frontBrakeForce, float leftBrakeForce, float rightBrakeForce) {
		if ((leftBrakeForce > this.getRMax()) || (leftBrakeForce < 0))
			throw new IllegalArgumentException();
		
		RealVector totalForce = new ArrayRealVector(new double[] {0, 0, 0}, false);
		
		if (this.getD(drone) != 0)  {
			RealVector forceTyre = new ArrayRealVector(new double[] {0, 
					this.getTyreSlope()*this.getD(drone)
					+ this.getDampSlope()*this.getVelocityD(drone), 0}, false);
			totalForce = totalForce.add(forceTyre);
			
			RealVector frictionForce = drone.transformationToWorldCoordinates(new ArrayRealVector(new double[] {
					-this.getFcMax() * forceTyre.getEntry(1) * this.getLateralVelocity(drone),
					0, 0}, false));
			totalForce = totalForce.add(frictionForce);
			
			RealVector brakeForce = new ArrayRealVector(new double[] {this.getVelocityTyre(drone).getEntry(0), 
					0, this.getVelocityTyre(drone).getEntry(2)}, false);
			if (brakeForce.getNorm() == 0) {
				brakeForce = new ArrayRealVector(new double[] {totalForce.getEntry(1), 0, totalForce.getEntry(2)}, false);
				if (brakeForce.getNorm() < leftBrakeForce)
					throw new IllegalArgumentException();
			}
			if (brakeForce.getNorm() != 0)
				brakeForce = brakeForce.mapMultiply(-(1/brakeForce.getNorm()));
			brakeForce = brakeForce.mapMultiply(leftBrakeForce);
			
			totalForce = totalForce.add(brakeForce);
		}
		
		return totalForce;
	}
	
	public boolean isValidWheelZ(float wheelZ) {
		return ((wheelZ > 0) && (wheelZ < Float.MAX_VALUE));
	}
}
