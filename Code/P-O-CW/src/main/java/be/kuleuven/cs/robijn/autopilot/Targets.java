package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.worldObjects.Drone;

public class Targets {
	
	public Targets(Drone drone) {
		this.drone = drone;
	}
	
	public RealVector[] getTargets() {
		return this.targets;
	}
	
	public RealVector getFirstTarget() {
		return this.getTargets()[0];
	}
	
	public int getNbTargets() {
		return this.getTargets().length;
	}
	
	public void setTargets(RealVector[] targets) {
		this.targets = targets;
	}
	
	private RealVector[] targets = new RealVector[] {new ArrayRealVector(new double[0], false)};
	
	private Drone drone;
}
