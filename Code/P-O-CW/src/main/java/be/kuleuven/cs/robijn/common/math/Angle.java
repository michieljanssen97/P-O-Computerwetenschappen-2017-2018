package be.kuleuven.cs.robijn.common.math;

import org.apache.commons.math3.linear.RealVector;

public class Angle {
	
	public Angle(float angle) {
		this.angle = angle;
	}
	
	public Angle(float angle, Type type) throws IllegalArgumentException {
		switch(type) {
			case DEGREES: 
				this.angle = (float) Math.toRadians(angle);
				break;
			case RADIANS:
				this.angle = angle;
				break;
			default:
				throw new IllegalArgumentException();
		}
	}
	
	private final float angle;
	
	public float getAngle(Type type) throws IllegalArgumentException {
		float angle = (float) (this.angle % (Math.PI*2));
		switch(type) {
			case DEGREES:
				return (float) Math.toDegrees(angle);
			case RADIANS:
				return angle;
			default:
				throw new IllegalArgumentException();
		}
	}
	
	public float getAngle() {
		return this.getAngle(Type.RADIANS);
	}
	
	public float getOrientation(Type type) {
		return (float) (this.getAngle(type) - 2*Math.PI);
	}
	
	public float getOrientation() {
		return this.getOrientation(Type.RADIANS);
	}
	
	public static Angle getXRotation(RealVector target, RealVector current) {
		float angle = (float) Math.atan((target.getEntry(1) - current.getEntry(1))
				/(current.getEntry(2) - target.getEntry(2)));
		if ((current.getEntry(2) - target.getEntry(2)) < 0) {
			angle = -angle;
		}
		return new Angle(angle);
	}
	
	public static Angle getYRotation(RealVector target, RealVector current) {
		float angle = (float) Math.atan((current.getEntry(0) - target.getEntry(0))
				/(current.getEntry(2) - target.getEntry(2)));
		if ((current.getEntry(2) - target.getEntry(2)) < 0) {
			angle += Math.PI;
		}
		return new Angle(angle);
	}
	
	public static Angle add(Angle angle, float value) {
		return new Angle(angle.getOrientation() + value);
	}
	
	public enum Type {
		RADIANS, DEGREES;
	}
}
