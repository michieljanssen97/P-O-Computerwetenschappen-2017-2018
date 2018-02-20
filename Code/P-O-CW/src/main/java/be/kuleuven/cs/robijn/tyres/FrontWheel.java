package be.kuleuven.cs.robijn.tyres;

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

}
