package be.kuleuven.cs.robijn.common.math;

import org.apache.commons.math3.linear.*;

/**
 * A helper class with supplementary calculations on RealVectors.
 */
public class VectorMath {
	public static final double EPSILON = 1E-6d;
	
	private VectorMath() { }
	
	/**
	 * Method to calculate the cross product between two 3-dimensional vectors.
	 * @param  v1
	 *         The first vector to calculate the cross product with.
	 * @param  v2
	 *         The second vector to calculate the cross product with.
	 * @return The cross product of realVector1 and realVector2.
	 * @throws IllegalArgumentException
	 *         At least one of the given vectors isn't 3-dimensional.
	 *       | ((realVector1.getDimension() != 3) || (realVector2.getDimension() != 3))
	 */
	public static RealVector crossProduct(RealVector v1, RealVector v2) throws IllegalArgumentException {
		if ((v1.getDimension() != 3) || (v2.getDimension() != 3)) {
			throw new IllegalArgumentException("v1 and v2 must be 3-dimensional");
		}
		double vecX = v1.getEntry(1)*v2.getEntry(2) - v1.getEntry(2)*v2.getEntry(1);
		double vecY = v1.getEntry(2)*v2.getEntry(0) - v1.getEntry(0)*v2.getEntry(2);
		double vecZ = v1.getEntry(0)*v2.getEntry(1) - v1.getEntry(1)*v2.getEntry(0);
    	return new ArrayRealVector(new double[] {vecX, vecY, vecZ}, false);
	}

	/**
	 * Returns true if v1 is rougly equal to v2.
	 * Being roughly equal is defines as having a distance smaller than VectorMath.EPSILON
	 * @param v1 the first vector
	 * @param v2 the second vector
	 * @return true if they the vectors are roughly equal.
	 * @throws IllegalArgumentException thrown when either v1 or v2 is null or the dimension of the vectors does not match.
	 */
	public static boolean fuzzyEquals(RealVector v1, RealVector v2) throws IllegalArgumentException {
		if (v1 == null || v2 == null){
			throw new IllegalArgumentException("v1 and v2 must not be null");
		} else if(v1.getDimension() != v2.getDimension()){
			throw new IllegalArgumentException("v1 and v2 must have the same dimension");
		}

        return v1.getDistance(v2) < EPSILON;
    }

	/**
	 * Converts a 3D cartesian coordinate vector to a 4D homogeneous coordinate vector.
	 * @param input a non-null 3D cartesian vector
	 * @return a 4D homogenous coordinate vector
	 * @throws IllegalArgumentException thrown when input is null or the input vector is not 3-dimensional
	 */
	public static RealVector cartesianToHomogeneous(RealVector input) throws IllegalArgumentException {
		if(input == null){
			throw new IllegalArgumentException("input must not be null");
		}
		if(input.getDimension() != 3){
			throw new IllegalArgumentException("input must be 3-dimensiona");
		}

		return new ArrayRealVector(new double[]{input.getEntry(0), input.getEntry(1), input.getEntry(2), 1}, false);
	}

	/**
	 * Converts a 4D homogeneous coordinate vector to a 3D cartesian coordinate vector
	 * @param input a non-null 4D homogeneous coordinate vector
	 * @return a 3D cartesian coordinate vector
	 * @throws IllegalArgumentException thrown when input is null or the input vector is not 4-dimensional
	 */
	public static RealVector homogeneousToCartesian(RealVector input){
		if(input == null){
			throw new IllegalArgumentException("input must not be null");
		}
		if(input.getDimension() != 4){
			throw new IllegalArgumentException("input must be 4-dimensional");
		}

		double scale = input.getEntry(3);
		return new ArrayRealVector(new double[]{
				input.getEntry(0) / scale,
				input.getEntry(1) / scale,
				input.getEntry(2) / scale
		}, false);
	}
}
