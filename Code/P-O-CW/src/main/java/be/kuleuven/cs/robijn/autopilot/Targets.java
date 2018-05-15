package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.math.Angle;
import be.kuleuven.cs.robijn.common.math.Angle.Type;
import be.kuleuven.cs.robijn.worldObjects.Drone;

public class Targets {
	
	public Targets() {
	}
	
	public RealVector[] getTargets() {
		return this.targets;
	}
	
	public RealVector getFirstTarget(Drone drone) {
		RealVector currentTarget = this.getTargets()[index];
		float distanceToTarget = (float) currentTarget.getDistance(this.getDronePosition());
		if (distanceToTarget <= 3) {
			index += 1;
			if (index == this.getTargets().length)
				return null;
			Angle YRotation = Angle.getYRotation(this.getTargets()[index], drone.getWorldPosition());
			YRotation = Angle.add(YRotation, - drone.getHeading());
			if ((Math.abs(YRotation.getOrientation(Type.DEGREES)) > 30) && (drone.getWorldPosition().getDistance(this.getTargets()[index]) < 500)) {
				System.out.println("test2");
				RealVector newTarget = drone.getVelocity().add(drone.getWorldPosition());
				if (newTarget.getNorm() != 0)
					newTarget = newTarget.mapMultiply(1/newTarget.getNorm());
				newTarget.mapMultiply(750);
				newTarget.setEntry(1, this.getTargets()[index].getEntry(1));
				RealVector[] tars = this.getTargets();
				RealVector[] newTars = new RealVector[tars.length+1];
				for (int i = index; i < tars.length; i++) {
					newTars[i+1] = tars[i];
					if (i == index)
						newTars[i] = newTarget;
				}
				this.setTargets(newTars);
			}
		}
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
