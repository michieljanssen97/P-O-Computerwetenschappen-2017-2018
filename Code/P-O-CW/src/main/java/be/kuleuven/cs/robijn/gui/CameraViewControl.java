package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.testbed.renderer.bmfont.Label3D;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;

/**
 * GUI component that displays a 3D render of the world, along with several buttons for changing the view
 */
public class CameraViewControl extends AnchorPane {
    //The object names for the cameras that provide the different perspectives.
    public static final String SIDE_CAMERA_ID = "gui_side_camera";
    public static final String TOPDOWN_CAMERA_ID = "gui_topdown_camera";
    public static final String DRONE_CAMERA_ID = "gui_drone_camera";
    public static final String THIRDPERSON_CAMERA_ID = "gui_thirdperson_camera";

    @FXML
    private ScrollPane imageViewHost;

    @FXML
    private ImageView imageView;

    @FXML
    private ToggleGroup perspectiveToggleGroup;

    @FXML
    private ComboBox<Drone> droneComboBox;

    private ObjectProperty<SimulationDriver> simulationProperty = new SimpleObjectProperty<>(this, "simulation");
    //Drone that is selected in sidebar
    private ObjectProperty<Drone> selectedDroneProperty = new SimpleObjectProperty<>(this, "selectedDrone");
    //Drone that is selected in combobox
    private ObjectProperty<Drone> activeDroneProperty = new SimpleObjectProperty<>(this, "activeDrone");

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
        setupCameras();
        setupLabels();
        setupDroneComboBox();
        setupDragging();
        setupResizing();
        setupPerspectiveChanging();
        setupRendering();
    }

    private OrthographicCamera sideCamera, topCamera;
    private PerspectiveCamera droneCamera, chaseCamera;

    private void setupCameras(){
        simulationProperty.addListener((observableValue, oldValue, newValue) -> {
            WorldObject world = newValue.getTestBed().getWorldRepresentation();

            activeCamera = sideCamera = getSimulation().getTestBed().getRenderer().createOrthographicCamera();
            sideCamera.setWidth(130);
            sideCamera.setHeight(30);
            sideCamera.setName(CameraViewControl.SIDE_CAMERA_ID);
            sideCamera.setRelativePosition(new ArrayRealVector(new double[]{1000, 5, -55}, false));
            sideCamera.setRelativeRotation(new Rotation(new Vector3D(0, 1, 0), Math.PI/2d));
            sideCamera.setFarPlane(100000);
            world.addChild(sideCamera);

            topCamera = getSimulation().getTestBed().getRenderer().createOrthographicCamera();
            topCamera.setWidth(130);
            topCamera.setHeight(40);
            topCamera.setName(CameraViewControl.TOPDOWN_CAMERA_ID);
            topCamera.setRelativePosition(new ArrayRealVector(new double[]{0, 1000, -55}, false));
            Rotation rot = new Rotation(new Vector3D(0, 0, 1), Math.PI/2d)
                    .applyTo(new Rotation(new Vector3D(0, 1, 0), Math.PI/2d));
            topCamera.setRelativeRotation(rot);
            topCamera.setFarPlane(100000);
            world.addChild(topCamera);

            chaseCamera = getSimulation().getTestBed().getRenderer().createPerspectiveCamera();
            chaseCamera.setHorizontalFOV((float)Math.toRadians(120));
            chaseCamera.setVerticalFOV((float)Math.toRadians(120));
            chaseCamera.setName(CameraViewControl.THIRDPERSON_CAMERA_ID);
            chaseCamera.setRelativePosition(new ArrayRealVector(new double[]{0, 0d, 7}, false));
            getSimulation().addOnUpdateEventHandler(new UpdateEventHandler((inputs, outputs) -> {
                Drone drone = activeDroneProperty.get();
                if(drone != null){
                    //Put camera at rotation (0, 0, 0), at position of drone +7 on z-axis.
                    chaseCamera.setRelativePosition(drone.getRelativePosition().add(new ArrayRealVector(new double[]{0, 0, 7}, false)));
                    chaseCamera.setRelativeRotation(Rotation.IDENTITY);

                    //Perform rotatearound of camera around drone position along y-axis with plane yaw.
                    chaseCamera.rotateAround(drone.getWorldPosition(), new Rotation(new Vector3D(0, 1, 0), drone.getHeading()));
                }
            },UpdateEventHandler.HIGH_PRIORITY));
            world.addChild(chaseCamera);
        });

        activeDroneProperty.addListener((observableValue, oldDrone, newDrone) -> {
            droneCamera = newDrone.getChildByName(DRONE_CAMERA_ID, PerspectiveCamera.class);
            if(droneCamera == null){
                droneCamera = getSimulation().getTestBed().getRenderer().createPerspectiveCamera();
                droneCamera.setHorizontalFOV((float)Math.toRadians(120));
                droneCamera.setVerticalFOV((float)Math.toRadians(120));
                droneCamera.setName(CameraViewControl.DRONE_CAMERA_ID);
                droneCamera.addVisibilityFilter(obj -> obj != newDrone); //Hide drone from itself
                droneCamera.setRelativePosition(new ArrayRealVector(new double[]{0, 0, 0}, false));
                newDrone.addChild(droneCamera);
            }
            setActiveCamera((String)perspectiveToggleGroup.getSelectedToggle().getUserData());
        });
    }

    private void setupLabels(){
        simulationProperty.addListener((observableValue, oldValue, newValue) -> {
            WorldObject world = newValue.getTestBed().getWorldRepresentation();

            for (Drone drone : world.getChildrenOfType(Drone.class)){
                Label3D label = new Label3D(drone.getDroneID());
                label.setRelativePosition(new ArrayRealVector(new double[]{0, 1, 0}, false));
                drone.addChild(label);
            }

            ArrayList<Airport> childrenOfType = world.getChildrenOfType(Airport.class);
            for (int i = 0; i < childrenOfType.size(); i++) {
                Airport airport = childrenOfType.get(i);
                Label3D label = new Label3D("Airport "+i);
                label.setRelativePosition(new ArrayRealVector(new double[]{0, 1, 0}, false));
                airport.addChild(label);
            }
        });
    }

    private void setActiveCamera(String cameraId){
        switch(cameraId){
            case SIDE_CAMERA_ID:
                activeCamera = sideCamera;
                break;
            case TOPDOWN_CAMERA_ID:
                activeCamera = topCamera;
                break;
            case DRONE_CAMERA_ID:
                activeCamera = droneCamera;
                break;
            case THIRDPERSON_CAMERA_ID:
                activeCamera = chaseCamera;
                break;
        }
    }

    private void setupResizing(){
        //Stretch imageView to fill scrollview, which has its size controlled by the SplittablePane
        imageViewHost.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        imageViewHost.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        imageView.fitWidthProperty().bind(this.widthProperty());
        imageView.fitHeightProperty().bind(this.heightProperty());

        //When the image viewport changes, resize the framebuffer and image buffers
        imageView.fitWidthProperty().addListener(e -> onSizeChanged());
        imageView.fitHeightProperty().addListener(e -> onSizeChanged());
    }

    private void setupDragging(){
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
    }

    private void setupPerspectiveChanging(){
        //There must always be an active perspective, so if no perspective is selected, select the first one
        perspectiveToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue == null){
                perspectiveToggleGroup.selectToggle(perspectiveToggleGroup.getToggles().get(0));
            }
        });

        //Change camera on toggle button press
        perspectiveToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                setActiveCamera((String)newValue.getUserData());
            }
        });
    }

    private void setupRendering(){
        //When the simulation starts, setup the rendering
        simulationProperty.addListener((observable, oldValue, newValue) -> {
            //Setup framebuffers and image buffers
            setupImages();

            //When the simulation is updated, update the displayed image
            newValue.addOnUpdateEventHandler(new UpdateEventHandler((inputs, outputs) -> {
                update();
            }, UpdateEventHandler.LOW_PRIORITY));
        });
    }

    private void setupDroneComboBox(){
        //droneComboBox is visible only when drone or chase camera is selected
        droneComboBox.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            Toggle selectedToggle = perspectiveToggleGroup.getSelectedToggle();
            if(selectedToggle != null){
                String cameraId = (String)selectedToggle.getUserData();
                return cameraId.equals(DRONE_CAMERA_ID) || cameraId.equals(THIRDPERSON_CAMERA_ID);
            }
            return false;
        }, perspectiveToggleGroup.selectedToggleProperty()));

        //Display the drone ID in the combobox
        Callback<ListView<Drone>, ListCell<Drone>> cellFactory = data -> new ListCell<Drone>(){
            public void updateItem(Drone item, boolean empty) {
                super.updateItem(item, empty);
                if(empty){
                    setText("");
                }else if (item == null) {
                    setText("Active drone");
                } else {
                    setText(item.getDroneID());
                }
            }
        };
        droneComboBox.setCellFactory(cellFactory);
        droneComboBox.setButtonCell(cellFactory.call(null));

        //Add all the drones to the combobox + a null value for selectedDroneProperty
        simulationProperty.addListener((observableValue, oldValue, newValue) -> {
            droneComboBox.getItems().add(null);
            droneComboBox.getItems().addAll(
                    newValue.getTestBed().getWorldRepresentation().getChildrenOfType(Drone.class)
            );
        });

        //On droneCombobox select, change activecamera
        droneComboBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue == null){
                activeDroneProperty.set(selectedDroneProperty.get());
            }else{
                activeDroneProperty.set(newValue);
            }
        });

        selectedDroneProperty.addListener((observableValue, oldValue, newValue) -> {
            if(droneComboBox.getSelectionModel().getSelectedItem() == null){
                activeDroneProperty.set(newValue);
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
        //If no camera is active, don't render.
        if(activeCamera == null){
            return;
        }

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

    public Drone getSelectedDroneProperty() {
        return selectedDroneProperty.get();
    }

    public ObjectProperty<Drone> getSelectedDronePropertyProperty() {
        return selectedDroneProperty;
    }

    public Drone getActiveDroneProperty() {
        return activeDroneProperty.get();
    }

    public ObjectProperty<Drone> getActiveDronePropertyProperty() {
        return activeDroneProperty;
    }
}
