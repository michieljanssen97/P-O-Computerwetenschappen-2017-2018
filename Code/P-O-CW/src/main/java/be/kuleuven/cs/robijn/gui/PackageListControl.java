package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.SimulationDriver;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.AirportPackage;
import be.kuleuven.cs.robijn.common.airports.Gate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackageListControl extends AnchorPane {
    private ObjectProperty<SimulationDriver> simulationProperty = new SimpleObjectProperty<>(this, "simulation");

    @FXML
    private ListView<AirportPackage> packageList;

    @FXML
    private Button addRandomPackageButton;

    @FXML
    private Button addPackagesButton;

    private Random random = new Random();

    public PackageListControl(){
        //Load the layout associated with this control.
        FXMLLoader fxmlLoader = new FXMLLoader(Resources.getResourceURL("/layouts/package_list_view.fxml"));
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
        addRandomPackageButton.setOnAction(e -> {
            addRandomPackage();
        });

        addPackagesButton.setOnAction(e -> {

        });
    }

    public void setSelectedPackage(AirportPackage pkg){
        packageList.getSelectionModel().select(pkg);
    }

    public void addPackage(Gate originGate, Gate targetGate){
        AirportPackage newPackage = getSimulation().addPackage(originGate, targetGate);
        newPackage.addDeliveryEventHandler(p -> packageList.getItems().remove(p));
        packageList.getItems().add(newPackage);
    }

    public void addRandomPackage(Gate originGate){
        Gate[] gates = getSimulation().getTestBed().getWorldRepresentation()
                .getDescendantsStream()
                .filter(obj -> obj instanceof Gate && originGate != obj)
                .map(o -> (Gate)o)
                .toArray(Gate[]::new);

        Gate destinationGate = gates[random.nextInt(gates.length)];

        addPackage(originGate, destinationGate);
    }

    public void addRandomPackage(){
        Gate[] gates = getSimulation().getTestBed().getWorldRepresentation()
                .getDescendantsStream()
                .filter(obj -> obj instanceof Gate)
                .map(o -> (Gate)o)
                .toArray(Gate[]::new);

        Gate destinationGate = gates[random.nextInt(gates.length)];

        List<Gate> originGateCandidates = Stream.of(gates)
                .filter(g -> g != destinationGate && !g.hasPackage())
                .collect(Collectors.toList());

        if(originGateCandidates.size() == 0){
            return;
        }

        Gate originGate = originGateCandidates.get(0);

        addPackage(originGate, destinationGate);
    }

    SimulationDriver getSimulation(){
        return simulationProperty().get();
    }

    public ObjectProperty<SimulationDriver> simulationProperty() {
        return simulationProperty;
    }
}
