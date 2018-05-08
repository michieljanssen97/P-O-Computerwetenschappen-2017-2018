package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class Targets {
	
	public Targets() {
	}
	
	public RealVector[] getTargets() {
		return this.targets;
	}
	
	public RealVector getFirstTarget() {
		RealVector currentTarget = this.getTargets()[index];
		float distanceToTarget = (float) currentTarget.getDistance(this.getDronePosition());
		if (distanceToTarget <= 3)
			index += 1;
		if (index == this.getTargets().length)
			return null;
		return this.getTargets()[index];
	}
		
	private int index = 0;
	
	public int getNbTargets() {
		return this.getTargets().length;
	}
	
	public void setTargets(RealVector[] targets) {
		this.targets = targets;
	}
	
	public RealVector getDronePosition() {
		return this.dronePosition;
	}
	
	public void setDronePosition(RealVector pos) {
		this.dronePosition = pos;
	}
	
	private RealVector[] targets = new RealVector[] {new ArrayRealVector(new double[0], false)};
	
	private RealVector dronePosition = new ArrayRealVector(new double[0], false);
}
