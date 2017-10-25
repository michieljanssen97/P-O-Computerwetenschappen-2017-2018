package be.kuleuven.cs.robijn.testbed;

import org.apache.commons.math3.linear.*;
import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.testbed.renderer.OpenGLRenderer;
import p_en_o_cw_2017.*;

/**
 * A class of virtual testbeds.
 * 
 * @author Pieter Vandensande en Roy De Prins.
 */
public class VirtualTestbed extends WorldObject implements TestBed {
	
	public VirtualTestbed(AutopilotConfig config) {
		Drone drone = new Drone(config, new ArrayRealVector(new double[] {0, 0, -1000.0/3.6}, false));
		this.addChild(drone);
	}
	
	/**
	 * Method to move the drone of this virtual testbed.
	 * The position and the velocity of the center of mass, the heading, the pitch, the roll
	 * and the angular velocities get updated
	 * using the outputs from the autopilot (thrust, leftWingInclination,
	 * rightWingInclination, horStabInclination, verStabInclination).
	 * 
	 * @param  dt
	 * 		   Time duration (in seconds) to move this drone.
	 * @throws IllegalArgumentException
	 * 		   The given time duration is negative.
	 * 		 | dt < 0
	 * @throws IllegalStateException
	 *         This virtual testbed has no drone.
	 *         drone == null
	 */
	public void moveDrone(float dt, AutopilotOutputs output) throws IllegalArgumentException, IllegalStateException {
		if (dt < 0)
			throw new IllegalArgumentException();
		Drone drone = this.getFirstChildOfType(Drone.class);
		if (drone == null)
			throw new IllegalStateException("this virtual testbed has no drone");
		RealVector position = drone.getWorldPosition();
		RealVector velocity = drone.getVelocity();
		RealVector acceleration = drone.getAcceleration(output.getThrust(),
				output.getLeftWingInclination(), output.getRightWingInclination(), output.getRightWingInclination(), output.getVerStabInclination());
		
		drone.setRelativePosition(position.add(velocity.mapMultiply(dt)).add(acceleration.mapMultiply(Math.pow(dt, 2)/2)));
		drone.setVelocity(velocity.add(acceleration.mapMultiply(dt)));
		
		float[] angularAccelerations = drone.getAngularAccelerations(output.getLeftWingInclination(),
				output.getRightWingInclination(), output.getRightWingInclination(), output.getVerStabInclination(), output.getThrust());
		float heading = drone.getHeading();
		float headingAngularVelocity = drone.getHeadingAngularVelocity();
		float headingAngularAcceleration = angularAccelerations[0];
		float pitch = drone.getPitch();
		float pitchAngularVelocity = drone.getPitchAngularVelocity();
		float pitchAngularAcceleration = angularAccelerations[1];
		float roll = drone.getRoll();
		float rollAngularVelocity = drone.getRollAngularVelocity();
		float rollAngularAcceleration = angularAccelerations[2];
		
		drone.setHeading((float) ((heading + headingAngularVelocity*dt + headingAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI)));
		drone.setPitch((float) ((pitch + pitchAngularVelocity*dt + pitchAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI)));
		drone.setRoll((float) ((roll + rollAngularVelocity*dt + rollAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI)));
		
		drone.setHeadingAngularVelocity(headingAngularVelocity + headingAngularAcceleration*dt);
		drone.setPitchAngularVelocity(pitchAngularVelocity + pitchAngularAcceleration*dt);
		drone.setRollAngularVelocity(rollAngularVelocity + rollAngularAcceleration*dt);
	}
	
	public void setElapsedTime(long elapsedTime) throws IllegalArgumentException {
		if (! isValidElapsedTime(elapsedTime))
			throw new IllegalArgumentException();
		this.elapsedTime = elapsedTime;
	}
	
	private long elapsedTime = 0;
	
	public long getElapsedTime() {
		return elapsedTime;
	}

	public long getBeginSimulation() {
		return beginSimulation;
	}

	private final long beginSimulation = System.currentTimeMillis();
	
	public long getLastUpdate() {
		return lastUpdate;
	}
	
	public static boolean isValidElapsedTime(long elapsedTime) {
		return ((elapsedTime >= 0) & (elapsedTime <= Long.MAX_VALUE));
	}
	
	public static boolean isValidBeginSimulation(long beginSimulation) {
		return ((beginSimulation >= 0) & (beginSimulation <= Long.MAX_VALUE));
	}
	
	public static boolean isValidLastUpdate(long LastUpdate) {
		return ((LastUpdate >= 0) & (LastUpdate <= Long.MAX_VALUE));
	}
	
	private long lastUpdate = System.currentTimeMillis();
	
	public void setLastUpdate(long lastUpdate) throws IllegalArgumentException {
		if (! isValidLastUpdate(lastUpdate))
			throw new IllegalArgumentException();
		this.lastUpdate = lastUpdate;
	}
	
	public void update(AutopilotOutputs output) {
		long now = System.currentTimeMillis();
		this.setElapsedTime(now - this.getBeginSimulation());
		this.moveDrone((float)(now- this.getLastUpdate())/1000f, output);
		this.setLastUpdate(now);
	}
	
	private OpenGLRenderer renderer;
	
	public Renderer getRenderer() {
		if (renderer == null)
			renderer = OpenGLRenderer.create();
		return renderer;
	}

	public AutopilotInputs getInputs() {
		Drone drone = this.getFirstChildOfType(Drone.class);
		return new AutopilotInputs() {
            public byte[] getImage() { return null; }
            public float getX() { return (float) drone.getWorldPosition().getEntry(0); }
            public float getY() { return (float) drone.getWorldPosition().getEntry(1); }
            public float getZ() { return (float) drone.getWorldPosition().getEntry(2); }
            public float getHeading() { return drone.getHeading(); }
            public float getPitch() { return drone.getPitch(); }
            public float getRoll() { return drone.getRoll(); }
            public float getElapsedTime() { return this.getElapsedTime(); }
        };
	}
}