package be.kuleuven.cs.robijn.common;

import interfaces.AutopilotConfig;

public class Tyre extends WorldObject {
	
    //  -----------------   //
    //                      //
    //   INITIALISE Tyre    //
    //                      //
    //  -----------------   //
	public Tyre(AutopilotConfig config) throws IllegalArgumentException{
		if (! isValidWheelY(config.getWheelY())) {
			throw new IllegalArgumentException();
		}
		this.wheelY = config.getWheelY();
		
		if (! isValidFrontWheelZ(config.getFrontWheelZ())) {
			throw new IllegalArgumentException();
		}
		this.frontWheelZ = config.getFrontWheelZ();
		
		if(! isValidRearWheelZ(config.getRearWheelZ())) {
			throw new IllegalArgumentException();
		}
		this.rearWheelZ = config.getRearWheelZ();
		
		if(! isValidRearWheelX(config.getRearWheelX())) {
			throw new IllegalArgumentException();
		}
		this.rearWheelX = config.getRearWheelX();
		
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
    //      Tyre ATTRIBUTES      //
    //                           //
    //     -----------------     //
	private final float wheelY;
	private final float frontWheelZ;
	private final float rearWheelZ;
	private final float rearWheelX;
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
	
	public float getFrontWheelZ() {
		return this.frontWheelZ;
	}
	
	public boolean isValidFrontWheelZ(float frontWheelZ) {
		return (frontWheelZ > 0) && (frontWheelZ <= Float.MAX_VALUE);
	}
	
	public float getRearWheelZ() {
		return this.rearWheelZ;
	}
	
	public boolean isValidRearWheelZ(float rearWheelZ) {
		return (rearWheelZ < 0) && (rearWheelZ >= - Float.MAX_VALUE);  // Is negatief, want ten opzichte van middelpunt Drone naar achter gelegen
	}
	
	public float getRearWheelX() {
		return this.rearWheelX;
	}
	
	public boolean isValidRearWheelX(float rearWheelX) {
		return (rearWheelX > 0) && (rearWheelX <= Float.MAX_VALUE);
	}
	
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
}
