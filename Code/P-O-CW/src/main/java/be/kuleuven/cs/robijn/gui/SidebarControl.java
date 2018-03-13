package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.SimulationDriver;
import be.kuleuven.cs.robijn.common.UpdateEventHandler;
import interfaces.AutopilotInputs;
import interfaces.Path;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import interfaces.AutopilotOutputs;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Controller for the sidebar.
 */
public class SidebarControl extends VBox {
    @FXML
    private ToggleButton playButton;
    @FXML
    private ToggleButton pauseButton;

    @FXML
    private ToggleGroup simulationRunningToggleGroup;
    private BooleanProperty simulationRunningProperty = new SimpleBooleanProperty(this, "simulationRunning");
    private BooleanProperty simulationFinishedProperty = new SimpleBooleanProperty(this, "simulationFinished");
    private BooleanProperty simulationCrashedProperty = new SimpleBooleanProperty(this, "simulationCrashed");
    private BooleanProperty simulationThrewExceptionProperty = new SimpleBooleanProperty(this, "simulationThrewException");

    @FXML
    private Label simulationFinishedLabel;

    @FXML
    private Label simulationCrashedLabel;

    @FXML
    private Label simulationThrewExceptionLabel;

    /// TESTBED INFO

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
        simulationRunningProperty.bindBidirectional(playButton.selectedProperty());
        simulationRunningProperty.setValue(true);
        simulationRunningProperty.addListener(e -> getSimulation().setSimulationPaused(!simulationRunningProperty.get()));

        simulationProperty.addListener(e -> {
            if(getSimulation() == null){
                return;
            }
            getSimulation().addOnUpdateEventHandler(new UpdateEventHandler((inputs, outputs) -> {
                simulationFinishedProperty.set(getSimulation().hasSimulationFinished());
                simulationCrashedProperty.set(getSimulation().hasSimulationCrashed());
                simulationThrewExceptionProperty.set(getSimulation().hasSimulationThrownException());
                updateLabels(inputs, outputs);
            },UpdateEventHandler.LOW_PRIORITY));
        });
        playButton.disableProperty().bind(simulationFinishedProperty.or(simulationCrashedProperty).or(simulationThrewExceptionProperty));
        pauseButton.disableProperty().bind(simulationFinishedProperty.or(simulationCrashedProperty).or(simulationThrewExceptionProperty));

        simulationFinishedLabel.visibleProperty().bind(simulationFinishedProperty);
        simulationFinishedLabel.managedProperty().bind(simulationFinishedProperty);

        simulationCrashedLabel.visibleProperty().bind(simulationCrashedProperty);
        simulationCrashedLabel.managedProperty().bind(simulationCrashedProperty);

        simulationThrewExceptionLabel.visibleProperty().bind(simulationThrewExceptionProperty);
        simulationThrewExceptionLabel.managedProperty().bind(simulationThrewExceptionProperty);
    }

    private void updateLabels(AutopilotInputs inputs, AutopilotOutputs outputs){
        if(inputs == null || outputs == null){
            return;
        }

        positionLabel.setText(String.format("X:%6.2f  Y:%6.2f  Z:%6.2f", inputs.getX(), inputs.getY(), inputs.getZ()));

        //Heading
        setIndicatorValue(headingIndicator, remap360to180(inputs.getHeading()), Math.PI*2d);
        headingLabel.setText(String.format("%.2f", Math.toDegrees(inputs.getHeading())));

        //Pitch
        setIndicatorValue(pitchIndicator, remap360to180(inputs.getPitch()), Math.PI*2d);
        pitchLabel.setText(String.format("%.2f", Math.toDegrees(inputs.getPitch())));

        //Roll
        setIndicatorValue(rollIndicator, remap360to180(inputs.getRoll()), Math.PI*2d);
        rollLabel.setText(String.format("%.2f", Math.toDegrees(inputs.getRoll())));

        //Thrust
        //TODO
        //double thrustValue = outputs.getThrust()/getSimulation().getConfig().getMaxThrust();
        //thrustBar.setProgress(thrustValue);
        //thrustLabel.setText(String.format("%15.2f", outputs.getThrust()));

        //Wing inclination
        setIndicatorValue(leftWingInclinationIndicator, outputs.getLeftWingInclination(), Math.PI/2d);
        leftWingInclinationLabel.setText(String.format("%8.4f", Math.toDegrees(outputs.getLeftWingInclination())));

        setIndicatorValue(rightWingInclinationIndicator, outputs.getRightWingInclination(), Math.PI/2d);
        rightWingInclinationLabel.setText(String.format("%8.4f", Math.toDegrees(outputs.getRightWingInclination())));

        //Stabilizer inclination
        setIndicatorValue(horStabInclinationIndicator, outputs.getHorStabInclination(), Math.PI/2d);
        horStabInclinationLabel.setText(String.format("%8.4f", Math.toDegrees(outputs.getHorStabInclination())));

        setIndicatorValue(verStabInclinationIndicator, outputs.getVerStabInclination(), Math.PI/2d);
        verStabInclinationLabel.setText(String.format("%8.4f", Math.toDegrees(outputs.getVerStabInclination())));
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
}
