package be.kuleuven.cs.robijn.testbed.renderer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.awt.*;

public class RenderDebug {
    private static OpenGLRenderer renderer;
    public static void setRenderer(OpenGLRenderer renderer){
        RenderDebug.renderer = renderer;
    }

    public static void drawLine(Vector3D pointA, Vector3D pointB, Color color){
        renderer.addLineToDraw(new Line(pointA, pointB, color));
    }
}
