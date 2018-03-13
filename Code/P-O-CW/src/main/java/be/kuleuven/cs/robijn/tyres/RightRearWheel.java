package be.kuleuven.cs.robijn.tyres;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.Drone;
import interfaces.AutopilotConfig;

public class RightRearWheel extends RearWheel{

	public RightRearWheel(AutopilotConfig config) throws IllegalArgumentException {
		super(config);
		this.wheelX = config.getRearWheelX();
	}
	
	public RealVector getTyreForce(Drone drone, float frontBrakeForce, float leftRearWheelBrakeForce, float rightRearWheelBrakeForce) {
		RealVector totalForce = new ArrayRealVector(new double[] {0, 0, 0}, false);
		
		if (this.getD(drone) != 0)  {
			RealVector forceTyre = drone.transformationToWorldCoordinates(new ArrayRealVector(new double[] {0, 
					this.getTyreSlope()*this.getD(drone)
					+ this.getDampSlope()*this.getVelocityD(drone), 0}, false));
			totalForce = totalForce.add(forceTyre);
			
			RealVector frictionForce = drone.transformationToWorldCoordinates(new ArrayRealVector(new double[] {
					-this.getFcMax() * forceTyre.getEntry(1) * this.getLateralVelocity(drone),
					0, 0}, false));
			totalForce = totalForce.add(frictionForce);
			
			RealVector brakeForce = new ArrayRealVector(new double[] {this.getVelocityTyre(drone).getEntry(0), 
					0, this.getVelocityTyre(drone).getEntry(2)}, false);
			if (brakeForce.getNorm() != 0)
				brakeForce = brakeForce.mapMultiply(-(1/brakeForce.getNorm()));
			brakeForce = brakeForce.mapMultiply(rightRearWheelBrakeForce);
			
			totalForce = totalForce.add(brakeForce);
		}
		
		return totalForce;
	}
}
