package be.kuleuven.cs.robijn.common.math;

public class Vector3f {

	private float x, y, z;
	
	/**
	 * Initializes this new vector with a it's given components.
	 * 
	 * @param x
	 *        |The x-component for this new vector.
	 * @param y
	 * 		  |The y-component for this new vector.
	 * @param z
	 *        |The z-component for this new vector.
	 * @post  The components of this new vector are equal to the given x-,y- and z-component.
	 * 		  | new.getX() == x
	 * 	      | new.getY() == y
	 * 		  | new.getZ() == z
	 @throws  IllegalArgumentException
	 * 		  The given component values are not valid
	 *        | ! isValidVector(x,y,z)
	 */
	public Vector3f(float x, float y, float z) throws IllegalArgumentException {
		if (isValidVector(x, y, z)) {
			this.x = x;
			this.y = y;
			this.z = z;
		} else {
			throw new IllegalArgumentException("Position must be valid");
		}

	}
	
	public Vector3f() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	/**
	 * Checks whether all given vector components are valid by returning a boolean indicating validness.
	 * 
	 * @param x
	 *        |The x-component for this vector.
	 * @param y
	 *        |The y-component for this vector.
	 * @param z
	 * 		  |The z-component for this vector.
	 * @invar Both x,y,z parameters must be a valid number
	 * 		  | Float.isNaN(x) != true
	 * 		  | Float.isNaN(y) != true
	 * 		  | Float.isNaN(z) != true
	 * 		  | Float.isInfinite(x) != true
	 * 		  | Float.isInfinite(y) != true
	 *        | Float.isInfinite(z) != true
	 * @return result == !(Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z) || Float.isInfinite(x) || Float.isInfinite(y)
				|| Float.isInfinite(z));
	 */
	public boolean isValidVector(float x, float y, float z) {
		return !(Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z) || Float.isInfinite(x) || Float.isInfinite(y)
				|| Float.isInfinite(z));
	}

	public float getX() {
		return x;
	}

	public Vector3f setX(float x) {
		return new Vector3f(x, this.y, this.z);
	}

	public float getY() {
		return y;
	}

	public Vector3f setY(float y) {
		return new Vector3f(this.x, y, this.z);
	}

	public float getZ() {
		return z;
	}

	public Vector3f setZ(float z) {
		return new Vector3f(this.x, this.y, z);
	}
	
	/**
	 * Subtracts the parameter vector from this vector.
	 * @param vector
	 * 		  |The parameter vector
	 * @see implementation 
	 */
	public Vector3f subtract(Vector3f vector) {
		return new Vector3f(this.x - vector.x, this.y - vector.y, this.z - vector.z);
	}
	
	/**
	 * Takes the sum of this vector and the parameter vector.
	 * @param vector
	 * 		  |The parameter vector
	 * @see implementation 
	 */
	public Vector3f sum(Vector3f vector) {
		return new Vector3f(this.x + vector.x, this.y + vector.y, this.z + vector.z);
	}
	
	/**
	 * Calculates the 2-norm of this vector.
	 * @see implementation 
	 */
	public float length() {
		return (float) Math.sqrt((Math.pow(this.x, 2)) + (Math.pow(this.y, 2)) + (Math.pow(this.z, 2)));
	}
	
	/**
	 * Calculates the unit vector of this vector.
	 * @see implementation 
	 */
	public Vector3f unit() {
		return new Vector3f(this.x / this.length(), this.y / this.length(), this.z / this.length());
	}
	
	/**
	 * Scales this vector by a given factor.
	 * @param factor
	 * 		  |A floating point number.
	 * @see implementation 
	 */
	public Vector3f scale(float factor) {
		return new Vector3f(this.x * factor, this.y * factor, this.z * factor);
	}
	
	/**
	 * Takes the dot product of this vector and the parameter vector.
	 * @param vector
	 * 		  |The parameter vector
	 * @see implementation 
	 */
	public float dot(Vector3f vector) {
		return this.x * vector.x + this.y * vector.y + this.z * vector.z;
	}
	
	/**
	 * Translates this vector with a given amount in the x,y,z direction.
	 * @param x
	 *        The amount of change for the x-component.
	 * @param y
	 * 		  |The amount of change for the y-component.
	 * @param z
	 *        |The amount of change for the z-component.
	 * @post  The components of this new vector are equal to the sum of the old x-,y- and z-components and the corresponding parameter components.
	 * 		  | new.getX() == this.getX()+x
	 * 	      | new.getY() == this.getY()+y
	 * 		  | new.getZ() == this.getZ()+z
	 */
	public Vector3f translate(float x, float y, float z) {
		return new Vector3f(this.x + x, this.y + y, this.z + z);
	}
	
	/**
	 * Checks whether two vectors are equal to each other by defining an epsilon value to set a margin error.
	 * @param b
	 * 		  | The other vector.
	 * @param epsilon
	 * 		  | The value for the margin error. 
	 * @return| if (b = null) return false
	 * 		  | else return Math.abs(this.x - b.x) < epsilon && Math.abs(this.y - b.y) < epsilon && Math.abs(this.z - b.z) < epsilon
	 */
	public boolean fuzzyEquals(Vector3f b, float epsilon) {
		if (b == null) {
			return false;
		}

		return Math.abs(this.x - b.x) < epsilon && Math.abs(this.y - b.y) < epsilon && Math.abs(this.z - b.z) < epsilon;
	}

	/**	
	 * Takes the cross product of this vector and the parameter vector.
	 * @param vector
	 * 		  |The parameter vector
	 * @return A x B = (Ay*Bz - Az*By , Az*Bx - Ax*Bz , Ax*By - Ay*Bx) 
	 */
	public Vector3f crossProduct(Vector3f vector) {
		float vecx = this.y * vector.z - this.z * vector.y;
		float vecy = this.z * vector.x - this.x * vector.z;
		float vecz = this.x * vector.y - this.y * vector.x;

		return new Vector3f(vecx, vecy, vecz);
	}

}
