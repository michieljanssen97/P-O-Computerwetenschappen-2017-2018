package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.*;

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

            Camera droneCamera = getSimulation().getTestBed().getRenderer().createCamera();
            droneCamera.setHorizontalFOV((float)Math.toRadians(120));
            droneCamera.setVerticalFOV((float)Math.toRadians(120));
            droneCamera.setName(CameraViewControl.DRONE_CAMERA_ID);
            droneCamera.setDronesHidden(true);
            droneCamera.setRelativePosition(new ArrayRealVector(new double[]{0, 0, 0}, false));
            drone.addChild(droneCamera);

            Camera chaseCamera = getSimulation().getTestBed().getRenderer().createCamera();
            chaseCamera.setHorizontalFOV((float)Math.toRadians(120));
            chaseCamera.setVerticalFOV((float)Math.toRadians(120));
            chaseCamera.setName(CameraViewControl.THIRDPERSON_CAMERA_ID);
            chaseCamera.setRelativePosition(new ArrayRealVector(new double[]{0, 0d, 7}, false));
            getSimulation().addOnUpdateEventHandler((inputs, outputs) -> {
                //Put camera at rotation (0, 0, 0), at position of drone +7 on z-axis.
                chaseCamera.setRelativePosition(drone.getRelativePosition().add(new ArrayRealVector(new double[]{0, 0, 7}, false)));
                chaseCamera.setRelativeRotation(Rotation.IDENTITY);

                //Perform rotatearound of camera around drone position along y-axis with plane yaw.
                chaseCamera.rotateAround(drone.getWorldPosition(), new Rotation(new Vector3D(0, 1, 0), drone.getHeading()));
            });
            world.addChild(chaseCamera);

            Box box = world.getFirstChildOfType(Box.class);
            Camera boxCamera = getSimulation().getTestBed().getRenderer().createCamera();
            boxCamera.setHorizontalFOV((float)Math.toRadians(120));
            boxCamera.setVerticalFOV((float)Math.toRadians(120));
            boxCamera.setName(CameraViewControl.BOX_CAMERA_ID);
            boxCamera.setRelativePosition(new ArrayRealVector(new double[]{0, 0, 0}, false));
            boxCamera.setRelativeRotation(new Rotation(new Vector3D(0, 1, 0), Math.PI));
            box.addChild(boxCamera);
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
            setSimulation(new SimulationDriver(e.getSettings()));
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
