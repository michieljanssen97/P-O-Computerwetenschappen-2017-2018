package be.kuleuven.cs.robijn.tyres;

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
}
