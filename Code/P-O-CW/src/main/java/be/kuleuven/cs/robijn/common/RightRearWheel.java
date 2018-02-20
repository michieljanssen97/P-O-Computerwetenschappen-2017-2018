package be.kuleuven.cs.robijn.common;

import interfaces.AutopilotConfig;

public class RightRearWheel extends Tyre{

	public RightRearWheel(AutopilotConfig config) throws IllegalArgumentException {
		super(config);
		if(! isValidWheelZ(config.getRearWheelZ())) {
			throw new IllegalArgumentException();
		}
		this.wheelZ = config.getRearWheelZ();
		
		if(! isValidWheelX(config.getRearWheelX())) {
			throw new IllegalArgumentException();
		}
		this.wheelX = config.getRearWheelX();
		
		this.wingX = config.getWingX(); //TODO kan beter door rechtstreeks naar de Drone te verwijzen. Bijvoorbeeld mbv droneID
	}
	
	private float wingX;
	
	private float getWingX() {
		return wingX;
	}
	
	@Override
	public boolean isValidWheelX(float wheelX) {
		return (wheelX > 0) && (wheelX <= Float.MAX_VALUE) && (wheelX <= getWingX());
	}
}
