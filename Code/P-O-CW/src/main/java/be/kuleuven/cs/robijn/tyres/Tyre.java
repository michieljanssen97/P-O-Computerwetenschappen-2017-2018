package be.kuleuven.cs.robijn.tyres;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.common.exceptions.CrashException;
import be.kuleuven.cs.robijn.common.math.VectorMath;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import interfaces.AutopilotConfig;

public abstract class Tyre extends WorldObject {
	
    //  -----------------   //
    //                      //
    //   INITIALISE TYRE    //
    //                      //
    //  -----------------   //
	public Tyre(AutopilotConfig config) throws IllegalArgumentException{
		if (! isValidWheelY(config.getWheelY())) {
			throw new IllegalArgumentException();
		}
		this.wheelY = config.getWheelY();
		
		if(! isValidTyreSlope(config.getTyreSlope())) {
			throw new IllegalArgumentException();
		}
		this.tyreSlope = config.getTyreSlope();
		
		if(! isValidDampSlope(config.getDampSlope())) {
			throw new IllegalArgumentException();
		}
		this.dampSlope = config.getDampSlope();
		
		if(! isValidTyreRadius(config.getTyreRadius())) {
			throw new IllegalArgumentException();
		}
		this.tyreRadius = config.getTyreRadius();
		
		if(! isValidRMax(config.getRMax())) {
			throw new IllegalArgumentException();
		}
		this.RMax = config.getRMax();
		
		if(! isValidFcMax(config.getFcMax())) {
			throw new IllegalArgumentException();
		}
		this.FcMax = config.getFcMax();
	}

	//     -----------------     //
    //                           //
    //      TYRE ATTRIBUTES      //
    //                           //
    //     -----------------     //
    protected float wheelX;
	private final float wheelY;
	protected float wheelZ;
	private float tyreSlope;
	private float dampSlope;
	private final float tyreRadius;
	private final float RMax;
	private final float FcMax;
	
	
	public float getWheelY() {
		return this.wheelY;
	}
	
	public boolean isValidWheelY(float wheelY) {
		return (wheelY < 0) && (Float.isFinite(wheelY));
	}
	
	public float getWheelZ() {
		return this.wheelZ;
	}
	
	public abstract boolean isValidWheelZ(float wheelZ);
	
	public float getWheelX() {
		return this.wheelX;
	}
	
	public abstract boolean isValidWheelX(float rearWheelX, float wingX);
	
	public float getTyreSlope() {
		return this.tyreSlope;
	}
	
	public boolean isValidTyreSlope(float tyreSlope) {
		return (tyreSlope > 0) && (tyreSlope <= Float.MAX_VALUE);
	}
	
	public float getDampSlope() {
		return this.dampSlope;
	}
	
	public boolean isValidDampSlope(float dampSlope) {
		return (dampSlope > 0) && (dampSlope <= Float.MAX_VALUE);
	}
	
	public float getTyreRadius() {
		return this.tyreRadius;
	}
	
	public boolean isValidTyreRadius(float tyreRadius) {
		return (tyreRadius > 0) && (tyreRadius <= Float.MAX_VALUE);
	}
	
	public float getRMax() {
		return this.RMax;
	}
	
	public boolean isValidRMax(float RMax) {
		return (RMax > 0) && (RMax <= Float.MAX_VALUE);
	}
	
	public float getFcMax() {
		return this.FcMax;
	}
	
	public boolean isValidFcMax(float FcMax) {
		return (FcMax > 0) && (FcMax <= 1);
	}

    //     -----------------     //
    //                           //
    //       Other Methods       //
    //                           //
    //     -----------------     //
	
	public RealVector getRelativePosition(Drone drone) {
		return drone.transformationToWorldCoordinates(
				new ArrayRealVector(new double[] {this.getWheelX(), this.getWheelY(), this.getWheelZ()}, false));
	}
	
	public RealVector getRelativePositionTyreGround(Drone drone) {
		RealVector vector = new ArrayRealVector(new double[] {0, -this.getDistanceCenterTyreAndGround(this.getD(drone)), 0}, false);
		vector = drone.transformationToWorldCoordinates(vector);
		return this.getRelativePosition(drone).add(vector);
	}
	
	public RealVector getPosition(Drone drone) {
		return drone.getWorldPosition().add(this.getRelativePosition(drone));
	}
	
	public float getDistanceCenterTyreAndGround(float D) {
		return this.getTyreRadius() - D;
	}
	
	public float getD(Drone drone) throws CrashException {
		if (this.getPosition(drone).getEntry(1) <=0)
			throw new CrashException();
		float d = (float) (this.getTyreRadius() - this.getPosition(drone).getEntry(1));
		if (d < 0)
			d = 0;
		else {
			GroundPlane g = this.getParent().getParent().getFirstChildOfType(GroundPlane.class);
//			if (g.isGrass(new ArrayRealVector(new double[]{this.getPosition(drone).getEntry(0), 0, this.getPosition(drone).getEntry(2)}, false))) //TODO zet terug aan
//				throw new CrashException();
		}
		return d;
	}
	
	public RealVector getVelocityTyre(Drone drone) {
		return drone.getVelocity().add(
				VectorMath.crossProduct(
						drone.getHeadingAngularVelocityVector()
						.add(drone.getPitchAngularVelocityVector())
						.add(drone.getRollAngularVelocityVector())
						, this.getRelativePositionTyreGround(drone)));
		
	}
	public float getVelocityD(Drone drone) {
		return (float) -drone.transformationToDroneCoordinates(this.getVelocityTyre(drone)).getEntry(1);
	}
	
	public float getLateralVelocity(Drone drone) {
		return (float) drone.transformationToDroneCoordinates(this.getVelocityTyre(drone)).getEntry(0);
	}
	
	public abstract RealVector getTyreForce(Drone drone, float wheelBrakeForce);
	
	public RealVector getTyreMoment(Drone drone, float wheelBrakeForce) {
		return VectorMath.crossProduct(
				   drone.transformationToDroneCoordinates(this.getRelativePositionTyreGround(drone)), //distance
				   drone.transformationToDroneCoordinates(this.getTyreForce(drone, wheelBrakeForce)) //forces
				   );
	}
}

