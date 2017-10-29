package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.*;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * GUI component that displays a 3D render of the world, along with several buttons for changing the view
 */
public class CameraViewControl extends AnchorPane {
    //The object names for the cameras that provide the different perspectives.
    public static final String DRONE_CAMERA_ID = "gui_drone_camera";
    public static final String THIRDPERSON_CAMERA_ID = "gui_thirdperson_camera";
    public static final String BOX_CAMERA_ID = "gui_box_camera";

    @FXML
    private ScrollPane imageViewHost;

    @FXML
    private ImageView imageView;

    @FXML
    private ToggleGroup perspectiveToggleGroup;

    private ObjectProperty<SimulationDriver> simulationProperty = new SimpleObjectProperty<>(this, "simulation");

    private FrameBuffer frameBuffer;
    private BufferedImage awtImage;
    private byte[] imageBackingBuffer;
    private WritableImage image;

    public CameraViewControl(){
        //Load the layout associated with this GUI control
        FXMLLoader fxmlLoader = new FXMLLoader(Resources.getResourceURL("/layouts/camera_view.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @FXML
    private void initialize() {
        //Stretch imageView to fill scrollview, which has its size controlled by the SplittablePane
        imageViewHost.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        imageViewHost.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        imageView.fitWidthProperty().bind(this.widthProperty());
        imageView.fitHeightProperty().bind(this.heightProperty());

        //When the simulation starts, setup the rendering
        simulationProperty.addListener((observable, oldValue, newValue) -> {
            //Setup framebuffers and image buffers
            setupImages();

            //When the simulation is updated, update the displayed image
            newValue.addOnUpdateEventHandler((inputs, outputs)->{
                update();
            });
        });

        //When the image viewport changes, resize the framebuffer and image buffers
        imageView.fitWidthProperty().addListener(e -> onSizeChanged());
        imageView.fitHeightProperty().addListener(e -> onSizeChanged());

        //There must always be an active perspective, so if no perspective is selected, select the first one
        perspectiveToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null){
                perspectiveToggleGroup.selectToggle(perspectiveToggleGroup.getToggles().get(0));
            }
        });
    }

    private void onSizeChanged(){
        //Discard old framebuffer
        if(frameBuffer != null){
            try {
                frameBuffer.close();
            } catch (Exception e) {}
        }
        image = null;

        //If a simulation is set, setup the framebuffer/image buffers
        if(getSimulation() != null){
            setupImages();
        }
    }

    private void setupImages(){
        //Dont create framebuffers of size 0
        int width = (int)imageView.getFitWidth();
        int height = (int)imageView.getFitHeight();
        if(width == 0 || height == 0){
            return;
        }

        //Create framebuffer
        frameBuffer = getSimulation().getTestBed().getRenderer().createFrameBuffer(width, height);
        //Create an AWT image that can be converted to a javafx image
        awtImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        //Retrieve the array which stores the AWT image data. We copy data from the framebuffer to this image buffer.
        imageBackingBuffer = ((DataBufferByte) awtImage.getRaster().getDataBuffer()).getData();
    }

    private void update(){
        //If framebuffer is not setup, try doing so. If that fails, skip this update
        if(frameBuffer == null){
            setupImages();
            if(frameBuffer == null){
                return;
            }
        }

        TestBed testBed = simulationProperty.get().getTestBed();
        Renderer renderer = testBed.getRenderer();
        WorldObject world = testBed.getWorldRepresentation();

        //Get the active camera and set its camera FOV to match the image width to height ratio so the image isnt warped/stretched.
        String selectedCameraId = (String)perspectiveToggleGroup.getSelectedToggle().getUserData();
        Camera camera = world.getDescendantsStream().filter(o -> Objects.equals(o.getName(), selectedCameraId)).map(o -> (Camera)o).findFirst().get();
        //TODO: fix viewport FOV compensation
        //double ratio = imageView.getFitWidth()/imageView.getFitHeight();
        //float targetFOV = camera.getVerticalFOV();
        //camera.setHorizontalFOV(targetFOV*(float)ratio);

        //Render to framebuffer, copy from framebuffer to image, convert image to javafx image, display javafx image
        renderer.render(world, frameBuffer, camera);
        frameBuffer.readPixels(imageBackingBuffer);
        image = SwingFXUtils.toFXImage(awtImage, image);
        imageView.setImage(image);
    }

    public void setSimulation(SimulationDriver simulationProperty) {
        this.simulationProperty.set(simulationProperty);
    }

    public SimulationDriver getSimulation() {
        return simulationProperty.get();
    }

    public ObjectProperty<SimulationDriver> getSimulationProperty() {
        return simulationProperty;
    }
}
