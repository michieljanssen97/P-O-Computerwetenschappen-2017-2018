package be.kuleuven.cs.robijn.testbed;

import java.util.ArrayList;

import org.apache.commons.math3.linear.*;
import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.testbed.renderer.OpenGLRenderer;
import p_en_o_cw_2017.*;

/**
 * A class of virtual testbeds.
 * 
 * @author Pieter Vandensande
 */
public class VirtualTestbed extends WorldObject implements TestBed {
	private final AutopilotConfig config;

	private float elapsedTime = 0; //Time between simulation start and latest update, in seconds

	private OpenGLRenderer renderer;
	private FrameBuffer frameBuffer;
	private PerspectiveCamera droneCamera;
	private byte[] latestCameraImage;
	
	private ArrayList<Box> allBoxList = new ArrayList<Box>();

	public VirtualTestbed(AutopilotConfig config, RealVector initialVelocity) {
		this.config = config;
		//Add drone to world
		Drone drone = new Drone(config, initialVelocity);
		this.addChild(drone);

		//Add box to world
		Box box = new Box();
		
		double zDistance = 100.0;
		
		//Case 1
		//box.setRelativePosition(new ArrayRealVector(new double[] {0, 0, -zDistance}, false));
		
		//Case 2
		//box.setRelativePosition(new ArrayRealVector(new double[] {0, zDistance*Math.tan(Math.PI/6.0), -zDistance}, false));
		
		//Case 3
		box.setRelativePosition(new ArrayRealVector(new double[] {0, -zDistance*Math.tan(Math.PI/12.0), -zDistance}, false));
		
		//Case 4
		box.setRelativePosition(new ArrayRealVector(new double[] {zDistance*Math.tan(Math.PI/6.0), 0, -zDistance}, false));
		
		//Case 5
		//box.setRelativePosition(new ArrayRealVector(new double[] {-zDistance*Math.tan(Math.PI/6.0), 0, -zDistance}, false));
		
		//Case 6
		//box.setRelativePosition(new ArrayRealVector(new double[] {zDistance*Math.tan(Math.PI/48.0), zDistance*Math.tan(Math.PI/6.0), -zDistance}, false));

//		box.setRelativePosition(new ArrayRealVector(new double[] {0, 5, -80}, false));		
//		Box box2 = new Box();
//		box2.setRelativePosition(new ArrayRealVector(new double[] {0, 0, -40}, false));
//		this.addChild(box2);
//		allBoxList.add(box2);
		
		this.addChild(box); //add box to Virtual testbed
		allBoxList.add(box); //add box to list of all boxes to reach
		
	}
	
	public ArrayList<Box> getAllBoxes(){
		return allBoxList;
	}
	
	public void removeBoxFromList(Box box) {
		allBoxList.remove(box);
	}

	public boolean update(float secondsSinceStart, float secondsSinceLastUpdate, AutopilotOutputs output){
		Drone drone = this.getFirstChildOfType(Drone.class);
		//Box box = this.getFirstChildOfType(Box.class);
		Box box = getClosestBox(drone);

		float stopDistanceToBox = 4;
		float cubeRadius = (float) 0.5;
		float stopDistanceToCenterBox = stopDistanceToBox + cubeRadius;
		
		//Check if the drone reached a cube
		//true if the center of mass of the drone is in a specified distance of the center of a cube
		if (calculateDistanceToDrone(box, drone) <= stopDistanceToCenterBox){
			//The box should not be taken into account anymore, it is already reached
			removeBoxFromList(box); 
			
			//Remove the reached box from the testbed
			removeChild(box);
			
			//Stop the simulation if all boxes are handled
			if(getAllBoxes().isEmpty()) {
				return true;
			}
		}

		this.setElapsedTime(secondsSinceStart);
		this.moveDrone(secondsSinceLastUpdate, output);
		this.renderCameraView();

		return false;
	}
	
	/**
	 * Get the box closest to the current position of the drone
	 */
	public Box getClosestBox(Drone drone) {
		ArrayList<Box> allBoxes = new ArrayList<>(getAllBoxes());
		
		Box closestBox = allBoxes.get(0);
		double distanceClosestBoxToDrone = calculateDistanceToDrone(closestBox, drone);
		
		allBoxes.remove(0); //first element is set as default, so should not be checked anymore
		for (Box b:allBoxes) {
			double distanceCurrentBoxToDrone = calculateDistanceToDrone(b, drone);
			if (distanceCurrentBoxToDrone < distanceClosestBoxToDrone) {
				//set the new box as closest box
				closestBox = b;
				distanceClosestBoxToDrone = distanceCurrentBoxToDrone;
			}
		}
		
		return closestBox;
	}
	
	/**
	 * Calculate the distance of the given box to the current position of the drone
	 * @param b
	 *        The box for which to calculate the distance to the drone
	 */
	public double calculateDistanceToDrone(Box b, Drone drone) {
		RealVector posBox = b.getWorldPosition();
		RealVector posDrone = drone.getWorldPosition();
		
		return posBox.getDistance(posDrone);
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

	///////////////
	/// PHYSICS ///
	///////////////
	
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
	public void moveDrone(float secondsSinceLastUpdate, AutopilotOutputs output) throws IllegalArgumentException, IllegalStateException {
		if (secondsSinceLastUpdate < 0)
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
		
//		FirstOrderIntegrator dp853 = new DormandPrince853Integrator(1.0e-8, 100.0, 1.0e-10, 1.0e-10);
//		FirstOrderDifferentialEquations ode = new SystemDifferentialEquations(drone, output);
//		double[] y = new double[] { drone.getWorldPosition().getEntry(0), drone.getVelocity().getEntry(0), 
//				drone.getWorldPosition().getEntry(1), drone.getVelocity().getEntry(1),
//				drone.getWorldPosition().getEntry(2), drone.getVelocity().getEntry(2),
//				drone.getHeading(), drone.getHeadingAngularVelocity(),
//				drone.getPitch(), drone.getPitchAngularVelocity(),
//				drone.getRoll(), drone.getRollAngularVelocity() };
//		dp853.integrate(ode, secondsSinceStart - secondsSinceLastUpdate, y, secondsSinceStart, y);
		
//		drone.setRelativePosition(new ArrayRealVector(new double[] {y[0], y[2], y[4]}, false));
//		drone.setVelocity(new ArrayRealVector(new double[] {y[1], y[3], y[5]}, false));
		drone.setRelativePosition(position.add(velocity.mapMultiply(secondsSinceLastUpdate)).add(acceleration.mapMultiply(Math.pow(secondsSinceLastUpdate, 2)/2)));
		drone.setVelocity(velocity.add(acceleration.mapMultiply(secondsSinceLastUpdate)));
		
		float newHeading = (float) ((heading + headingAngularVelocity*secondsSinceLastUpdate + headingAngularAcceleration*(Math.pow(secondsSinceLastUpdate, 2)/2)) % (2*Math.PI));
//		float newHeading = (float) y[6];
		if (newHeading < 0)
			newHeading += (2*Math.PI);
		if (newHeading >= 2*Math.PI)
			newHeading = 0;
		float newPitch = (float) ((pitch + pitchAngularVelocity*secondsSinceLastUpdate + pitchAngularAcceleration*(Math.pow(secondsSinceLastUpdate, 2)/2)) % (2*Math.PI));
//		float newPitch = (float) y[8];
		if (newPitch < 0)
			newPitch += (2*Math.PI);
		if (newPitch >= 2*Math.PI)
			newPitch = 0;
		float newRoll = (float) ((roll + rollAngularVelocity*secondsSinceLastUpdate + rollAngularAcceleration*(Math.pow(secondsSinceLastUpdate, 2)/2)) % (2*Math.PI));
//		float newRoll = (float) y[10];
		if (newRoll < 0)
			newRoll += (2*Math.PI);
		if (newRoll >= 2*Math.PI)
			newRoll = 0;
		drone.setHeading(newHeading);
		drone.setPitch(newPitch);
		drone.setRoll(newRoll);
		
		drone.setHeadingAngularVelocity(headingAngularVelocity + headingAngularAcceleration*secondsSinceLastUpdate);
		drone.setPitchAngularVelocity(pitchAngularVelocity + pitchAngularAcceleration*secondsSinceLastUpdate);
		drone.setRollAngularVelocity(rollAngularVelocity + rollAngularAcceleration*secondsSinceLastUpdate);
//		drone.setHeadingAngularVelocity((float) y[7]);
//		drone.setPitchAngularVelocity((float) y[9]);
//		drone.setRollAngularVelocity((float) y[11]);
	}

	///////////////////////
	/// TIME MANAGEMENT ///
	///////////////////////
	
	public void setElapsedTime(float elapsedTime) throws IllegalArgumentException {
		if (! isValidElapsedTime(elapsedTime))
			throw new IllegalArgumentException();
		this.elapsedTime = elapsedTime;
	}

	public float getElapsedTime() {
		return elapsedTime;
	}

	public static boolean isValidElapsedTime(float elapsedTime) {
		return ((elapsedTime >= 0) & (elapsedTime <= Float.MAX_VALUE));
	}
	
	/////////////////
	/// RENDERING ///
	/////////////////
	
	public Renderer getRenderer() {
		if (renderer == null)
			renderer = OpenGLRenderer.create();
		return renderer;
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

	private PerspectiveCamera createDroneCamera(){
		PerspectiveCamera camera = getRenderer().createPerspectiveCamera();
		camera.setHorizontalFOV((float)Math.toRadians(config.getHorizontalAngleOfView()));
		camera.setVerticalFOV((float)Math.toRadians(config.getVerticalAngleOfView()));
		camera.setDronesHidden(true);
		Drone drone = this.getFirstChildOfType(Drone.class);
		drone.addChild(camera);
		return camera;
	}
}