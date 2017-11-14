package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.SimulationDriver;
import be.kuleuven.cs.robijn.common.UpdateEventHandler;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import p_en_o_cw_2017.AutopilotInputs;
import p_en_o_cw_2017.AutopilotOutputs;

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
    private BooleanProperty simulationErrorProperty = new SimpleBooleanProperty(this, "simulationError");

    @FXML
    private Label simulationFinishedLabel;

    @FXML
    private Label simulationErrorLabel;

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
                simulationErrorProperty.set(getSimulation().hasSimulationCrashed());
                updateLabels(inputs, outputs);
            },UpdateEventHandler.LOW_PRIORITY));
        });
        playButton.disableProperty().bind(simulationFinishedProperty.or(simulationErrorProperty));
        pauseButton.disableProperty().bind(simulationFinishedProperty.or(simulationErrorProperty));

        simulationFinishedLabel.visibleProperty().bind(simulationFinishedProperty);
        simulationFinishedLabel.managedProperty().bind(simulationFinishedProperty);

        simulationErrorLabel.visibleProperty().bind(simulationErrorProperty);
        simulationErrorLabel.managedProperty().bind(simulationErrorProperty);
    }

    private void updateLabels(AutopilotInputs inputs, AutopilotOutputs outputs){
        positionLabel.setText(String.format("X:%6.2f  Y:%6.2f  Z:%6.2f", inputs.getX(), inputs.getY(), inputs.getZ()));

        //Heading
        headingIndicator.setProgress((inputs.getHeading()+Math.PI) / (2d*Math.PI));
        headingLabel.setText(String.format("%.2f", Math.toDegrees(inputs.getHeading())));

        //Pitch
        pitchIndicator.setProgress((inputs.getPitch()+Math.PI) / (2d*Math.PI));
        pitchLabel.setText(String.format("%.2f", Math.toDegrees(inputs.getPitch())));

        //Roll
        rollIndicator.setProgress((inputs.getRoll()+Math.PI) / (2d*Math.PI));
        rollLabel.setText(String.format("%.2f", Math.toDegrees(inputs.getRoll())));

        //Thrust
        double thrustValue = outputs.getThrust()/getSimulation().getConfig().getMaxThrust();
        thrustBar.setProgress(thrustValue);
        thrustLabel.setText(String.format("%15.2f", outputs.getThrust()));

        //Wing inclination
        leftWingInclinationIndicator.setProgress((outputs.getLeftWingInclination()+(Math.PI/2d)) / Math.PI);
        leftWingInclinationLabel.setText(String.format("%8.5f", outputs.getLeftWingInclination()));

        rightWingInclinationIndicator.setProgress((outputs.getRightWingInclination()+(Math.PI/2d)) / Math.PI);
        rightWingInclinationLabel.setText(String.format("%8.5f", outputs.getRightWingInclination()));

        //Stabilizer inclination
        horStabInclinationIndicator.setProgress((outputs.getHorStabInclination()+(Math.PI/2d)) / Math.PI);
        horStabInclinationLabel.setText(String.format("%8.5f", outputs.getHorStabInclination()));

        verStabInclinationIndicator.setProgress((outputs.getVerStabInclination()+(Math.PI/2d)) / Math.PI);
        verStabInclinationLabel.setText(String.format("%8.5f", outputs.getVerStabInclination()));
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
