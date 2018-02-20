package be.kuleuven.cs.robijn.tyres;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;

import be.kuleuven.cs.robijn.common.Drone;
import be.kuleuven.cs.robijn.common.SystemDifferentialEquations2;
import interfaces.AutopilotConfig;
import interfaces.AutopilotOutputs;

public class RightRearWheel extends Tyre{

	public RightRearWheel(AutopilotConfig config) throws IllegalArgumentException {
		super(config);
		if(! isValidWheelZ(config.getRearWheelZ())) {
			throw new IllegalArgumentException();
		}
		this.wheelZ = config.getRearWheelZ();
		
		if(! isValidWheelX(config.getRearWheelX(), config.getWingX())) {
			throw new IllegalArgumentException();
		}
		this.wheelX = config.getRearWheelX();
		
	}
	
	@Override
	public boolean isValidWheelX(float wheelX, float wingX) {
		return (wheelX > 0) && (wheelX <= wingX);
	}
}
