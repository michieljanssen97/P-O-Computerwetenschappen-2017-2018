package be.kuleuven.cs.robijn.testbed;

import java.util.ArrayList;

import org.apache.commons.math3.linear.*;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.experiments.ExpEquations;
import be.kuleuven.cs.robijn.testbed.renderer.OpenGLRenderer;
import interfaces.*;

import java.util.List;
import java.util.Optional;

/**
 * A class of virtual testbeds.
 * 
 * @author Pieter Vandensande
 */
public class VirtualTestbed extends WorldObject implements TestBed {
	private final AutopilotConfig config;
	public boolean randomCubesGenerated = false;

	private float elapsedTime = 0; //Time between simulation start and latest update, in seconds

	private OpenGLRenderer renderer;
	private FrameBuffer frameBuffer;
	private PerspectiveCamera droneCamera;
	private byte[] latestCameraImage;
	
	private int VTUpdatesSinceChartUpdates = 0;
	private boolean drawChart = true;
	private String type = "heading".toLowerCase(); //heading, pitch,...       toLowerCase necessary if you accidentally write capitals
	
	private ArrayList<Box> boxesToFlyTo = new ArrayList<>();

	public VirtualTestbed(List<Box> boxes, AutopilotConfig config, RealVector initialVelocity) {
		this.config = config;
		//Add drone to world
		Drone drone = new Drone(config, initialVelocity);
		this.addChild(drone);

		//Add boxes to world
		for (Box box : boxes){
			this.addChild(box);
		}
		boxesToFlyTo.addAll(boxes);
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
			boxesToFlyTo.remove(box);
			
			//Remove the reached box from the testbed
			removeChild(box);
			
			//Stop the simulation if all boxes are handled
			if(boxesToFlyTo.isEmpty()) {
				if (drawChart) {
					ExpEquations.drawMain(type); //draw chart of 'type' when simulation stops
				}
				return true;
			}
		}

		this.setElapsedTime(secondsSinceStart);
		this.moveDrone(secondsSinceLastUpdate, output);
		this.renderCameraView();
		
		//only execute if there must be a chart of values in time
		if(drawChart) {
			VTUpdatesSinceChartUpdates++;
			if(VTUpdatesSinceChartUpdates >= 5 ) {//Update the chart every x iterations of the VTestbed			
				updateSeriesForFloat(type, box, drone);
				//updateSeriesForVector(drone.getHeadingAngularVelocityVector());
				VTUpdatesSinceChartUpdates = 0;
			}
		}
				
		return false;
	}
	
	RealVector getAbs(RealVector v) {
		double x = Math.abs(v.getEntry(0));
		double y = Math.abs(v.getEntry(1));
		double z = Math.abs(v.getEntry(2));
		
		return new ArrayRealVector(new double[] {x,y,z},false);
	}
	
	/**
	 * Get the box closest to the current position of the drone
	 */
	public Box getClosestBox(Drone drone) {
		Optional<Box> closestBox = boxesToFlyTo.stream().min((boxA, boxB) -> {
			double distanceBoxAToDrone = calculateDistanceToDrone(boxA, drone);
			double distanceBoxBToDrone = calculateDistanceToDrone(boxB, drone);
			return Double.compare(distanceBoxAToDrone, distanceBoxBToDrone);
		});
		
		return closestBox.orElse(null);
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
	
	/////////////////////////////
	/// Experiments for Chart ///
	/////////////////////////////
	
	static TimeSeries series = new TimeSeries( "timeSeries" );
	Second current = new Second( ); 
	
	public static TimeSeries getDataSet() {
		return series;
	}
	
	public void updateSeriesForVector(RealVector value) {
		try {
			series.add(current, new Double( value.getEntry(0) ) );
			current = ( Second ) current.next( ); 
			
		} catch ( SeriesException e ) {
            System.err.println("Error adding to series");
         }
	}
	
	public void updateSeriesForFloat(String type, Box box, Drone drone) {
		float value = (float) getAnglesDifference(type,box, drone); //x-value
		System.out.println(value);

		try {
			series.add(current, new Double( value ) );
			current = ( Second ) current.next( ); 
			
		} catch ( SeriesException e ) {
            System.err.println("Error adding to series");
         }
	}
	
	public static XYDataset createDatasetForChart() {
		
		return new TimeSeriesCollection(getDataSet());
	}
	
	/**
	 * Get the angles from the current place of the plane to the closest cube
	 * @param inputs
	 * @return
	 */
	public double getAnglesDifference(String type, Box box, Drone drone) throws IllegalArgumentException {
		double precision = 0.01;
		RealVector droneCo = drone.getWorldPosition();
		RealVector boxCo = box.getWorldPosition();
		
		//Vector containing the differences in x-,y- and z-coordinate between the drone and the cube
		RealVector distanceVector= new ArrayRealVector(new double[] {
				Math.abs(droneCo.getEntry(0) - boxCo.getEntry(0)),
				Math.abs(droneCo.getEntry(1) - boxCo.getEntry(1)),
				Math.abs(droneCo.getEntry(2) - boxCo.getEntry(2))
				},false);
		
		//Vector containing the heading, pitch and roll angles of the drone
//		RealVector droneRotation = new ArrayRealVector(new double[] {drone.getHeading(), drone.getPitch(), drone.getRoll()},false);     	
//		
//		RealVector horVector   = new ArrayRealVector(new double[] {distanceVector.getEntry(0),0,0},false);
//		RealVector verVector   = new ArrayRealVector(new double[] {0,distanceVector.getEntry(1),0},false);
//		RealVector depthVector = new ArrayRealVector(new double[] {0,0,distanceVector.getEntry(2)},false);
//		
//		RealVector distanceAngularVector = new ArrayRealVector(new double[] {
//				(distanceVector.cosine(horVector)),
//				(distanceVector.cosine(verVector)),
//				(distanceVector.cosine(depthVector)),
//				},false);
//		
//		RealVector necessaryRotation = getAbs(droneRotation.subtract(distanceAngularVector));
//      	return necessaryRotation;
		
		if (type == "heading") {
			double angle = Math.atan(distanceVector.getEntry(0) / Math.abs(distanceVector.getEntry(2)));
			double heading = drone.getHeading();
			if (heading > Math.PI) {
				heading -= 2*Math.PI;
			}
			double diffAngle = Math.abs(angle - heading);
			if (diffAngle <= precision) {
				return 0;
			}
			return diffAngle;
		}
		
		else if (type == "pitch") {
			double angle = Math.atan(distanceVector.getEntry(1) /Math.abs( distanceVector.getEntry(2))); 
			double pitch = drone.getPitch();
			if (pitch > Math.PI) {
				pitch -= 2*Math.PI;
			}
			double diffAngle =  Math.abs(angle - pitch);
			if (diffAngle <= precision) {
				return 0;
			}
			return diffAngle;
		}
		
		//else:
		throw new IllegalArgumentException();

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
		
//		FirstOrderIntegrator dp853 = new DormandPrince853Integrator(1.0e-8, 100.0, 1.0e-5, 1.0e-5);
//		FirstOrderDifferentialEquations ode = new SystemDifferentialEquations(drone, output);
//		double[] y = new double[] { drone.getWorldPosition().getEntry(0), drone.getVelocity().getEntry(0), 
//				drone.getWorldPosition().getEntry(1), drone.getVelocity().getEntry(1),
//				drone.getWorldPosition().getEntry(2), drone.getVelocity().getEntry(2),
//				drone.getHeading(), drone.getHeadingAngularVelocity(),
//				drone.getPitch(), drone.getPitchAngularVelocity(),
//				drone.getRoll(), drone.getRollAngularVelocity() };
//		dp853.integrate(ode, 0.0, y, secondsSinceLastUpdate, y);
		
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
		camera.setHorizontalFOV(config.getHorizontalAngleOfView());
		camera.setVerticalFOV(config.getVerticalAngleOfView());
		camera.setDronesHidden(true);
		Drone drone = this.getFirstChildOfType(Drone.class);
		drone.addChild(camera);
		return camera;
	}
}