package be.kuleuven.cs.robijn.testbed;

import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.testbed.renderer.OpenGLRenderer;
import be.kuleuven.cs.robijn.testbed.renderer.bmfont.Label3D;
import be.kuleuven.cs.robijn.worldObjects.Camera;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import be.kuleuven.cs.robijn.worldObjects.PerspectiveCamera;
import be.kuleuven.cs.robijn.worldObjects.WorldObject;
import be.kuleuven.cs.robijn.worldObjects.Package;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

import java.util.HashMap;
import java.util.List;

public class VirtualTestbed implements TestBed {
	//Simulation
	private final AutopilotInputs[] inputs;
	private final TestbedSimulation simulation;
	private final WorldObject world;
	private final List<Drone> drones;

	//Renderer
	private OpenGLRenderer renderer;
	private FrameBuffer frameBuffer;
	private final HashMap<Drone, byte[]> latestCameraImage = new HashMap<>();
	private final boolean AUTOPILOT_CAMERA_ENABLED = false;

	public VirtualTestbed(SimulationSettings settings) {
		world = new WorldObject();

		//Create airports and drones
		SimulationBuilder.buildSimulation(settings, world);
		drones = world.getChildrenOfType(Drone.class);

		//Set initial autopilotinputs
		List<Drone> drones = world.getChildrenOfType(Drone.class);
		inputs = new AutopilotInputs[settings.getDrones().length];
		for (int i = 0; i < settings.getDrones().length; i++) {
			Drone drone = drones.get(i);
			byte[] image = null;
			if(AUTOPILOT_CAMERA_ENABLED){
				image = renderCameraView(drone);
			}
			inputs[i] = new VirtualTestbed.TestbedAutopilotInputs(drone, image, 0);
		}

		simulation = new TestbedSimulation(world);
	}

	@Override
	public boolean update(float secondsSinceStart, float secondsSinceLastUpdate, AutopilotOutputs[] outputs) {
		//Update drones
		Package.assignPackages();
		for(int i = 0; i < drones.size(); i++){
			Drone drone = drones.get(i);
			simulation.updateDrone(drone, secondsSinceStart, secondsSinceLastUpdate, outputs[i]);
			byte[] image = renderCameraView(drone);
			inputs[i] = new VirtualTestbed.TestbedAutopilotInputs(drone, image, secondsSinceStart);
		}

		return simulation.isSimulationFinished();
	}

	@Override
	public AutopilotInputs getInputs(int i) {
		return inputs[i];
	}


	@Override
	public WorldObject getWorldRepresentation() {
		return world;
	}

	/////////////////
	/// RENDERING ///
	/////////////////

	public Renderer getRenderer() {
		if (renderer == null)
			renderer = OpenGLRenderer.create();
		return renderer;
	}

	private byte[] renderCameraView(Drone drone){
		byte[] buffer = latestCameraImage.computeIfAbsent(drone, d -> {
			int pixelCount = drone.getConfig().getNbRows() * drone.getConfig().getNbColumns();
			return new byte[pixelCount * 3]; //3 because RGB
		});
		renderCameraView(drone, buffer);
		return buffer;
	}

	//Renders the cameraview and stores the result in targetArray
	private void renderCameraView(Drone drone, byte[] targetArray){
		int pixelCount = drone.getConfig().getNbColumns() * drone.getConfig().getNbRows();
		if(targetArray.length < pixelCount * 3){
			throw new IllegalArgumentException("target buffer is too small");
		}

		Renderer renderer = getRenderer();

		if(frameBuffer != null &&
			(frameBuffer.getWidth() != drone.getConfig().getNbColumns() ||
			frameBuffer.getHeight() != drone.getConfig().getNbRows())
		){
			frameBuffer.close();
			frameBuffer = null;
		}

		if(frameBuffer == null){
			frameBuffer = renderer.createFrameBuffer(drone.getConfig().getNbColumns(), drone.getConfig().getNbRows());
		}

		Camera droneCamera = drone.getFirstChildOfType(Camera.class);
		if(droneCamera == null){
			droneCamera = createDroneCamera(drone);
		}
		renderer.startRender(world, frameBuffer, droneCamera);
		frameBuffer.readPixels(targetArray);

		//Swap blue and red bytes (BGR -> RGB)
		for(int i = 0; i < pixelCount; i++){
			byte b = targetArray[(i*3)+0];
			targetArray[(i*3)+0] = targetArray[(i*3)+2];
			targetArray[(i*3)+2] = b;
		}
	}

	private PerspectiveCamera createDroneCamera(Drone drone){
		PerspectiveCamera camera = getRenderer().createPerspectiveCamera();
		camera.setHorizontalFOV(drone.getConfig().getHorizontalAngleOfView());
		camera.setVerticalFOV(drone.getConfig().getVerticalAngleOfView());
        camera.addVisibilityFilter(obj -> obj != drone); //Hide drone from itself
        camera.addVisibilityFilter(obj -> !(obj instanceof Label3D)); //Hide labels
		camera.addVisibilityFilter(Camera.HIDE_DEBUG_OBJECTS);
		drone.addChild(camera);
		return camera;
	}

	///////////////////////
	/// AUTOPILOTINPUTS ///
	///////////////////////

	class TestbedAutopilotInputs implements AutopilotInputs{
		private final byte[] image;
		private final float x, y, z;
		private final float heading, pitch, roll;
		private final float elapsedTime;

		public TestbedAutopilotInputs(Drone drone, byte[] image, float elapsedTime) {
			this.image = image;

			this.x = (float)drone.getWorldPosition().getEntry(0);
			this.y = (float)drone.getWorldPosition().getEntry(1);
			this.z = (float)drone.getWorldPosition().getEntry(2);

			this.heading = drone.getHeading();
			this.pitch = drone.getPitch();
			this.roll = drone.getRoll();

			this.elapsedTime = elapsedTime;
		}

		@Override
		public byte[] getImage() {
			return image;
		}

		@Override
		public float getX() {
			return x;
		}

		@Override
		public float getY() {
			return y;
		}

		@Override
		public float getZ() {
			return z;
		}

		@Override
		public float getHeading() {
			return heading;
		}

		@Override
		public float getPitch() {
			return pitch;
		}

		@Override
		public float getRoll() {
			return roll;
		}

		@Override
		public float getElapsedTime() {
			return elapsedTime;
		}
	}
}