package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Camera;
import be.kuleuven.cs.robijn.common.FrameBuffer;
import be.kuleuven.cs.robijn.common.Renderer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class MainController {
    @FXML
    private ImageView imageView;

    private WritableImage renderedImage;
    private Renderer renderer;

    @FXML
    private void initialize(){
        //renderer = new OpenGLRenderer();
        int width = 700;
        int height = 700;
        FrameBuffer frameBuffer = renderer.createFrameBuffer(width, height);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] imgBackingByteArray = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();

        Camera camera = renderer.createCamera();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000/60), e -> {
            renderer.render(frameBuffer, camera);
            frameBuffer.readPixels(imgBackingByteArray);
            renderedImage = SwingFXUtils.toFXImage(img, renderedImage);

            imageView.setImage(renderedImage);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}
