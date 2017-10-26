package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.SimulationDriver;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Controller for the sidebar.
 */
public class SidebarControl extends VBox {
    @FXML
    private ToggleButton playButton;
    @FXML
    private ToggleGroup simulationRunningToggleGroup;
    private BooleanProperty simulationRunningProperty = new SimpleBooleanProperty(this, "simulationRunning");

    @FXML
    private ProgressBar progressBar;

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
