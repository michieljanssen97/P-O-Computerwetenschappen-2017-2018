package be.kuleuven.cs.robijn.autopilot;

public class AutopilotSettings {
	
	public AutopilotSettings() {
	}
	
	public float getTurningTime() {
		return this.turningTime;
	}
	
	public void setTurningtime(float turningTime) {
		this.turningTime = turningTime;
	}
	
	private float turningTime = 0.5f;
	
	public float getXMovementTime() {
		return this.xMovementTime;
	}
	
	public void setXMovementTime(float xMovementTime) {
		this.xMovementTime = xMovementTime;
	}
	
	private float xMovementTime = 3f;
	
	public float getMaxRoll() {
		return this.maxRoll;
	}
	
	public float getMaxHeadingAngularAcceleration() {
		return maxHeadingAngularAcceleration;
	}

	public float getMaxPitchAngularAcceleration() {
		return maxPitchAngularAcceleration;
	}

	public float getMaxRollAngularAcceleration() {
		return maxRollAngularAcceleration;
	}

	public float getMaxXAcceleration() {
		return maxXAcceleration;
	}

	public float getMaxYAcceleration() {
		return maxYAcceleration;
	}

	public float getMaxHeadingAngularVelocity() {
		return maxHeadingAngularVelocity;
	}

	public float getMaxPitchAngularVelocity() {
		return maxPitchAngularVelocity;
	}

	public float getMaxRollAngularVelocity() {
		return maxRollAngularVelocity;
	}

	public float getMaxXVelocity() {
		return maxXVelocity;
	}

	public float getMaxYVelocity() {
		return maxYVelocity;
	}

	public float getCorrectionFactor() {
		return correctionFactor;
	}

	public float getPitchTakeOff() {
		return pitchTakeOff;
	}

	public float getTargetVelocity() {
		return targetVelocity;
	}

	public float getHeight() {
		return height;
	}

	private final float maxRoll = (float) Math.toRadians(45.0);
	private final float maxHeadingAngularAcceleration = Float.MAX_VALUE;
	private final float maxPitchAngularAcceleration =Float.MAX_VALUE;
	private final float maxRollAngularAcceleration = Float.MAX_VALUE;
	private final float maxXAcceleration = Float.MAX_VALUE;
	private final float maxYAcceleration = Float.MAX_VALUE;
	private final float maxHeadingAngularVelocity = Float.MAX_VALUE; //0.3f
	private final float maxPitchAngularVelocity =Float.MAX_VALUE;
	private final float maxRollAngularVelocity = Float.MAX_VALUE;
	private final float maxXVelocity = Float.MAX_VALUE; //2f
	private final float maxYVelocity = Float.MAX_VALUE;
	private final float correctionFactor = 3.0f;
	private final float pitchTakeOff = (float) Math.toRadians(30.0);
	private final float targetVelocity = -43f;
	private final float height = 10;
}
