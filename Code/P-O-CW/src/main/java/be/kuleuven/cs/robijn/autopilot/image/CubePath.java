package be.kuleuven.cs.robijn.autopilot.image;

public class CubePath implements interfaces.Path {
	
	public CubePath(float[] x, float[] y, float[] z) {
		if (!isValidPath(x, y, z))
			throw new IllegalArgumentException("The given path is invalid.");
		this.x = x;
		this.y = y;
		this.z = z;		
	}
	
	private float[] x;
	
	private float[] y;
	
	private float[] z;
	
	public float[] getX() {
		return this.x;
	}
	
	public float[] getY() {
		return this.y;
	}
	
	public float[] getZ() {
		return this.z;
	}
	
	public boolean isValidPath(float[] x, float[] y, float[] z) {
		if ( (x.length != y.length) || (y.length != z.length) || (x.length != z.length) )
			return false;
		int l = x.length;
		for (int i=0; i<l; i++) {
			if ( (!isValidFloat(x[i])) || (!isValidFloat(y[i])) || (!isValidFloat(z[i])) )
				return false;
		}
		return true;
	}
	
	public boolean isValidFloat(float f) {
		return (Float.isFinite(f) && !Float.isNaN(f));
	}
	
	public float[] getCubeIndexXYZ(int i) {
		if (i<0 || i>=this.x.length)
			throw new IllegalArgumentException();
		return new float[] {this.x[i], this.y[i], this.z[i]};
	}
	
	public int getPathSize() {
		return this.x.length;
	}
	
}
