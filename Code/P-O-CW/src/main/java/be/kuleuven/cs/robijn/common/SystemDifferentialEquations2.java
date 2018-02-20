package be.kuleuven.cs.robijn.common;

import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.ode.*;

import be.kuleuven.cs.robijn.tyres.FrontWheel;
import interfaces.AutopilotOutputs;

public class SystemDifferentialEquations2 implements FirstOrderDifferentialEquations {
	
	public SystemDifferentialEquations2(Drone drone, AutopilotOutputs autopilotOutputs, int amount) throws IllegalArgumentException {
		if (! isValidDrone(drone))
			throw new IllegalArgumentException();
		this.drone = drone;
		this.autopilotOutputs = autopilotOutputs;
		if (! isValidAmount(amount))
			throw new IllegalArgumentException();
		this.amount = amount;
	}
	
	private final Drone drone;
	
	public Drone getDrone() {
		return this.drone;
	}
	
	public static boolean isValidDrone(Drone drone) {
		return (drone != null);
	}
	
	private final AutopilotOutputs autopilotOutputs;
	
	public AutopilotOutputs getAutopilotOutputs() {
		return this.autopilotOutputs;
	}
	
	public static boolean isValidAmount(int amount) {
		return ((amount == 1) || (amount == 2) || (amount == 3));
	}
	
	private final int amount;
	
	public int getAmount() {
		return this.amount;
	}

	public int getDimension() {
		return 1;
	}

	public void computeDerivatives(double t, double[] y, double[] yDot) {
		Drone drone = this.getDrone();
		
		FrontWheel tyre = drone.getFirstChildOfType(FrontWheel.class);
		AutopilotOutputs output = this.getAutopilotOutputs();
		RealVector totalForceDrone = drone.getGravitationalForceEngine()
				.add(drone.getGravitationalForceTail())
				.add(drone.getGravitationalForceWing().mapMultiply(2)) 
				.add(drone.getLiftForceLeftWing(output.getLeftWingInclination()))
				.add(drone.getLiftForceRightWing(output.getRightWingInclination()))
				.add(drone.getLiftForceHorStab(output.getHorStabInclination()));
		totalForceDrone = totalForceDrone.mapMultiply(1/this.getAmount());
		
		yDot[0] = (totalForceDrone.getEntry(1) - tyre.getTyreSlope()*y[0])/tyre.getDampSlope();
	}
}
