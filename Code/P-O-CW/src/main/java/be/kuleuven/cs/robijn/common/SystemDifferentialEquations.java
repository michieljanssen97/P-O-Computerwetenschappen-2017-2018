package be.kuleuven.cs.robijn.common;

import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.ode.*;
import p_en_o_cw_2017.*;

public class SystemDifferentialEquations implements FirstOrderDifferentialEquations {
	
	public SystemDifferentialEquations(Drone drone, AutopilotOutputs autopilotOutputs) throws IllegalArgumentException {
		if (! isValidDrone(drone))
			throw new IllegalArgumentException();
		this.drone = drone;
		this.autopilotOutputs = autopilotOutputs;
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

	public int getDimension() {
		return 12;
	}

	public void computeDerivatives(double t, double[] y, double[] yDot) {
		RealVector acceleration = this.getDrone().getAcceleration(this.getAutopilotOutputs().getThrust(),
				this.getAutopilotOutputs().getLeftWingInclination(), this.getAutopilotOutputs().getRightWingInclination(),
				this.getAutopilotOutputs().getHorStabInclination(), this.getAutopilotOutputs().getVerStabInclination());
		
		float[] angularAccelerations = this.getDrone().getAngularAccelerations(this.getAutopilotOutputs().getLeftWingInclination(),
				this.getAutopilotOutputs().getRightWingInclination(), this.getAutopilotOutputs().getHorStabInclination(), 
				this.getAutopilotOutputs().getVerStabInclination());
		float headingAngularAcceleration = angularAccelerations[0];
		float pitchAngularAcceleration = angularAccelerations[1];
		float rollAngularAcceleration = angularAccelerations[2];
		
		yDot[0] = y[1];
		yDot[1] = acceleration.getEntry(0);
		yDot[2] = y[3];
		yDot[3] = acceleration.getEntry(1);
		yDot[4] = y[5];
		yDot[5] = acceleration.getEntry(2);
		yDot[6] = y[7];
		yDot[7] = headingAngularAcceleration;
		yDot[8] = y[9];
		yDot[9] = pitchAngularAcceleration;
		yDot[10] = y[11];
		yDot[11] = rollAngularAcceleration;	
	}
}
