package be.kuleuven.cs.robijn.testbed.renderer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.awt.*;

public class Line {
    private Vector3D pointA;
    private Vector3D pointB;
    private Color color;

    public Line(Vector3D pointA, Vector3D pointB, Color color) {
        if(pointA == null || pointB == null || color == null){
            throw new IllegalArgumentException();
        }

        this.pointA = pointA;
        this.pointB = pointB;
        this.color = color;
    }

    public Vector3D getPointA() {
        return pointA;
    }

    public Vector3D getPointB() {
        return pointB;
    }

    public Color getColor() {
        return color;
    }
}
