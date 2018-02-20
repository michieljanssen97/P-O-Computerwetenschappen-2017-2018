package be.kuleuven.cs.robijn.autopilot.image;

import java.util.ArrayList;

public class CubePath implements interfaces.Path {
	
	public CubePath(float[] x, float[] y, float[] z) {
		if (!isValidPath(x, y, z))
			throw new IllegalArgumentException("The given path is invalid.");
		this.x = x;
		this.y = y;
		this.z = z;
		this.coordinatesList = getAllCubesXYZ();
	}
	
	private float[] x;
	
	private float[] y;
	
	private float[] z;
	
	private ArrayList<float[]> coordinatesList;
	
	public float[] getX() {
		return this.x;
	}
	
	public float[] getY() {
		return this.y;
	}
	
	public float[] getZ() {
		return this.z;
	}
	
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
	
	public ArrayList<float[]> getAllCubesXYZ() {
		ArrayList<float[]> co = new ArrayList<float[]>();
		for (int i=0; i<getPathSize(); i++) {
			float[] a = new float[] {getX()[i], getY()[i], getZ()[i]};
			co.add(a);
		}
		return co;
	}
	
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
	
	public void removeCoordinate(int index) {
		coordinatesList.remove(index);
	}
	
}
