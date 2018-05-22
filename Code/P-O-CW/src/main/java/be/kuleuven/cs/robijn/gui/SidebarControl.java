package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.SimulationDriver;
import be.kuleuven.cs.robijn.common.UpdateEventHandler;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Controller for the sidebar.
 */
public class SidebarControl extends VBox {
    @FXML
    private ToggleButton pauseButton;
    @FXML
    private ToggleButton playButton;
    @FXML
    private Slider simulationSpeedSlider;

    @FXML
    private ToggleGroup simulationRunningToggleGroup;
    private BooleanProperty simulationRunningProperty = new SimpleBooleanProperty(this, "simulationRunning");
    private BooleanProperty simulationFinishedProperty = new SimpleBooleanProperty(this, "simulationFinished");
    private BooleanProperty simulationCrashedProperty = new SimpleBooleanProperty(this, "simulationCrashed");
    private BooleanProperty outOfControlProperty = new SimpleBooleanProperty(this, "outOfControl");
    private BooleanProperty simulationThrewExceptionProperty = new SimpleBooleanProperty(this, "simulationThrewException");

    @FXML
    private Label simulationFinishedLabel;

    @FXML
    private Label simulationCrashedLabel;
    
    @FXML
    private Label outOfControlLabel;

    @FXML
    private Label simulationThrewExceptionLabel;

    /// TESTBED INFO

    @FXML
    private Label upsLabel;

    @FXML
    private Label positionLabel;

    @FXML
    private ProgressIndicator headingIndicator;

    @FXML
    private Label headingLabel;

    @FXML
    private ProgressIndicator pitchIndicator;

    @FXML
    private Label pitchLabel;

    @FXML
    private ProgressIndicator rollIndicator;

    @FXML
    private Label rollLabel;

    /// AUTOPILOT INFO

    //Thrust
    @FXML
    private ProgressBar thrustBar;

    @FXML
    private Label thrustLabel;

    //Wing inclination
    @FXML
    private ProgressIndicator leftWingInclinationIndicator;

    @FXML
    private Label leftWingInclinationLabel;

    @FXML
    private ProgressIndicator rightWingInclinationIndicator;

    @FXML
    private Label rightWingInclinationLabel;

    //Stabilizer inclination
    @FXML
    private ProgressIndicator horStabInclinationIndicator;

    @FXML
    private Label horStabInclinationLabel;

    @FXML
    private ProgressIndicator verStabInclinationIndicator;

    @FXML
    private Label verStabInclinationLabel;

    //@FXML
    //private ProgressBar progressBar;

    private String negativeValueColor = "red";
    private String positiveValueColor = "blue";

    private ObjectProperty<SimulationDriver> simulationProperty = new SimpleObjectProperty<>(this, "simulation");

    private ObjectProperty<Drone> selectedDroneProperty = new SimpleObjectProperty<>(this, "selectedDrone");
    private IntegerProperty selectedDroneIndexProperty = new SimpleIntegerProperty(this, "selectedDroneIndex");

    public SidebarControl(){
        //Load the layout associated with this GUI control.
        FXMLLoader fxmlLoader = new FXMLLoader(Resources.getResourceURL("/layouts/sidebar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @FXML
    private void initialize(){
        //Bind the play/pause buttons to the simulation pause variable.
        simulationRunningProperty.bind(
                playButton.selectedProperty()
                    .and(simulationSpeedSlider.valueProperty().greaterThan(0))
        );
        playButton.selectedProperty().setValue(true);
        simulationRunningToggleGroup.selectedToggleProperty().addListener((observableValue, toggle, newSelectedToggle) -> {
            if(newSelectedToggle == playButton && simulationSpeedSlider.getValue() == 0){
                simulationSpeedSlider.setValue(1);
            }
            if(newSelectedToggle == null){
                pauseButton.selectedProperty().setValue(true);
            }
        });
        simulationRunningProperty.addListener(e -> getSimulation().setSimulationPaused(!simulationRunningProperty.get()));
        simulationSpeedSlider.valueProperty().addListener((observableValue, oldVal, newVal) -> {
            if(newVal.doubleValue() == 0){
                pauseButton.selectedProperty().setValue(true);
            }else{
                getSimulation().setSimulationSpeedMultiplier(newVal.doubleValue());
            }
        });

        //Bind the play/fastforward/lightspeed buttons to the simulation speed multiplier
        playButton.selectedProperty().addListener((property, wasSelected, isSelected) -> {
            if(isSelected){
                getSimulation().setSimulationSpeedMultiplier(1.0d);
            }
        });

        simulationProperty.addListener(e -> {
            if(getSimulation() == null){
                return;
            }
            getSimulation().addOnUpdateEventHandler(new UpdateEventHandler((inputs, outputs) -> {
                simulationFinishedProperty.set(getSimulation().hasSimulationFinished());
                simulationCrashedProperty.set(getSimulation().hasSimulationCrashed());
                outOfControlProperty.set(getSimulation().isOutOfControl());
                simulationThrewExceptionProperty.set(getSimulation().hasSimulationThrownException());
                updateLabels(inputs, outputs);
            },UpdateEventHandler.LOW_PRIORITY));
        });
        BooleanBinding canPlay = simulationFinishedProperty
        		.or(simulationCrashedProperty)
        		.or(simulationThrewExceptionProperty)
        		.or(outOfControlProperty);
        pauseButton.disableProperty().bind(canPlay);
        playButton.disableProperty().bind(canPlay);
        simulationSpeedSlider.disableProperty().bind(canPlay);

        simulationFinishedLabel.visibleProperty().bind(simulationFinishedProperty);
        simulationFinishedLabel.managedProperty().bind(simulationFinishedProperty);

        simulationCrashedLabel.visibleProperty().bind(simulationCrashedProperty);
        simulationCrashedLabel.managedProperty().bind(simulationCrashedProperty);
        
        outOfControlLabel.visibleProperty().bind(outOfControlProperty);
        outOfControlLabel.managedProperty().bind(outOfControlProperty);

        simulationThrewExceptionLabel.visibleProperty().bind(simulationThrewExceptionProperty);
        simulationThrewExceptionLabel.managedProperty().bind(simulationThrewExceptionProperty);
    }

    private void updateLabels(AutopilotInputs[] inputs, AutopilotOutputs[] outputs){
        if(inputs == null || outputs == null || getSelectedDrone() == null){
            return;
        }

        int index = selectedDroneIndexProperty.get();
        AutopilotInputs in = inputs[index];
        AutopilotOutputs out = outputs[index];

        positionLabel.setText(String.format("X:%6.2f  Y:%6.2f  Z:%6.2f", in.getX(), in.getY(), in.getZ()));

        upsLabel.setText(getSimulation().getUpdatesPerSecond()+"");

        //Heading
        setIndicatorValue(headingIndicator, remap360to180(in.getHeading()), Math.PI*2d);
        headingLabel.setText(String.format("%.2f", Math.toDegrees(in.getHeading())));

        //Pitch
        setIndicatorValue(pitchIndicator, remap360to180(in.getPitch()), Math.PI*2d);
        pitchLabel.setText(String.format("%.2f", Math.toDegrees(in.getPitch())));

        //Roll
        setIndicatorValue(rollIndicator, remap360to180(in.getRoll()), Math.PI*2d);
        rollLabel.setText(String.format("%.2f", Math.toDegrees(in.getRoll())));

        //Thrust
        double thrustValue = out.getThrust()/getSelectedDrone().getConfig().getMaxThrust();
        thrustBar.setProgress(thrustValue);
        thrustLabel.setText(String.format("%15.2f", out.getThrust()));

        //Wing inclination
        setIndicatorValue(leftWingInclinationIndicator, out.getLeftWingInclination(), Math.PI/2d);
        leftWingInclinationLabel.setText(String.format("%8.4f", Math.toDegrees(out.getLeftWingInclination())));

        setIndicatorValue(rightWingInclinationIndicator, out.getRightWingInclination(), Math.PI/2d);
        rightWingInclinationLabel.setText(String.format("%8.4f", Math.toDegrees(out.getRightWingInclination())));

        //Stabilizer inclination
        setIndicatorValue(horStabInclinationIndicator, out.getHorStabInclination(), Math.PI/2d);
        horStabInclinationLabel.setText(String.format("%8.4f", Math.toDegrees(out.getHorStabInclination())));

        setIndicatorValue(verStabInclinationIndicator, out.getVerStabInclination(), Math.PI/2d);
        verStabInclinationLabel.setText(String.format("%8.4f", Math.toDegrees(out.getVerStabInclination())));
    }

    //Remaps [0;(2*PI)] to [-PI;PI]
    private double remap360to180(double input){
        return input < Math.PI ? input : input - (Math.PI*2d);
    }

    private void setIndicatorValue(ProgressIndicator indicator, double value, double maxValue){
        indicator.setProgress(Math.abs(value) / maxValue);
        StringBuilder style = new StringBuilder();
        style.append("-fx-progress-color: ").append(value > 0 ? positiveValueColor : negativeValueColor).append("; ");
        style.append("-fx-scale-x: ").append(value > 0 ? 1 : -1).append("; ");
        indicator.setStyle(style.toString());
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

    public Drone getSelectedDrone() {
        return selectedDroneProperty.get();
    }

    public ObjectProperty<Drone> selectedDroneProperty() {
        return selectedDroneProperty;
    }

    public int getSelectedDroneIndex() {
        return selectedDroneIndexProperty.get();
    }

    public IntegerProperty selectedDroneIndexProperty() {
        return selectedDroneIndexProperty;
    }
}
