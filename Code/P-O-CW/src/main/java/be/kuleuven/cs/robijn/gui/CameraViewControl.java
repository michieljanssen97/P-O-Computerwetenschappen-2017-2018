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
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

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
    public static final String SIDE_CAMERA_ID = "gui_side_camera";
    public static final String TOPDOWN_CAMERA_ID = "gui_topdown_camera";
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
    private Camera activeCamera;

    private DragHelper dragHelper;
    private double dragSensitivity = 0.1;

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

        dragHelper = new DragHelper(imageView);
        dragHelper.addOnDragEventHandler(e -> {
            if(activeCamera instanceof OrthographicCamera){
                OrthographicCamera ortho = (OrthographicCamera)activeCamera;
                Rotation camRot = ortho.getRelativeRotation();
                Vector3D right = camRot.applyTo(new Vector3D(1, 0, 0));
                Vector3D up = camRot.applyTo(new Vector3D(0, 1, 0));
                Vector3D delta = right.scalarMultiply(-e.getDeltaX()*dragSensitivity).add(up.scalarMultiply(e.getDeltaY()*dragSensitivity));
                ortho.setRelativePosition(ortho.getRelativePosition().add(new ArrayRealVector(new double[]{delta.getX(), delta.getY(), delta.getZ()}, false)));
            }
        });

        //When the simulation starts, setup the rendering
        simulationProperty.addListener((observable, oldValue, newValue) -> {
            //Setup framebuffers and image buffers
            setupImages();

            //When the simulation is updated, update the displayed image
            newValue.addOnUpdateEventHandler(new UpdateEventHandler((inputs, outputs) -> {
               update();
            },UpdateEventHandler.LOW_PRIORITY));
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
        activeCamera = world.getDescendantsStream()
                .filter(o -> Objects.equals(o.getName(), selectedCameraId))
                .map(o -> (Camera)o)
                .findFirst().get();

        double aspect = ((double)frameBuffer.getWidth())/((double)frameBuffer.getHeight());
        setCameraAspectRatio(activeCamera, aspect);

        //If the camera is orthographic, update the icon rendering properties
        if(activeCamera instanceof OrthographicCamera){
            OrthographicCamera orthoCam = (OrthographicCamera) activeCamera;
            orthoCam.setRenderIconsThresholdRatio(0.025);
            orthoCam.setIconOffset(new Vector2D(0, 7));
            orthoCam.setIconSize(8f);
        }

        //Render to framebuffer, copy from framebuffer to image, convert image to javafx image, display javafx image
        renderer.render(world, frameBuffer, activeCamera);
        frameBuffer.readPixels(imageBackingBuffer);
        image = SwingFXUtils.toFXImage(awtImage, image);
        imageView.setImage(image);
    }

    private void setCameraAspectRatio(Camera camera, double aspectRatio){
        if(camera instanceof PerspectiveCamera){
            PerspectiveCamera perspCam = (PerspectiveCamera)camera;
            //TODO: pick a permanent preference and replace hotfix with real solution that stops warping and remembers target FOV
            boolean preferNoStretchOverCorrectFOV = true;
            if(preferNoStretchOverCorrectFOV){
                double targetFOV = Math.toRadians(120);
                double fovX = (float)(targetFOV*aspectRatio);
                double fovY = targetFOV;
                if(fovX >= Math.PI){
                    fovX = Math.PI-0.01;
                    fovY = (float)(Math.PI/aspectRatio);
                }

                perspCam.setHorizontalFOV((float)fovX);
                perspCam.setVerticalFOV((float)fovY);
            }else{
                perspCam.setHorizontalFOV((float)(2.0d * Math.atan(Math.tan(perspCam.getVerticalFOV()/2.0d) * aspectRatio)));
            }
        } else if(camera instanceof OrthographicCamera) {
            OrthographicCamera orthoCam = (OrthographicCamera) camera;
            orthoCam.setHeight(orthoCam.getWidth()/(float)aspectRatio);
        }
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
