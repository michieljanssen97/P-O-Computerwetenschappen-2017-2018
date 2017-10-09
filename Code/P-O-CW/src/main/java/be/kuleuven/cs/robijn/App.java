package be.kuleuven.cs.robijn;

import be.kuleuven.cs.robijn.testbed.renderer.FrameBuffer;
import be.kuleuven.cs.robijn.testbed.renderer.Renderer;

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

    }

    private static void testRenderer(){
        System.out.println( "P&0 CW" );
        Renderer renderer = Renderer.create();
        int width = 400;
        int height = 400;
        FrameBuffer buffer = FrameBuffer.create(width, height);
        renderer.renderWorld(null, null, buffer);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] imgBackingByteArray = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();

        buffer.read(imgBackingByteArray);

        try {
            ImageIO.write(img, "png", new File("test.png")); //Writes test.png to folder where java is running
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
