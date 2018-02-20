package be.kuleuven.cs.robijn.common;

import interfaces.AutopilotConfig;

public abstract class Tyre extends WorldObject {
	
    //  -----------------   //
    //                      //
    //   INITIALISE TYRE    //
    //                      //
    //  -----------------   //
	public Tyre(AutopilotConfig config) throws IllegalArgumentException{
		this.droneID = config.getDroneID();
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
    protected String droneID;
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
		return (wheelY > 0) && (wheelY <= Float.MAX_VALUE);
	}
	
	public float getWheelZ() {
		return this.wheelZ;
	}
	
	public boolean isValidWheelZ(float wheelZ) {
		return (wheelZ > 0) && (wheelZ <= Float.MAX_VALUE);
	}
	
	public float getWheelX() {
		return this.wheelX;
	}
	
	public abstract boolean isValidWheelX(float rearWheelX);
	
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
	
	public float getDistanceCenterTyreAndGround(float D) {
		return this.getTyreRadius() - D;
	}
	
	/**
	 * Calculate the Total Force in the radial direction of the tyre
	 * @param   D
	 * 			De ingedrukte afstand van de band
	 * @return
	 * 			The total force in the radial distance
	 */
	public float forceN(float D) {
		float derivatieveD = (Float) null; //TODO
		
		float minValue = 0;
		float totalForce = this.getTyreSlope() * D + this.getDampSlope() * derivatieveD;
		
		return Math.max(minValue, totalForce);
		
	}
	
	
	
}

