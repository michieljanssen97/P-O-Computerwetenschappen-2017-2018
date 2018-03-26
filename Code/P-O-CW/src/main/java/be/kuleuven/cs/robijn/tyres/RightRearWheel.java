package be.kuleuven.cs.robijn.tyres;

import interfaces.AutopilotConfig;

public class RightRearWheel extends RearWheel{

	public RightRearWheel(AutopilotConfig config) throws IllegalArgumentException {
		super(config);
		this.wheelX = config.getRearWheelX();
	}
}
