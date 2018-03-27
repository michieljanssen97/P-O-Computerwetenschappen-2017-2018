package be.kuleuven.cs.robijn.autopilot.image;

import java.util.ArrayList;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class CubePath {
	
	public CubePath(float[] x, float[] y, float[] z) {
		if (!isValidPath(x, y, z))
			throw new IllegalArgumentException("The given path is invalid.");
		ArrayList<RealVector> co = new ArrayList<RealVector>();
		int l = x.length;
		for (int i=0; i<l; i++) {
			ArrayRealVector a = new ArrayRealVector(new double[] {x[i], y[i], z[i]}, false);
			co.add(a);
		}
//		if (l != 0) {
//			ArrayRealVector a = new ArrayRealVector(new double[] {x[l-1]/4, 30, z[l-1]/2}, false);
//			co.add(a);
//		}
		this.coordinatesList = co;
	}
	
	/**
	 * List that stores the path coordinates of the cubes.
	 */
	private ArrayList<RealVector> coordinatesList;
	
	private boolean isValidPath(float[] x, float[] y, float[] z) {
		if ( (x.length != y.length) || (y.length != z.length) || (x.length != z.length) )
			return false;
		int l = x.length;
		for (int i=0; i<l; i++) {
			if ( (!isValidFloat(x[i])) || (!isValidFloat(y[i])) || (!isValidFloat(z[i])) )
				return false;
		}
		return true;
	}
	
	private boolean isValidFloat(float f) {
		return (Float.isFinite(f) && !Float.isNaN(f));
	}
	
	public int getPathSize() {
		return this.coordinatesList.size();
	}
	
	/**
	 * Gives the path coordinates of the cube closest to the given 
	 * coordinates (usually the position of the drone).
	 * @param dronePos
	 * 		The given coordinates.
	 * @return
	 * 		The path coordinates of the closest cube.
	 */
	public RealVector getNextPathCoordinate() {
		RealVector result = null;
		if (getPathSize() != 0)
			result = coordinatesList.get(0);
		return result;
	}
	
	public void removeCoordinate(RealVector path) {
		coordinatesList.remove(path);
	}
	
	public void addCoordinate(RealVector path) {
		coordinatesList.add(path);
	}
	
}
