package be.kuleuven.cs.robijn.common.math;

public class Vector3f {

	private float x, y, z;

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
	
	public boolean isValidVector(float x, float y, float z){
		return !(Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z) || Double.isInfinite(x)
				||Double.isInfinite(y) || Double.isInfinite(z));
	}

	public float getX() {
		return x;
	}

	public Vector3f setX(float x) {
		return new Vector3f(x,this.y,this.z);	
	}

	public float getY() {
		return y;
	}

	public Vector3f setY(float y) {
		return new Vector3f(this.x,y,this.z);	
	}

	public float getZ() {
		return z;
	}

	public Vector3f setZ(float z) {
		return new Vector3f(this.x,this.y,z);	

	}
	
	public Vector3f subtract(Vector3f vector){
        return new Vector3f(this.x-vector.x,this.y-vector.y,this.z-vector.z);
    }
    
    public Vector3f sum(Vector3f vector){
        return new Vector3f(this.x+vector.x,this.y+vector.y,this.z+vector.z);
    }
        
    public float length(){
        return (float)Math.sqrt((Math.pow(this.x, 2)) + (Math.pow(this.y, 2))+(Math.pow(this.z, 2)));
    }
    
    public Vector3f unit(){
        return new Vector3f(this.x/this.length(),this.y/this.length(),this.z/this.length());
    }
    
    public Vector3f scale(float factor){
        return new Vector3f(this.x*factor,this.y*factor,this.z*factor);
    }
    
    public float dot(Vector3f vector){
        return (this.x*vector.x) + (this.y*vector.y) + (this.z*vector.z);
    }
    
    public Vector3f translate(float x, float y,float z){
    	return new Vector3f(this.x+x,this.y+y,this.z+z);
    }

    public boolean fuzzyEquals(Vector3f b, float epsilon){ 
        if(b == null){
            return false;
        }
        
        return Math.abs(this.x-b.x)<epsilon && Math.abs(this.y-b.y)<epsilon && Math.abs(this.z-b.z)<epsilon;
    }
    
    public Vector3f crossProduct(Vector3f vector){
    	float vecx = this.y*vector.z - this.z*vector.y;
    	float vecy = this.z*vector.x - this.x*vector.z;
    	float vecz = this.x*vector.y - this.y*vector.x;
    	
    	return new Vector3f(vecx,vecy,vecz);
    }
    
    
}
