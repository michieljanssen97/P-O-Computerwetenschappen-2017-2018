package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.FrameBuffer;
import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.common.airports.Runway;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.concurrent.Semaphore;

/**
 * Render a ground texture
 */
public class GroundBaker {
    private final OpenGLRenderer renderer;

    public GroundBaker(OpenGLRenderer renderer){
        this.renderer = renderer;
    }

    public Texture bake(int groundWidth, int groundHeight, int texWidth, int texHeight, WorldObject world, Semaphore worldStateLock){
        Texture targetTexture = Texture.createEmpty(new int[]{texWidth, texHeight}, 3);
        OpenGLFrameBuffer frameBuffer = OpenGLFrameBuffer.create(targetTexture, true);
        OpenGLOrthographicCamera camera = new OpenGLOrthographicCamera();
        camera.setWidth(groundWidth);
        camera.setHeight(groundHeight);
        camera.setRelativePosition(new ArrayRealVector(new double[]{0, 100, 0}, false));
        Rotation rot = new Rotation(new Vector3D(0, 0, -1), new Vector3D(0, -1, 0));
        camera.setRelativeRotation(rot);
        camera.setFarPlane(10000);
        camera.addVisibilityFilter(obj -> obj.getName().equals("GROUND") || obj instanceof Airport || obj instanceof Gate || obj instanceof Runway);
        world.addChild(camera);
        renderer.startRender(world, frameBuffer, camera, worldStateLock).waitUntilFinished();
        return targetTexture;
    }
}
