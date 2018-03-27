package be.kuleuven.cs.robijn.tyres;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.worldObjects.Drone;
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
	
	public RealVector getTyreForce(Drone drone, float wheelBrakeForce) {
		if ((wheelBrakeForce > this.getRMax()) || (wheelBrakeForce < 0))
			throw new IllegalArgumentException();
		
		RealVector totalForce = new ArrayRealVector(new double[] {0, 0, 0}, false);
		
		if (this.getD(drone) != 0)  {
			RealVector forceTyre = new ArrayRealVector(new double[] {0, 
					this.getTyreSlope()*this.getD(drone)
					+ this.getDampSlope()*this.getVelocityD(drone), 0}, false);
			totalForce = totalForce.add(forceTyre);
			
			RealVector brakeForce = new ArrayRealVector(new double[] {this.getVelocityTyre(drone).getEntry(0), 
					0, this.getVelocityTyre(drone).getEntry(2)}, false);
			if (brakeForce.getNorm() == 0) {
				brakeForce = new ArrayRealVector(new double[] {totalForce.getEntry(1), 0, totalForce.getEntry(2)}, false);
				if (brakeForce.getNorm() < wheelBrakeForce)
					throw new IllegalArgumentException();
			}
			if (brakeForce.getNorm() != 0)
				brakeForce = brakeForce.mapMultiply(-(1/brakeForce.getNorm()));
			brakeForce = brakeForce.mapMultiply(wheelBrakeForce);
			
			totalForce = totalForce.add(brakeForce);
		}
		
		return totalForce;
	}
	
	public boolean isValidWheelZ(float wheelZ) {
		return ((wheelZ < 0) && (Float.isFinite(wheelZ)));
	}
}
