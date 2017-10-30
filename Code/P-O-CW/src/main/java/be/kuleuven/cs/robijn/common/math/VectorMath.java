package be.kuleuven.cs.robijn.common.math;

import org.apache.commons.math3.linear.*;

/**
 * A class with methods (that are not in the class RealVector)
 * to do calculations with 3-dimensional RealVectors.
 * 
 * @author Pieter Vandensande
 */
public class VectorMath {
	
	private VectorMath() {
	}
	
	/**
	 * Method to calculate the cross product between two 3-dimensional vectors.
	 * @param  realVector1
	 *         The first vector to calculate the cross product with.
	 * @param  realVector2
	 *         The second vector to calculate the cross product with.
	 * @return The cross product of realVector1 and realVector2.
	 * @throws IllegalArgumentException
	 *         At least one of the given vectors isn't 3-dimensional.
	 *       | ((realVector1.getDimension() != 3) || (realVector2.getDimension() != 3))
	 */
	public static RealVector crossProduct(RealVector realVector1, RealVector realVector2)
			throws IllegalArgumentException {
		if ((realVector1.getDimension() != 3) || (realVector2.getDimension() != 3))
			throw new IllegalArgumentException();
		double vecX = realVector1.getEntry(1)*realVector2.getEntry(2) - realVector1.getEntry(2)*realVector2.getEntry(1);
		double vecY = realVector1.getEntry(2)*realVector2.getEntry(0) - realVector1.getEntry(0)*realVector2.getEntry(2);
		double vecZ = realVector1.getEntry(0)*realVector2.getEntry(1) - realVector1.getEntry(1)*realVector2.getEntry(0); 	
    	return new ArrayRealVector(new double[] {vecX, vecY, vecZ}, false);
	}
	
	public static boolean fuzzyEquals(RealVector v1, RealVector v2) throws IllegalArgumentException {
		if ((v1.getDimension() != 3) || (v2.getDimension() != 3))
			throw new IllegalArgumentException();
        double epsilon = 0.0001;
        return Math.abs(v1.getEntry(0)-v2.getEntry(0))<epsilon && Math.abs(v1.getEntry(1)-v2.getEntry(1))<epsilon && Math.abs(v1.getEntry(2)-v2.getEntry(2))<epsilon;
    }
}
