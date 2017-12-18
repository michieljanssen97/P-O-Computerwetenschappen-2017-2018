package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.common.stopwatch.ConstantIntervalStopwatch;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.*;
import javafx.util.Duration;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * Controller for the main window layout
 */
public class MainController {
    @FXML
    private SplitPane contentRoot;

    @FXML
    private AnchorPane overlayRoot;
    private final BooleanProperty overlayVisible = new SimpleBooleanProperty(this, "overlayVisible");

    @FXML
    private SimulationSettingsControl simulationSettingsControl;

    @FXML
    private SidebarControl sidebar;

    @FXML
    private SplittablePane camerasViewRoot;

    private ObjectProperty<SimulationDriver> simulationProperty = new SimpleObjectProperty<>(this, "simulation");

    @FXML
    private void initialize(){
        //Setup simulation settings overlay
        initializeOverlay();
        initializeSimulationSettings();

        //Setup CameraViewControls
        camerasViewRoot.setViewSupplier(() -> {
            CameraViewControl cameraView = new CameraViewControl();
            cameraView.getSimulationProperty().bind(simulationProperty);
            return cameraView;
        });
        camerasViewRoot.initialize();

        //Setup sidebar
        sidebar.getSimulationProperty().bind(simulationProperty);

        //Setup cameras in simulation world
        simulationProperty.addListener(e -> {
            WorldObject world = getSimulation().getTestBed().getWorldRepresentation();

            Drone drone = world.getFirstChildOfType(Drone.class);

            PerspectiveCamera droneCamera = getSimulation().getTestBed().getRenderer().createPerspectiveCamera();
            droneCamera.setHorizontalFOV((float)Math.toRadians(120));
            droneCamera.setVerticalFOV((float)Math.toRadians(120));
            droneCamera.setName(CameraViewControl.DRONE_CAMERA_ID);
            droneCamera.setDronesHidden(true);
            droneCamera.setRelativePosition(new ArrayRealVector(new double[]{0, 0, 0}, false));
            droneCamera.setDrawnDebugObjects(true);
            drone.addChild(droneCamera);

            PerspectiveCamera chaseCamera = getSimulation().getTestBed().getRenderer().createPerspectiveCamera();
            chaseCamera.setHorizontalFOV((float)Math.toRadians(120));
            chaseCamera.setVerticalFOV((float)Math.toRadians(120));
            chaseCamera.setName(CameraViewControl.THIRDPERSON_CAMERA_ID);
            chaseCamera.setRelativePosition(new ArrayRealVector(new double[]{0, 0d, 7}, false));
            chaseCamera.setDrawnDebugObjects(true);
            getSimulation().addOnUpdateEventHandler(new UpdateEventHandler((inputs, outputs) -> {
                //Put camera at rotation (0, 0, 0), at position of drone +7 on z-axis.
                chaseCamera.setRelativePosition(drone.getRelativePosition().add(new ArrayRealVector(new double[]{0, 0, 7}, false)));
                chaseCamera.setRelativeRotation(Rotation.IDENTITY);

                //Perform rotatearound of camera around drone position along y-axis with plane yaw.
                chaseCamera.rotateAround(drone.getWorldPosition(), new Rotation(new Vector3D(0, 1, 0), drone.getHeading()));
            },UpdateEventHandler.HIGH_PRIORITY));
            world.addChild(chaseCamera);

            OrthographicCamera sideCamera = getSimulation().getTestBed().getRenderer().createOrthographicCamera();
            sideCamera.setWidth(130);
            sideCamera.setHeight(30);
            sideCamera.setName(CameraViewControl.SIDE_CAMERA_ID);
            sideCamera.setRelativePosition(new ArrayRealVector(new double[]{1000, 5, -55}, false));
            sideCamera.setRelativeRotation(new Rotation(new Vector3D(0, 1, 0), Math.PI/2d));
            sideCamera.setFarPlane(100000);
            sideCamera.setDrawnDebugObjects(true);
            world.addChild(sideCamera);

            OrthographicCamera topCamera = getSimulation().getTestBed().getRenderer().createOrthographicCamera();
            topCamera.setWidth(130);
            topCamera.setHeight(40);
            topCamera.setName(CameraViewControl.TOPDOWN_CAMERA_ID);
            topCamera.setRelativePosition(new ArrayRealVector(new double[]{0, 1000, -55}, false));
            Rotation rot = new Rotation(new Vector3D(0, 0, 1), Math.PI/2d)
                    .applyTo(new Rotation(new Vector3D(0, 1, 0), Math.PI/2d));
            topCamera.setRelativeRotation(rot);
            topCamera.setFarPlane(100000);
            topCamera.setDrawnDebugObjects(true);
            world.addChild(topCamera);
        });
    }

    /////////////////
    //// OVERLAY ////
    /////////////////

    private void initializeOverlay(){
        overlayVisibleProperty().addListener(e -> {
            contentRoot.setEffect(isOverlayVisible() ? new BoxBlur(40, 40, 3) : null);
        });
        overlayRoot.visibleProperty().bind(overlayVisibleProperty());
        overlayRoot.mouseTransparentProperty().bind(overlayVisibleProperty().not());
        setOverlayVisible(true);
    }

    public void setOverlayVisible(boolean visible){
        overlayVisible.setValue(visible);
    }

    public boolean isOverlayVisible(){
        return overlayVisible.getValue();
    }

    public BooleanProperty overlayVisibleProperty() {
        return overlayVisible;
    }

    /////////////////////
    //// SIMULATION  ////
    /////////////////////

    public SimulationDriver getSimulation() {
        return simulationProperty.get();
    }

    public void setSimulation(SimulationDriver simulationProperty) {
        this.simulationProperty.set(simulationProperty);
    }

    public ObjectProperty<SimulationDriver> getSimulationProperty() {
        return simulationProperty;
    }

    private void initializeSimulationSettings(){
        simulationSettingsControl.addEventFilter(SimulationSettingsConfirmEvent.CONFIRM, e -> {
            setOverlayVisible(false);
            setSimulation(new SimulationDriver(e.getBoxes(), e.getSettings(), new ConstantIntervalStopwatch(1.0/30.0)));
            startSimulation();
        });
    }

    //Starts running the simulation.
    private void startSimulation(){
        int targetFPS = 30;
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000/targetFPS), e -> {
            getSimulation().runUpdate();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}
