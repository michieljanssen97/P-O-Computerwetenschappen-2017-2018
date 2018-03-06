package be.kuleuven.cs.robijn.testbed.renderer;

import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.awt.*;

public class RenderDebug {
    private static OpenGLRenderer renderer;
    public static void setRenderer(OpenGLRenderer renderer){
        RenderDebug.renderer = renderer;
    }

    public static void drawLine(Vector3D pointA, Vector3D pointB, Color color){
        renderer.addLineToDraw(new Line(pointA, pointB, color));
    }

    public static void drawFloorGrid(Vector2D gridStart, Vector2D gridEnd, float gridScale, Color color){
        for(double z = gridStart.getY(); z <= gridEnd.getY(); z += gridScale){
            Vector3D pointA = new Vector3D(gridStart.getX(), 0, z);
            Vector3D pointB = new Vector3D(gridEnd.getX(), 0, z);
            renderer.addLineToDraw(new Line(pointA, pointB, color));
        }

        for(double x = gridStart.getX(); x <= gridEnd.getX(); x += gridScale){
            Vector3D pointA = new Vector3D(x, 0, gridStart.getY());
            Vector3D pointB = new Vector3D(x, 0, gridEnd.getY());
            renderer.addLineToDraw(new Line(pointA, pointB, color));
        }
    }
}
