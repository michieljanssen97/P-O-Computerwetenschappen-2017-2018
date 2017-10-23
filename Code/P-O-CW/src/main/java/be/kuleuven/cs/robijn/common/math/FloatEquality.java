package be.kuleuven.cs.robijn.common.math;

public class FloatEquality {
    public static final double DEFAULT_EPSILON = 1E-6d;

    public static boolean equals(float f1, float f2, float epsilon){
        return Math.abs(f1 - f2) < epsilon;
    }

    public static boolean equals(float f1, float f2){
        return Math.abs(f1 - f2) < DEFAULT_EPSILON;
    }

    public static boolean equals(double d1, double d2, double epsilon){
        return Math.abs(d1 - d2) < epsilon;
    }

    public static boolean equals(double d1, double d2){
        return Math.abs(d1 - d2) < DEFAULT_EPSILON;
    }
}
