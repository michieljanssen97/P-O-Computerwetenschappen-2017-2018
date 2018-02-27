package be.kuleuven.cs.robijn.autopilot.image;

import java.util.ArrayList;

public class CubePath {
	
	public CubePath(float[] x, float[] y, float[] z) {
		if (!isValidPath(x, y, z))
			throw new IllegalArgumentException("The given path is invalid.");
		ArrayList<float[]> co = new ArrayList<float[]>();
		int l = x.length;
		for (int i=0; i<l; i++) {
			float[] a = new float[] {x[i], y[i], z[i]};
			co.add(a);
		}
		this.coordinatesList = co;
	}
	
	/**
	 * List that stores the path coordinates of the cubes.
	 */
	private ArrayList<float[]> coordinatesList;
	
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
	public float[] getClosestXYZTo(double[] dronePos) {
		float minimum = Float.POSITIVE_INFINITY;
		float[] result = new float[3];
		for (int i=0; i<getPathSize(); i++) {
			float[] curCo = coordinatesList.get(i);
			float distance = (float) Math.sqrt(Math.pow(curCo[0] - dronePos[0], 2) + Math.pow(curCo[1] - dronePos[1], 2) + Math.pow(curCo[2] - dronePos[2], 2));
			if (distance < minimum) {
				minimum = distance;
				result = curCo;
			}
		}
		return result;
	}
	
	public void removeCoordinate(float[] path) {
		coordinatesList.remove(path);
	}
	
}
