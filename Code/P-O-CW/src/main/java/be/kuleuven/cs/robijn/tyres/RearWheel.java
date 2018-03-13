package be.kuleuven.cs.robijn.tyres;

import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.Drone;
import interfaces.AutopilotConfig;

public abstract class RearWheel extends Tyre{

	public RearWheel(AutopilotConfig config) throws IllegalArgumentException {
		super(config);
		if(! isValidWheelZ(config.getRearWheelZ())) {
			throw new IllegalArgumentException();
		}
		this.wheelZ = config.getRearWheelZ();
		
		if(! isValidWheelX(config.getRearWheelX(), config.getWingX())) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public boolean isValidWheelZ(float wheelZ) {
		return ((wheelZ > 0) && (wheelZ < Float.MAX_VALUE));
	}

	@Override
	public boolean isValidWheelX(float wheelX, float wingX) {
		return (wheelX > 0) && (wheelX <= wingX);
	}

	public abstract RealVector getTyreForce(Drone drone, float frontWheelBrakeForce, float leftRearWheelBrakeForce, float rightRearWheelBrakeForce);


}
