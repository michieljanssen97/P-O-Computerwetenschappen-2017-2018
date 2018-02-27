package be.kuleuven.cs.robijn.tyres;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.Drone;
import interfaces.AutopilotConfig;

public class FrontWheel extends Tyre{

	public FrontWheel(AutopilotConfig config) throws IllegalArgumentException {
		super(config);
		
		if (! isValidWheelZ(config.getFrontWheelZ())) {
			throw new IllegalArgumentException();
		}
		this.wheelX = 0; //Zie opgave
		this.wheelZ = config.getFrontWheelZ();

	}

	@Override
	public boolean isValidWheelX(float wheelX, float wingX) {
		return wheelX == 0; //Zie opgave
	}
	
	public RealVector getTyreForce(Drone drone, float frontBrakeForce, float leftBrakeForce, float rightBrakeForce) {
		RealVector totalForce = new ArrayRealVector(new double[] {0, 0, 0}, false);
		
		if (this.getD(drone) != 0)  {
			RealVector forceTyre = drone.transformationToWorldCoordinates(new ArrayRealVector(new double[] {0, 
					this.getTyreSlope()*this.getD(drone)
					+ this.getDampSlope()*this.getVelocityD(drone), 0}, false));
			totalForce.add(forceTyre);
			
			RealVector brakeForce = new ArrayRealVector(new double[] {this.getVelocityTyre(drone).getEntry(0), 
					0, this.getVelocityTyre(drone).getEntry(2)}, false);
			brakeForce = brakeForce.mapMultiply(-(1/brakeForce.getNorm()));
			brakeForce = brakeForce.mapMultiply(frontBrakeForce);
			
			totalForce.add(brakeForce);
		}
		
		return totalForce;
	}
}
