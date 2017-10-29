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
	private final AutopilotConfig config;

	private OpenGLRenderer renderer;
	private FrameBuffer frameBuffer;
	private Camera droneCamera;
	private byte[] latestCameraImage;

	public VirtualTestbed(AutopilotConfig config) {
		this.config = config;
		Drone drone = new Drone(config, new ArrayRealVector(new double[] {0, 0, -13.3}, false));
		this.addChild(drone);
		Box box = new Box();
		double zDistance = 200.0;
		box.setRelativePosition(new ArrayRealVector(new double[] {zDistance*Math.tan(Math.PI/96.0), zDistance*Math.tan(Math.PI/24.0), -zDistance}, false));
		//box.setRelativePosition(new ArrayRealVector(new double[] {zDistance*Math.tan(Math.PI/96.0), 0, -zDistance}, false));
		//box.setRelativePosition(new ArrayRealVector(new double[] {0, zDistance*Math.tan(Math.PI/24.0), -zDistance}, false));
		//box.setRelativePosition(new ArrayRealVector(new double[] {0, 0, -zDistance}, false));
		this.addChild(box);
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
				output.getLeftWingInclination(), output.getRightWingInclination(), output.getHorStabInclination(), output.getVerStabInclination());
		
		float[] angularAccelerations = drone.getAngularAccelerations(output.getLeftWingInclination(),
				output.getRightWingInclination(), output.getHorStabInclination(), output.getVerStabInclination());
		float heading = drone.getHeading();
		float headingAngularVelocity = drone.getHeadingAngularVelocity();
		float headingAngularAcceleration = angularAccelerations[0];
		float pitch = drone.getPitch();
		float pitchAngularVelocity = drone.getPitchAngularVelocity();
		float pitchAngularAcceleration = angularAccelerations[1];
		float roll = drone.getRoll();
		float rollAngularVelocity = drone.getRollAngularVelocity();
		float rollAngularAcceleration = angularAccelerations[2];
		
		drone.setRelativePosition(position.add(velocity.mapMultiply(dt)).add(acceleration.mapMultiply(Math.pow(dt, 2)/2)));
		drone.setVelocity(velocity.add(acceleration.mapMultiply(dt)));
		
		float newHeading = (float) ((heading + headingAngularVelocity*dt + headingAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI));
		if ((Math.abs(newHeading - 0) < 0.0001) || (Math.abs(newHeading - 2*Math.PI) < 0.0001))
			newHeading = 0;
		else if (newHeading < 0)
			newHeading += (2*Math.PI);
		float newPitch = (float) ((pitch + pitchAngularVelocity*dt + pitchAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI));
		if ((Math.abs(newPitch - 0) < 0.0001) || (Math.abs(newPitch - 2*Math.PI) < 0.0001))
			newPitch = 0;
		else if (newPitch < 0)
			newPitch += (2*Math.PI);
		float newRoll = (float) ((roll + rollAngularVelocity*dt + rollAngularAcceleration*(Math.pow(dt, 2)/2)) % (2*Math.PI));
		if ((Math.abs(newRoll - 0) < 0.0001) || (Math.abs(newRoll - 2*Math.PI) < 0.0001))
			newRoll = 0;
		else if (newRoll < 0)
			newRoll += (2*Math.PI);
		drone.setHeading(newHeading);
		drone.setPitch(newPitch);
		drone.setRoll(0);
		
		drone.setHeadingAngularVelocity(headingAngularVelocity + headingAngularAcceleration*dt);
		drone.setPitchAngularVelocity(pitchAngularVelocity + pitchAngularAcceleration*dt);
		drone.setRollAngularVelocity(rollAngularVelocity + rollAngularAcceleration*dt);
	}
	
	public void setElapsedTime(float elapsedTime) throws IllegalArgumentException {
		if (! isValidElapsedTime(elapsedTime))
			throw new IllegalArgumentException();
		this.elapsedTime = elapsedTime;
	}
	
	private float elapsedTime = 0;
	
	public float getElapsedTime() {
		return elapsedTime;
	}

	public long getBeginSimulation() {
		return beginSimulation;
	}

	private final long beginSimulation = System.currentTimeMillis();
	
	public long getLastUpdate() {
		return lastUpdate;
	}
	
	public static boolean isValidElapsedTime(float elapsedTime) {
		return ((elapsedTime >= 0) & (elapsedTime <= Float.MAX_VALUE));
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
	
	public void update(AutopilotOutputs output) throws IllegalStateException {
		Drone drone = this.getFirstChildOfType(Drone.class);
		Box box = this.getFirstChildOfType(Box.class);
		if ((drone.getRightWingPosition().getDistance(box.getWorldPosition()) < 4.5) 
				|| (drone.getLeftWingPosition().getDistance(box.getWorldPosition()) < 4.5) 
				|| (drone.getEnginePosition().getDistance(box.getWorldPosition()) < 4.5))
			throw new IllegalStateException("simulation has ended");
		long now = System.currentTimeMillis();
		this.setElapsedTime((float)(now - this.getBeginSimulation())/1000f);
		this.moveDrone((float)(now- this.getLastUpdate())/1000f, output);
		this.renderCameraView();
		this.setLastUpdate(now);
	}
	
	public Renderer getRenderer() {
		if (renderer == null)
			renderer = OpenGLRenderer.create();
		return renderer;
	}

	@Override
	public WorldObject getWorldRepresentation() {
		return this;
	}

	public AutopilotInputs getInputs() {
		Drone drone = this.getFirstChildOfType(Drone.class);
		if (latestCameraImage == null)
			renderCameraView();
		return new AutopilotInputs() {
            public byte[] getImage() { return latestCameraImage; }
            public float getX() { return (float) drone.getWorldPosition().getEntry(0); }
            public float getY() { return (float) drone.getWorldPosition().getEntry(1); }
            public float getZ() { return (float) drone.getWorldPosition().getEntry(2); }
            public float getHeading() { return drone.getHeading(); }
            public float getPitch() { return drone.getPitch(); }
            public float getRoll() { return drone.getRoll(); }
            public float getElapsedTime() { return VirtualTestbed.this.getElapsedTime(); }
        };
	}

	private void renderCameraView(){
		if(latestCameraImage == null){
			int pixelCount = config.getNbRows() * config.getNbColumns();
			latestCameraImage = new byte[pixelCount * 3]; //3 because RGB
		}
		renderCameraView(latestCameraImage);
	}

	//Renders the cameraview and stores the result in targetArray
	private void renderCameraView(byte[] targetArray){
		int pixelCount = config.getNbColumns() * config.getNbRows();
		if(targetArray.length < pixelCount * 3){
			throw new IllegalArgumentException("target buffer is too small");
		}

		Renderer renderer = getRenderer();
		if(frameBuffer == null){
			frameBuffer = renderer.createFrameBuffer(config.getNbColumns(), config.getNbRows());
		}
		if(droneCamera == null){
			droneCamera = createDroneCamera();
		}
		renderer.render(this, frameBuffer, droneCamera);
		frameBuffer.readPixels(targetArray);

		//Swap blue and red bytes (BGR -> RGB)
		for(int i = 0; i < pixelCount; i++){
			byte b = targetArray[(i*3)+0];
			targetArray[(i*3)+0] = targetArray[(i*3)+2];
			targetArray[(i*3)+2] = b;
		}
	}

	private Camera createDroneCamera(){
		Camera camera = getRenderer().createCamera();
		camera.setDronesHidden(true);
		Drone drone = this.getFirstChildOfType(Drone.class);
		drone.addChild(camera);
		return camera;
	}
}