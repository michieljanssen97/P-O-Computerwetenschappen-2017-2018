package be.kuleuven.cs.robijn.common.math;

import org.apache.commons.math3.linear.*;

public class VectorMath {
	
	private VectorMath() {
	}
	
	public static RealVector crossProduct(RealVector realVector1, RealVector realVector2)
			throws IllegalArgumentException {
		if ((realVector1.getDimension() != 3) || (realVector2.getDimension() != 3))
			throw new IllegalArgumentException();
		double vecX = realVector1.getEntry(1)*realVector2.getEntry(2) - realVector1.getEntry(2)*realVector2.getEntry(1);
		double vecY = realVector1.getEntry(2)*realVector2.getEntry(0) - realVector1.getEntry(0)*realVector2.getEntry(2);
		double vecZ = realVector1.getEntry(0)*realVector2.getEntry(1) - realVector1.getEntry(1)*realVector2.getEntry(0); 	
    	return new ArrayRealVector(new double[] {vecX, vecY, vecZ}, false);
	}
}
