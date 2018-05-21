package be.kuleuven.cs.robijn.testbed;

import be.kuleuven.cs.robijn.common.airports.AirportPackage;
import be.kuleuven.cs.robijn.common.airports.Gate;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.common.math.SystemDifferentialEquations;
import be.kuleuven.cs.robijn.tyres.Tyre;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import interfaces.AutopilotOutputs;

import java.util.Optional;

public class TestbedSimulation {
    private final WorldObject world;
    private float elapsedTime = 0;

    public TestbedSimulation(WorldObject world){
        this.world = world;
    }

    public void updateDrone(Drone drone, float secondsSinceStart, float secondsSinceLastUpdate, AutopilotOutputs output){
		this.setElapsedTime(secondsSinceStart);
		this.moveDrone(drone, secondsSinceLastUpdate, output);
		this.checkForPackages(drone);
    }

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
	
    /**
	 * Method to move the drone of this virtual testbed.
	 * The position and the velocity of the center of mass, the heading, the pitch, the roll
	 * and the angular velocities get updated
	 * using the outputs from the autopilot (thrust, leftWingInclination,
	 * rightWingInclination, horStabInclination, verStabInclination).
	 *
	 * @param  secondsSinceLastUpdate
	 * 		   Time duration (in seconds) to move this drone.
	 * @throws IllegalArgumentException
	 * 		   The given time duration is negative.
	 * 		 | dt < 0
	 * @throws IllegalStateException
	 *         This virtual testbed has no drone.
	 *         drone == null
	 */
	public void moveDrone(Drone drone, float secondsSinceLastUpdate, AutopilotOutputs output) throws IllegalArgumentException, IllegalStateException {
		boolean useDiffEquations = true;
		
		if (secondsSinceLastUpdate < 0)
			throw new IllegalArgumentException();
		if (drone == null)
			throw new IllegalStateException("this virtual testbed has no drone");
		RealVector position = drone.getWorldPosition();
		RealVector velocity = drone.getVelocity();
		RealVector acceleration = drone.getAcceleration(output.getThrust(),
				output.getLeftWingInclination(), output.getRightWingInclination(), output.getHorStabInclination(), output.getVerStabInclination(),
				output.getFrontBrakeForce(), output.getLeftBrakeForce(), output.getRightBrakeForce());
		
		float[] angularAccelerations = drone.getAngularAccelerations(output.getLeftWingInclination(),
				output.getRightWingInclination(), output.getHorStabInclination(), output.getVerStabInclination(),
				output.getFrontBrakeForce(), output.getLeftBrakeForce(), output.getRightBrakeForce());
		float heading = drone.getHeading();
		float headingAngularVelocity = drone.getHeadingAngularVelocity();
		float headingAngularAcceleration = angularAccelerations[0];
		float pitch = drone.getPitch();
		float pitchAngularVelocity = drone.getPitchAngularVelocity();
		float pitchAngularAcceleration = angularAccelerations[1];
		float roll = drone.getRoll();
		float rollAngularVelocity = drone.getRollAngularVelocity();
		float rollAngularAcceleration = angularAccelerations[2];
		
		if (useDiffEquations){
//			FirstOrderIntegrator dp853 = new DormandPrince853Integrator(1.0e-8, 100.0, 1.0e-5, 1.0e-5);
			FirstOrderIntegrator rk4 = new ClassicalRungeKuttaIntegrator(secondsSinceLastUpdate);
			//FirstOrderIntegrator euler = new EulerIntegrator(secondsSinceLastUpdate);
			FirstOrderDifferentialEquations ode = new SystemDifferentialEquations(drone, output);
			double[] y = new double[] { drone.getWorldPosition().getEntry(0), drone.getVelocity().getEntry(0), 
					drone.getWorldPosition().getEntry(1), drone.getVelocity().getEntry(1),
					drone.getWorldPosition().getEntry(2), drone.getVelocity().getEntry(2),
					drone.getHeading(), drone.getHeadingAngularVelocity(),
					drone.getPitch(), drone.getPitchAngularVelocity(),
					drone.getRoll(), drone.getRollAngularVelocity() };
			rk4.integrate(ode, 0.0, y, secondsSinceLastUpdate, y);
			
			drone.setRelativePosition(new ArrayRealVector(new double[] {y[0], y[2], y[4]}, false));
			drone.setVelocity(new ArrayRealVector(new double[] {y[1], y[3], y[5]}, false));
			
			float newHeading = (float) y[6];
			if (newHeading < 0)
				newHeading += (2*Math.PI);
			if (newHeading >= 2*Math.PI)
				newHeading = 0;
			float newPitch = (float) y[8];
			if (newPitch < 0)
				newPitch += (2*Math.PI);
			if (newPitch >= 2*Math.PI)
				newPitch = 0;
			float newRoll = (float) y[10];
			if (newRoll < 0)
				newRoll += (2*Math.PI);
			if (newRoll >= 2*Math.PI)
				newRoll = 0;
			drone.setHeading(newHeading);
			drone.setPitch(newPitch);
			drone.setRoll(newRoll);
			
			drone.setHeadingAngularVelocity((float) y[7]);
			drone.setPitchAngularVelocity((float) y[9]);
			drone.setRollAngularVelocity((float) y[11]);
			
			for (Tyre tyres: drone.getChildrenOfType(Tyre.class)) {
				@SuppressWarnings("unused")
				float d = tyres.getD(drone);
			}
		}

		else {
			drone.setRelativePosition(position.add(velocity.mapMultiply(secondsSinceLastUpdate)).add(acceleration.mapMultiply(Math.pow(secondsSinceLastUpdate, 2)/2)));
			drone.setVelocity(velocity.add(acceleration.mapMultiply(secondsSinceLastUpdate)));
			
			float newHeading = (float) ((heading + headingAngularVelocity*secondsSinceLastUpdate + headingAngularAcceleration*(Math.pow(secondsSinceLastUpdate, 2)/2)) % (2*Math.PI));
			if (newHeading < 0)
				newHeading += (2*Math.PI);
			if (newHeading >= 2*Math.PI)
				newHeading = 0;
			float newPitch = (float) ((pitch + pitchAngularVelocity*secondsSinceLastUpdate + pitchAngularAcceleration*(Math.pow(secondsSinceLastUpdate, 2)/2)) % (2*Math.PI));
			if (newPitch < 0)
				newPitch += (2*Math.PI);
			if (newPitch >= 2*Math.PI)
				newPitch = 0;
			float newRoll = (float) ((roll + rollAngularVelocity*secondsSinceLastUpdate + rollAngularAcceleration*(Math.pow(secondsSinceLastUpdate, 2)/2)) % (2*Math.PI));
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
			
			for (Tyre tyres: drone.getChildrenOfType(Tyre.class)) {
				@SuppressWarnings("unused")
				float d = tyres.getD(drone);
			}
		}
	}

	private void checkForPackages(Drone drone) {
		//Drone must be on the ground and moving slower than 1 m/s
		if(drone.getVelocity().getNorm() > 1 || drone.getWorldPosition().getEntry(1) > 1.5){
			return;
		}

		//What gate is the drone currently at
		Optional<Gate> gate = world.getDescendantsStream()
				.filter(c -> c instanceof Gate).map(c -> (Gate)c)
				.filter(g -> g.isDroneAbove(drone))
				.findFirst();

		if(gate.isPresent()){
			AirportPackage p = gate.get().getPackage();
			if(drone.getPackage() != null && drone.getPackage().getDestination().getWorldPosition().getDistance(gate.get().getWorldPosition()) < (gate.get().getAirport().width + 10)){
				drone.getPackage().markAsDelivered();
			}else if(drone.getPackage() == null && gate.get().hasPackage() && p != null && p.droneCanStart(drone, p.getOrigin(), p.getDestination(), p.getOrigin().getAirport())){
				gate.get().getPackage().markAsInTransit(drone);
			}
		}
	}
	
    public boolean isSimulationFinished(){
        return false;
    }
}