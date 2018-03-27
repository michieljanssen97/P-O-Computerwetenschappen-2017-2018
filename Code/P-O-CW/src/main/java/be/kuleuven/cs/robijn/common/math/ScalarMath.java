package be.kuleuven.cs.robijn.common.math;

public class ScalarMath {
	
	public static float betweenBoundaries(float value, float maxValue) {
		if (value > maxValue)
			return maxValue;
		if (value < -maxValue)
			return -maxValue;
		return value;
	}
	
	public static float betweenBoundaries(float value, float maxValue, float minValue) {
		if (value > maxValue)
			return maxValue;
		if (value < minValue)
			return minValue;
		return value;
	}
	
	public static float upperBoundary(float value, float maxValue, boolean NaN) {
		if ((value > maxValue) || ((NaN) && (Float.isNaN(value))))
			return maxValue;
		return value;
	}
	
	public static float lowerBoundary(float value, float minValue, boolean NaN) {
		if ((value < minValue) || ((NaN) && (Float.isNaN(value))))
			return minValue;
		return value;
	}
	
	public static Angle upperBoundary(Angle value, float maxValue, boolean NaN) {
		float angle = value.getOrientation();
		if ((angle > maxValue) || ((NaN) && (Float.isNaN(angle))))
			return new Angle(maxValue);
		return new Angle(angle);
	}
	
	public static Angle lowerBoundary(Angle value, float minValue, boolean NaN) {
		float angle = value.getOrientation();
		if ((angle < minValue) || ((NaN) && (Float.isNaN(angle))))
			return new Angle(minValue);
		return new Angle(angle);
	}
}
