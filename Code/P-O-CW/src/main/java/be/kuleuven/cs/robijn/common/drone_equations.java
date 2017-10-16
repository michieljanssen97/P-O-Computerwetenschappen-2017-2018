package be.kuleuven.cs.robijn.common;

public class drone_equations {
	private final double GRAVITY_CONSTANT = 9.81; // gravitional constant g
	private final int NB_AXES = 3; // x-, y- and z-axis
	
	private float engineMass;
	private float tailSize;
	private float wingX;
	private float wingMass;
	private float maxThrust;
	private float maxAOA;
	private float wingLiftSlope;
	private float horStabLiftSlope;
	private float verStabLiftSlope;
	private float horizontalAngleOfView;
	private float tailMass;
	private float verticalAngleOfView;
	private int nbColumns;
	private int nbRows;
	private Vector3f enginePos = new Vector3f();
	
	
	/*
	 * Create a drone
	 */
	public drone_equations createDrone(float wingX, float tailSize, float engineMass, float wingMass, float tailMass, float maxThrust, float maxAOA, 
			float wingLiftSlope, float horStabLiftSlope, float verStabLiftSlope,float horizontalAngleOfAttack, float verticalAngleOfView,
			int nbColumns, int nbRows) {
		
		this.wingX = wingX;
		this.tailSize = tailSize;
		this.engineMass = engineMass;
		this.wingMass = wingMass;
		this.tailMass = tailMass;
		this.maxThrust = maxThrust;
		this.maxAOA = maxAOA;
		this.wingLiftSlope = wingLiftSlope;
		this.horStabLiftSlope = horStabLiftSlope;
		this.verStabLiftSlope = verStabLiftSlope;
		this.horizontalAngleOfView = horizontalAngleOfAttack;
		this.verticalAngleOfView = verticalAngleOfView;
		this.nbColumns = nbColumns;
		this.nbRows = nbRows;
		this.enginePos = null;
		
		return this;
	}
	
	/*
	 * getters for the drone's attributes
	 */
	public float getWingX() {
		return this.wingX;
	}
	
	public float getTailSize() {
		return this.tailSize;
	}
	
	public float getEngineMass() {
		return this.engineMass;
	}
	
	public float getWingMass() {
		return this.wingMass;
	}
	
	public float getTailMass() {
		return this.tailMass;
	}
	
	public float getMaxThrust() {
		return this.maxThrust;
	}
	
	public float getMaxAOA() {
		return this.maxAOA;
	}
	
	public float getWingLiftSlope() {
		return this.wingLiftSlope;
	}
	
	public float getHorStabLiftSlope() {
		return this.horStabLiftSlope;
	}
	
	public float getVerStabLiftSlope() {
		return this.verStabLiftSlope;
	}
	
	public float getHorizontalAngleOfView() {
		return this.horizontalAngleOfView;
	}
	
	public float getVerticalAngleOfView() {
		return this.verticalAngleOfView;
	}
	
	public int getNbColumns() {
		return this.nbColumns;				
	}
	
	public int getNbRows() {
		return this.nbRows;
	}
	
	public Vector3f getEnginePosition() {
		return this.enginePos;
	}
	
	/*
	 * Setter for the position of the engine in drone coordinates
	 */
	public void setEnginePosition() {
		this.enginePos.setX(0);
		this.enginePos.setY(0);
		this.enginePos.setZ((-this.getTailMass() * this.getTailSize()) / this.getEngineMass());
	}
	
	/*
	 * Return the gravitational force for the given mass
	 */
	public float getGravitationalForce(float mass) {
		return (float) (mass * GRAVITY_CONSTANT);
	}
	
	/*
	 * Calculate the lift force
	 */
	public Vector3f getLiftForce(Vector3f normal, float AOA, float liftslope, float projectedVelocityScalar) {
		float scalar = (float) (AOA * liftslope * Math.pow(projectedVelocityScalar,2));
		
		Vector3f liftForce = new Vector3f(normal.getX() * scalar, normal.getY() * scalar, normal.getZ() * scalar);
		
		return liftForce;
	}
	
	/*
	 * Calculate the Projected Velocity vector of the drone
	 * @param 	droneVelocityVector
	 * 			The velocity vector of the drone
	 * 
	 * @param	windVelocityVector
	 * 			The Velocity vector of the wind
	 * 
	 * @Return	Returns the accumulated velocity of the drone and the wind, projected onto the correct axis.
	 */
	public Vector3f getProjectedVelocityVector(Vector3f droneVelocityVector, Vector3f windVelocityVector) {
		Vector3f accumulatedVelocity = new Vector3f(droneVelocityVector.getX() - windVelocityVector.getX(),
													droneVelocityVector.getY() - windVelocityVector.getY(),
													droneVelocityVector.getZ() - windVelocityVector.getZ());
		//TODO project correctly
		return accumulatedVelocity;
	}
	
	public float getProjectedVelocityScalar(Vector3f projectedVelocityVector) {
		return projectedVelocityVector.length();
	}
	
	
	/*
	 * Attack vectors of the Wings and stabilizers
	 */
	public Vector3f getAttackVectorLeftWing() {
		Vector3f attackVectorLW = new Vector3f(0, (float) Math.sin(getLeftWingInclination()), (float) -Math.cos(getLeftWingInclination()));
		return attackVectorLW;
	}

	public Vector3f getAttackVectorRightWing() {
		Vector3f attackVectorRW = new Vector3f(0, (float) Math.sin(getRightWingInclination()), (float) -Math.cos(getRightWingInclination()));
		return attackVectorRW;
	}
	
	public Vector3f getAttackVectorHorizontalStab() {
		Vector3f attackVectorHS = new Vector3f(0, (float) Math.sin(getHorStabInclination()), (float) -Math.cos(getHorStabInclination()));
		return attackVectorHS;
	}
	
	public Vector3f getAttackVectorVerticalStab() {
		Vector3f attackVectorVS = new Vector3f((float) -Math.sin(getVerStabInclination()), 0, (float) -Math.cos(getVerStabInclination()));
		return attackVectorVS;
	}
	
	
	/*
	 * Inclinations of the Wings and Stabilizers
	 */
	public float getLeftWingInclination() {
		return //TODO
	}
	
	public float getRightWingInclination() {
		return //TODO
	}
	
	public float getHorStabInclination() {
		return //TODO
	}
	
	public float getVerStabInclination() {
		return //TODO
	}
	
	
	/*
	 * Normals of the Wings and Stabilizers
	 */
	public Vector3f getNormalLeftWing() {
		return (this.getAxisvectorLeftWing()).crossProduct(this.getAttackVectorLeftWing());
	}
	
	public Vector3f getNormaRightWing() {
		return this.getAxisVectorRightWing().crossProduct(this.getAttackVectorRightWing());
	}
	
	public Vector3f getNoralHorizontalStab() {
		return this.getAxisVectorHorizontalStab().crossProduct(this.getAttackVectorHorizontalStab());
	}
	
	public Vector3f getNormalVerticalStab() {
		return this.getAxisVectorVerticalStab().crossProduct(this.getAttackVectorVerticalStab());
	}

	
	/*
	 * Axis vectors of the Wings and Stabilizers
	 */
	public Vector3f getAxisvectorLeftWing() {
		Vector3f axisVectorLW = new Vector3f(1,0,0);
		return axisVectorLW;
	}
	
	public Vector3f getAxisVectorRightWing() {
		Vector3f axisVectorRW = new Vector3f(1,0,0);
		return axisVectorRW;
	}
	
	public Vector3f getAxisVectorHorizontalStab() {
		Vector3f axisVectorHS = new Vector3f(1,0,0);
		return axisVectorHS;
	}
	
	public Vector3f getAxisVectorVerticalStab() {
		Vector3f axisVectorVS = new Vector3f(0,1,0);
		return axisVectorVS;
	}
	

//	/*
//	 * Calculate the Cross Product of 2 given vectors
//	 */
//	public float[] crossProduct(float[] vector1, float[] vector2) {
//		if (vector1.length != vector2.length) {
//			throw new IllegalArgumentException();
//		}
//		
//		float[] crossprod = new float[3];
//		crossprod[0] = vector1[1]*vector2[2] - vector2[1]*vector1[2];
//		crossprod[1] = vector1[2]*vector2[0] - vector2[2]*vector1[0];
//		crossprod[2] = vector1[0]*vector2[1] - vector2[0]*vector1[1];
//		return crossprod;
//	}
//	
//	
//	/*
//	 * calculate the Dot Product of 2 given vectors
//	 */
//	public float dotProduct(float[] vector1, float[] vector2) {
//		if (vector1.length != vector2.length) {
//			throw new IllegalArgumentException();
//		}
//		
//		float dotprod = 0;
//		for (int i=0; i < NB_AXES; i++) {
//			dotprod += vector1[i] * vector2[i];
//		}
//		return dotprod;
//	}

	/*
	 * Calculate the Angle Of Attack
	 */
	public float getAngleOfAttack(Vector3f normal, Vector3f projSpeedVector, Vector3f attackVector) {
		float aoa = (float) -Math.atan2(normal.dot(projSpeedVector), attackVector.dot(projSpeedVector));
		if (aoa > this.maxAOA)
			throw new IndexOutOfBoundsException();
		return aoa;
	}
}
