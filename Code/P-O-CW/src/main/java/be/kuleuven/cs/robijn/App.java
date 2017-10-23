package be.kuleuven.cs.robijn;

import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.common.math.Vector3f;
import be.kuleuven.cs.robijn.testbed.renderer.OpenGLRenderer;
import org.apache.commons.math3.linear.ArrayRealVector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try {
            testRenderer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testRenderer() throws Exception {
        System.out.println( "P&0 CW" );
        Renderer renderer = OpenGLRenderer.create();

        int width = 4000;
        int height = 4000;
        FrameBuffer buffer = renderer.createFrameBuffer(width, height);

        Camera camera = renderer.createCamera();
        WorldObject world = new WorldObject();
        Box box = new Box();
        box.setRotation(new ArrayRealVector(new double[]{0, 0, (float)Math.PI/2f}, false));
        box.setRelativePosition(new ArrayRealVector(new double[]{0, 0, -6}, false));
        world.addChild(box);

        renderer.render(world, buffer, camera);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] imgBackingByteArray = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();

        buffer.readPixels(imgBackingByteArray);

        try {
            ImageIO.write(img, "png", new File("test.png")); //Writes test.png to folder where java is running
        } catch (IOException e) {
            e.printStackTrace();
        }

        buffer.close();
        renderer.close();
    }
}
