package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.SimulationDriver;
import be.kuleuven.cs.robijn.common.SimulationSettings;
import be.kuleuven.cs.robijn.common.airports.AirportPackage;
import be.kuleuven.cs.robijn.common.airports.Gate;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackageListControl extends AnchorPane {
    private ObjectProperty<SimulationDriver> simulationProperty = new SimpleObjectProperty<>(this, "simulation");

    @FXML
    private TableView<AirportPackage> packageTable;

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
        setupTable();

        addRandomPackageButton.setOnAction(e -> {
            addRandomPackage();
        });

        addPackagesButton.setOnAction(e -> {

        });
    }

    public void setSelectedPackage(AirportPackage pkg){
        packageTable.getSelectionModel().select(pkg);
    }

    private void onPackageUpdate(AirportPackage pkg){
        switch (pkg.getState()){
            case AT_GATE:
            case IN_TRANSIT:
                packageTable.refresh();
                break;
            case DELIVERED:
                packageTable.getItems().remove(pkg);
                break;
        }
    }

    public void addPackage(Gate originGate, Gate targetGate){
        AirportPackage newPackage = getSimulation().addPackage(originGate, targetGate);
        newPackage.addStateUpdateEventHandler(this::onPackageUpdate);
        packageTable.getItems().add(newPackage);
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

    private void setupTable(){
        packageTable.setEditable(false);

        //Column 1 is origin
        TableColumn<AirportPackage, String> originColumn = new TableColumn<>();
        originColumn.setText("Origin");
        originColumn.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue().getOrigin().getUID()));
        originColumn.setPrefWidth(53);
        packageTable.getColumns().add(originColumn);

        //Column 2 is destination
        TableColumn<AirportPackage, String> destinationColumn = new TableColumn<>();
        destinationColumn.setText("Destination");
        destinationColumn.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue().getDestination().getUID()));
        destinationColumn.setPrefWidth(55);
        packageTable.getColumns().add(destinationColumn);

        //Column 3 is state
        TableColumn<AirportPackage, String> stateColumn = new TableColumn<>();
        stateColumn.setText("State");
        stateColumn.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue().getState().toString()));
        stateColumn.setPrefWidth(85);
        packageTable.getColumns().add(stateColumn);

        //Column 4 is current gate
        TableColumn<AirportPackage, String> gateColumn = new TableColumn<>();
        gateColumn.setText("Gate");
        gateColumn.setCellValueFactory(cd -> Bindings.createStringBinding(() -> {
            if(cd.getValue().getState() == AirportPackage.State.AT_GATE){
                return cd.getValue().getCurrentGate().getUID();
            }
            return "";
        }));
        gateColumn.setPrefWidth(42);
        packageTable.getColumns().add(gateColumn);

        //Column 4 is current gate
        TableColumn<AirportPackage, String> transporterColumn = new TableColumn<>();
        transporterColumn.setText("Transporter");
        transporterColumn.setCellValueFactory(cd -> Bindings.createStringBinding(() -> {
            if(cd.getValue().getState() == AirportPackage.State.IN_TRANSIT){
                return cd.getValue().getCurrentTransporter().getDroneID();
            }
            return "";
        }));
        transporterColumn.setPrefWidth(95);
        packageTable.getColumns().add(transporterColumn);
    }

    SimulationDriver getSimulation(){
        return simulationProperty().get();
    }

    public ObjectProperty<SimulationDriver> simulationProperty() {
        return simulationProperty;
    }
}
