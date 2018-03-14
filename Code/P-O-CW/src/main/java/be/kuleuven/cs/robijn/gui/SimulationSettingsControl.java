package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.SimulationSettings;
import be.kuleuven.cs.robijn.common.SimulationSettings.AirportDefinition;
import be.kuleuven.cs.robijn.common.SimulationSettings.DroneDefinition;
import interfaces.AutopilotConfig;
import interfaces.AutopilotConfigReader;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Controller for the simulation settings overlay
 */
public class SimulationSettingsControl extends AnchorPane {

    /// AIRPORT WIDTH & HEIGHT

    @FXML
    private Spinner<Float> runwayLengthSpinner;

    @FXML
    private Spinner<Float> gateLengthSpinner;

    /// AIRPORTS SETUP

    @FXML
    private Button addAirportButton;

    @FXML
    private Button removeAirportButton;

    @FXML
    private Button loadAirportsConfigFileButton;

    @FXML
    private Button saveAirportsConfigFileButton;

    @FXML
    private Button generateAirportsButton;

    @FXML
    private TableView airportsTable;

    /// DRONES SETUP

    @FXML
    private Button addDroneButton;

    @FXML
    private Button removeDroneButton;

    @FXML
    private Button loadDroneSetupFileButton;

    @FXML
    private Button saveDroneSetupFileButton;

    @FXML
    private Button loadDroneSetupDefaultsButton;

    @FXML
    private TableView dronesTable;

    /// MISC

    @FXML
    private Button okButton;


    public SimulationSettingsControl(){
        //Load the layout associated with this control.
        FXMLLoader fxmlLoader = new FXMLLoader(Resources.getResourceURL("/layouts/simulation_settings.fxml"));
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
        setupAirportsTab();
        setupDronesTab();

        //User can only click OK if number of airports > 0 and number of drones > 0
        okButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> airportsTable.getItems().size() == 0 ||
                        dronesTable.getItems().size() == 0 ||
                        dronesTable.getItems().stream().anyMatch(d -> {
                           DroneDefinition drone = (DroneDefinition)d;
                           return !drone.isValid();
                        }
                ),
                airportsTable.itemsProperty(),
                dronesTable.itemsProperty()
        ));

        //Fire a SimulationSettingsConfirmEvent when the user clicks the OK button
        //This event is observed in the MainController, where the overlay is hidden and the simulation is started.
        okButton.setOnAction(e -> {
            SimulationSettingsConfirmEvent confirmEvent = new SimulationSettingsConfirmEvent();
            confirmEvent.setSimulationSettings(buildSettings());
            fireEvent(confirmEvent);
        });
    }

    /****************/
    /*** AIRPORTS ***/
    /****************/

    private void setupAirportsTab(){
        setupAirportSizeSpinners();
        setupAirportsButtons();
        setupAirportsTable();
    }

    private void setupAirportSizeSpinners(){

    }

    private void setupAirportsButtons(){
        addAirportButton.setOnAction(e -> airportsTable.getItems().add(new AirportDefinition()));

        removeAirportButton.setOnAction(e -> airportsTable.getItems().removeAll(airportsTable.getSelectionModel().getSelectedItems()));
        removeAirportButton.disableProperty().bind(airportsTable.getSelectionModel().selectedIndexProperty().isEqualTo(-1));

        loadAirportsConfigFileButton.setVisible(false); //Not implemented yet
        loadAirportsConfigFileButton.setOnAction(e -> {

        });

        saveAirportsConfigFileButton.setVisible(false); //Not implemented yet
        saveAirportsConfigFileButton.setOnAction(e -> {

        });

        generateAirportsButton.setVisible(false); //Not implemented yet
        generateAirportsButton.setOnAction(e -> {

        });
    }

    private void setupAirportsTable(){
        //Make table editable
        airportsTable.setEditable(true);

        //Column 1 is X-coordinates and is editable
        TableColumn<AirportDefinition, Number> xColumn = new TableColumn<>();
        xColumn.setText("X");
        xColumn.setEditable(true);
        xColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        xColumn.setCellValueFactory(cd -> Bindings.createFloatBinding(() -> cd.getValue().getCenterX()));
        xColumn.setOnEditCommit((e) -> {
            AirportDefinition curAirport = (AirportDefinition)airportsTable.getItems().get(e.getTablePosition().getRow());
            curAirport.setCenterX(e.getNewValue().floatValue());
        });
        airportsTable.getColumns().add(xColumn);

        //Column 2 is Z-coordinates and is editable
        TableColumn<AirportDefinition, Number> zColumn = new TableColumn<>();
        zColumn.setText("Z");
        zColumn.setEditable(true);
        zColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        zColumn.setCellValueFactory(cd -> Bindings.createFloatBinding(() -> cd.getValue().getCenterZ()));
        zColumn.setOnEditCommit((e) -> {
            AirportDefinition curAirport = (AirportDefinition)airportsTable.getItems().get(e.getTablePosition().getRow());
            curAirport.setCenterZ(e.getNewValue().floatValue());
        });
        airportsTable.getColumns().add(zColumn);

        //Column 3 is centerToRunway0X and is editable
        TableColumn<AirportDefinition, Number> rotVectXColumn = new TableColumn<>();
        rotVectXColumn.setText("Center to runway 0 X");
        rotVectXColumn.setEditable(true);
        rotVectXColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        rotVectXColumn.setCellValueFactory(cd -> Bindings.createFloatBinding(() -> cd.getValue().getCenterToRunway0X()));
        rotVectXColumn.setOnEditCommit((e) -> {
            AirportDefinition curAirport = (AirportDefinition)airportsTable.getItems().get(e.getTablePosition().getRow());
            curAirport.setCenterToRunway0X(e.getNewValue().floatValue());
        });
        airportsTable.getColumns().add(rotVectXColumn);

        //Column 4 is centerToRunway0Z and is editable
        TableColumn<AirportDefinition, Number> rotVectZColumn = new TableColumn<>();
        rotVectZColumn.setText("Center to runway 0 Z");
        rotVectZColumn.setEditable(true);
        rotVectZColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        rotVectZColumn.setCellValueFactory(cd -> Bindings.createFloatBinding(() -> cd.getValue().getCenterToRunway0Z()));
        rotVectZColumn.setOnEditCommit((e) -> {
            AirportDefinition curAirport = (AirportDefinition)airportsTable.getItems().get(e.getTablePosition().getRow());
            curAirport.setCenterToRunway0Z(e.getNewValue().floatValue());
        });
        airportsTable.getColumns().add(rotVectZColumn);

        //Autosize columns
        autosizeTableColumns(airportsTable);
    }

    /**************/
    /*** DRONES ***/
    /**************/

    private void setupDronesTab(){
        setupDronesButtons();
        setupDronesTable();
    }

    private void setupDronesButtons(){
        addDroneButton.setOnAction(e -> {
            ObservableAutoPilotConfig config = new ObservableAutoPilotConfig(getDefaultAutopilotConfig());
            config.setProperty(ObservableAutoPilotConfig.DRONE_ID_KEY, "Drone " + dronesTable.getItems().size());
            dronesTable.getItems().add(new DroneDefinition(config));
        });

        removeDroneButton.setOnAction(e -> dronesTable.getItems().removeAll(dronesTable.getSelectionModel().getSelectedItems()));
        removeDroneButton.disableProperty().bind(dronesTable.getSelectionModel().selectedIndexProperty().isEqualTo(-1));

        loadDroneSetupFileButton.setVisible(false); //Not implemented yet
        loadDroneSetupFileButton.setOnAction(e -> {

        });

        saveDroneSetupFileButton.setVisible(false); //Not implemented yet
        saveDroneSetupFileButton.setOnAction(e -> {

        });

        loadDroneSetupDefaultsButton.setVisible(false); //Not implemented yet
        loadDroneSetupDefaultsButton.setOnAction(e -> {

        });
    }

    private AutopilotConfig getDefaultAutopilotConfig() {
        try(DataInputStream in = new DataInputStream(Resources.getResourceStream("/default_autopilot_config.bin"))){
            return AutopilotConfigReader.read(in);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void setupDronesTable(){
        //Make table editable
        dronesTable.setEditable(true);

        //Column 1 is drone ID and is editable
        TableColumn<DroneDefinition, String> idColumn = new TableColumn<>();
        idColumn.setText("ID");
        idColumn.setEditable(true);
        idColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        idColumn.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue().getConfig().getDroneID()));
        idColumn.setOnEditCommit((e) -> {
            DroneDefinition curDrone = (DroneDefinition)dronesTable.getItems().get(e.getTablePosition().getRow());
            curDrone.getConfig().setProperty(ObservableAutoPilotConfig.DRONE_ID_KEY, e.getNewValue());
        });
        dronesTable.getColumns().add(idColumn);

        //Column 2 is airport and is editable
        TableColumn<DroneDefinition, Number> airportColumn = new TableColumn<>();
        airportColumn.setText("Airport index");
        airportColumn.setEditable(true);
        airportColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter(){
            @Override
            public String toString(Number value) {
                if(value.intValue() == -1){
                    return "<No airport>";
                }
                return super.toString(value);
            }
        }));
        airportColumn.setCellValueFactory(cd -> Bindings.createIntegerBinding(
                () -> airportsTable.getItems().indexOf(cd.getValue().getAirport())
        ));
        airportColumn.setOnEditCommit((e) -> {
            DroneDefinition curDrone = (DroneDefinition)dronesTable.getItems().get(e.getTablePosition().getRow());

            int index = e.getNewValue().intValue();
            if(index < 0 || index >= airportsTable.getItems().size()){
                dronesTable.getItems().set(
                    e.getTablePosition().getRow(),
                    new DroneDefinition(curDrone.getConfig(), null, curDrone.getGate(), curDrone.getRunwayToFace())
                );
            }else{
                curDrone.setAirport((AirportDefinition) airportsTable.getItems().get(index));
            }
        });
        dronesTable.getColumns().add(airportColumn);

        //Column 3 is gate and is editable
        TableColumn<DroneDefinition, Number> gateColumn = new TableColumn<>();
        gateColumn.setText("Gate");
        gateColumn.setEditable(true);
        gateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        gateColumn.setCellValueFactory(cd -> Bindings.createIntegerBinding(() -> cd.getValue().getGate()));
        gateColumn.setOnEditCommit((e) -> {
            DroneDefinition curDrone = (DroneDefinition)dronesTable.getItems().get(e.getTablePosition().getRow());
            int gate = Integer.max(0, Integer.min(1, e.getNewValue().intValue()));
            dronesTable.getItems().set(
                e.getTablePosition().getRow(),
                new DroneDefinition(curDrone.getConfig(), curDrone.getAirport(), gate, curDrone.getRunwayToFace())
            );
        });
        dronesTable.getColumns().add(gateColumn);

        //Column 4 is runwayToFace and is editable
        TableColumn<DroneDefinition, Number> runwayColumn = new TableColumn<>();
        runwayColumn.setText("Runway to face");
        runwayColumn.setEditable(true);
        runwayColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        runwayColumn.setCellValueFactory(cd -> Bindings.createIntegerBinding(() -> cd.getValue().getRunwayToFace()));
        runwayColumn.setOnEditCommit((e) -> {
            DroneDefinition curDrone = (DroneDefinition)dronesTable.getItems().get(e.getTablePosition().getRow());
            int runway = Integer.max(0, Integer.min(1, e.getNewValue().intValue()));
            dronesTable.getItems().set(
                e.getTablePosition().getRow(),
                new DroneDefinition(curDrone.getConfig(), curDrone.getAirport(), curDrone.getGate(), runway)
            );
        });
        dronesTable.getColumns().add(runwayColumn);

        //Column 5 is autopilotconfig and is editable
        TableColumn<DroneDefinition, ObservableAutoPilotConfig> configColumn = new TableColumn<>();
        configColumn.setText("Autopilot config");
        configColumn.setEditable(true);
        configColumn.setCellFactory(cb -> {
            final TableCell<DroneDefinition, ObservableAutoPilotConfig> cell = new TableCell<DroneDefinition, ObservableAutoPilotConfig>() {
                final Button btn = new Button("Edit");

                @Override
                public void updateItem(ObservableAutoPilotConfig item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        btn.setOnAction(event -> {
                            Stage parentStage = (Stage)addDroneButton.getScene().getWindow();
                            ObservableAutoPilotConfig newConfig = ConfigEditorDialog.showDialog(parentStage, item);
                            if(newConfig != null){
                                int rowIndex = getTableRow().getIndex();
                                DroneDefinition oldDef = (DroneDefinition)dronesTable.getItems().get(rowIndex);
                                DroneDefinition newDef = new DroneDefinition(newConfig, oldDef.getAirport(), oldDef.getGate(), oldDef.getRunwayToFace());
                                dronesTable.getItems().set(rowIndex, newDef);
                            }
                        });
                        setGraphic(btn);
                        setText(null);
                    }
                }
            };

            return cell;
        });
        configColumn.setCellValueFactory(cd -> Bindings.createObjectBinding(() -> cd.getValue().getConfig()));
        configColumn.setOnEditCommit((e) -> {
            DroneDefinition curDrone = (DroneDefinition)dronesTable.getItems().get(e.getTablePosition().getRow());
            curDrone.setConfig(e.getNewValue());
        });
        dronesTable.getColumns().add(configColumn);

        //Autosize columns
        autosizeTableColumns(dronesTable);
    }

    private void autosizeTableColumns(TableView table){
        DoubleBinding widthPerColumn = table.widthProperty().divide(table.getColumns().size());
        for (Object col : table.getColumns()){
            TableColumn column = (TableColumn) col;
            column.prefWidthProperty().bind(widthPerColumn);
        }
    }

    /***************************/
    /*** SIMULATION SETTINGS ***/
    /***************************/

    private SimulationSettings buildSettings() {
        SimulationSettings settings = new SimulationSettings();

        //Airport size
        settings.setRunwayLength(runwayLengthSpinner.getValue());
        settings.setGateLength(gateLengthSpinner.getValue());

        //Airports
        AirportDefinition[] airports = new AirportDefinition[airportsTable.getItems().size()];
        for (int i = 0; i < airports.length; i++) {
            airports[i] = (AirportDefinition) airportsTable.getItems().get(i);
        }
        settings.setAirports(airports);

        //Drones
        DroneDefinition[] drones = new DroneDefinition[dronesTable.getItems().size()];
        for (int i = 0; i < drones.length; i++) {
            drones[i] = (DroneDefinition) dronesTable.getItems().get(i);
        }
        settings.setDrones(drones);

        return settings;
    }
}
