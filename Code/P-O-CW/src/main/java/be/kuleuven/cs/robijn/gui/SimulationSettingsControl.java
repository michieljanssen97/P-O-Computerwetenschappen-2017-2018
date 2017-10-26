package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.converter.NumberStringConverter;

import p_en_o_cw_2017.AutopilotConfig;
import p_en_o_cw_2017.AutopilotConfigReader;
import p_en_o_cw_2017.AutopilotConfigWriter;

import java.io.*;

/**
 * Controller for the simulation settings overlay
 */
public class SimulationSettingsControl extends AnchorPane {
    @FXML
    private Button loadFileButton;

    @FXML
    private Button saveFileButton;

    @FXML
    private Button loadDefaultsButton;

    @FXML
    private Button okButton;

    @FXML
    private TableView propertiesTable;

    private ObjectProperty<ObservableAutoPilotConfig> configProperty = new SimpleObjectProperty<>(this, "config");

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
        //Load the initial, default values for the AutoPilotConfig
        loadDefaultConfig();

        //When the "load file" button is clicked, show a dialog and load the config file.
        loadFileButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select the simulation settings file");
            chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("AutopilotConfig file", "bin"));
            File chosenFile = chooser.showOpenDialog(this.getScene().getWindow());
            if(chosenFile == null){
                return;
            }

            try(DataInputStream in = new DataInputStream(new FileInputStream(chosenFile))){
                loadConfig(AutopilotConfigReader.read(in));
            } catch (IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Failed to load file");
                alert.setHeaderText("Could not load config file");
                alert.setContentText("An error occured while loading the file! ("+ex.getMessage()+")");
                alert.showAndWait();
            }
        });

        //When the "save file" button is clicked, show a dialog and save the config file.
        saveFileButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select where to save the simulation settings file");
            chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("AutopilotConfig file", "bin"));
            File targetFile = chooser.showSaveDialog(this.getScene().getWindow());
            if(targetFile == null){
                return;
            }

            try(DataOutputStream out = new DataOutputStream(new FileOutputStream(targetFile))){
                AutopilotConfigWriter.write(out, configProperty.get());
            } catch (IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Failed to save file");
                alert.setHeaderText("Could not save config file");
                alert.setContentText("An error occured while saving the file! ("+ex.getMessage()+")");
                alert.showAndWait();
            }
        });

        //When the "load default settings" button is clicked, load the default settings.
        loadDefaultsButton.setOnAction(e -> {
            loadDefaultConfig();
        });

        //User can only click OK if an AutoPilotConfig is loaded.
        okButton.disableProperty().bind(configProperty.isNull());
        //Fire a SimulationSettingsConfirmEvent when the user clicks the OK button
        //This event is observed in the MainController, where the overlay is hidden and the simulation is started.
        okButton.setOnAction(e -> {
            SimulationSettingsConfirmEvent confirmEvent = new SimulationSettingsConfirmEvent();
            confirmEvent.setSettings(configProperty.get());
            fireEvent(confirmEvent);
        });

        //Setup the simulation properties table
        setupTable();
    }

    /**
     * Creates an ObservableAutoPilotConfig version of the specified config and assigns it to configProperty.
     * The propertiesTable is refreshed to make sure the values displayed are correct and up-to-date.
     */
    private void loadConfig(AutopilotConfig config){
        configProperty.set(new ObservableAutoPilotConfig(config));
        propertiesTable.refresh();
    }

    /**
     * Loads the default_autopilot_config.bin config file from the application resources
     */
    private void loadDefaultConfig(){
        try(DataInputStream in = new DataInputStream(Resources.getResourceStream("/default_autopilot_config.bin"))){
            loadConfig(AutopilotConfigReader.read(in));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void setupTable(){
        //Make table editable
        propertiesTable.setEditable(true);

        //Load property names (hashmap keys) in table
        propertiesTable.setItems(FXCollections.observableArrayList(configProperty.get().getProperties().keySet()));

        //Column 1 is keys and is readonly
        TableColumn<String, String> keyColumn = new TableColumn<>();
        keyColumn.setText("Property");
        keyColumn.setEditable(false);
        keyColumn.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue()));
        propertiesTable.getColumns().add(keyColumn);

        //Column 2 is values and is editable
        TableColumn<String, Number> valueColumn = new TableColumn<>();
        valueColumn.setText("Value");
        valueColumn.setEditable(true);
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        valueColumn.setCellValueFactory(cd -> Bindings.valueAt(configProperty.get().getProperties(), cd.getValue()));
        valueColumn.setOnEditCommit( (e) ->
                configProperty.get().setProperty((String)propertiesTable.getItems().get(e.getTablePosition().getRow()), e.getNewValue())
        );
        propertiesTable.getColumns().add(valueColumn);
    }
}
