package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.common.stopwatch.RealTimeStopwatch;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import be.kuleuven.cs.robijn.worldObjects.OrthographicCamera;
import be.kuleuven.cs.robijn.worldObjects.PerspectiveCamera;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.*;
import javafx.util.Duration;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.List;

/**
 * Controller for the main window layout
 */
public class MainController {
    @FXML
    private SplitPane contentRoot;

    @FXML
    private ListView<Drone> droneList;

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

    private ObjectProperty<Drone> selectedDroneProperty = new SimpleObjectProperty<>(this, "selectedDrone");
    private IntegerProperty selectedDroneIndexProperty = new SimpleIntegerProperty(this, "selectedDroneIndex");

    @FXML
    private void initialize(){
        //Setup simulation settings overlay
        initializeOverlay();
        initializeSimulationSettings();

        setupDroneList();

        //Setup CameraViewControls
        camerasViewRoot.setViewSupplier(() -> {
            CameraViewControl cameraView = new CameraViewControl();
            cameraView.getSimulationProperty().bind(simulationProperty);
            cameraView.getSelectedDronePropertyProperty().bind(selectedDroneProperty);
            return cameraView;
        });
        camerasViewRoot.initialize();

        //Setup sidebar
        sidebar.getSimulationProperty().bind(simulationProperty);
        sidebar.selectedDroneProperty().bind(selectedDroneProperty);
        sidebar.selectedDroneIndexProperty().bind(selectedDroneIndexProperty);
    }

    private void setupDroneList(){
        //Bind selected drone property to dronelist selection model
        selectedDroneProperty.bind(droneList.getSelectionModel().selectedItemProperty());
        selectedDroneIndexProperty.bind(droneList.getSelectionModel().selectedIndexProperty());

        //Add all drones
        simulationProperty.addListener((observableValue, oldValue, newValue) -> {
            List<Drone> drones = newValue.getTestBed().getWorldRepresentation().getChildrenOfType(Drone.class);
            droneList.getItems().addAll(drones);
            droneList.getSelectionModel().select(0);
        });


        droneList.setCellFactory(view -> new ListCell<Drone>(){
            @Override
            protected void updateItem(Drone item, boolean empty) {
                super.updateItem(item, empty);
                if(!empty && item != null){
                    setText(item.getDroneID());
                }
            }
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
            setSimulation(new SimulationDriver(e.getSimulationSettings()));
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
