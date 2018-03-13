package be.kuleuven.cs.robijn.tyres;

import interfaces.AutopilotConfig;

public class LeftRearWheel extends RearWheel{

	public LeftRearWheel(AutopilotConfig config) throws IllegalArgumentException {
		super(config);
		this.wheelX = - config.getRearWheelX();
	}


}
