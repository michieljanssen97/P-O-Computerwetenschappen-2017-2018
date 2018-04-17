package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;
import interfaces.AutopilotConfig;
import interfaces.AutopilotConfigReader;
import interfaces.AutopilotConfigWriter;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.io.*;

public class ConfigEditorDialog {
    public static ObservableAutoPilotConfig showDialog(Stage parentStage, ObservableAutoPilotConfig configToEdit){
        Stage dialog = new Stage();
        //Load layout
        Parent root = null;
        ConfigEditorDialog controller;
        try {
            FXMLLoader loader = new FXMLLoader(Resources.getResourceURL("/layouts/config_editor_dialog.fxml"));
            root = loader.load();
            controller = loader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if(configToEdit != null){
            controller.loadConfig(configToEdit);
        }

        //Setup and display window
        Scene scene = new Scene(root);
        dialog.setTitle("AutopilotConfig editor");
        dialog.setScene(scene);
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();

        return controller.okWasPressed ? controller.configProperty.get() : null;
    }

    /// CONFIG

    @FXML
    private Button loadConfigFileButton;

    @FXML
    private Button saveConfigFileButton;

    @FXML
    private Button loadConfigDefaultsButton;

    @FXML
    private TableView stringPropertiesTable;

    @FXML
    private TableView numberPropertiesTable;

    private ObjectProperty<ObservableAutoPilotConfig> configProperty = new SimpleObjectProperty<>(this, "config");

    /// DIALOG

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    public boolean okWasPressed;

    @FXML
    private void initialize(){
        setupConfigControls();
        setupConfigTable();
        setupDialogButtons();
    }

    private void setupConfigControls(){
        //Load the initial, default values for the AutoPilotConfig
        loadDefaultConfig();

        //When the "load file" button is clicked, show a dialog and load the config file.
        loadConfigFileButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select the simulation settings file");
            chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("AutopilotConfig file", "bin"));
            File chosenFile = chooser.showOpenDialog(okButton.getScene().getWindow());
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
        saveConfigFileButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select where to save the simulation settings file");
            chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("AutopilotConfig file", "bin"));
            File targetFile = chooser.showSaveDialog(okButton.getScene().getWindow());
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
        loadConfigDefaultsButton.setOnAction(e -> {
            loadDefaultConfig();
        });
    }

    private void setupConfigTable(){
        //Make table editable
        stringPropertiesTable.setEditable(true);
        numberPropertiesTable.setEditable(true);

        //Load property names (hashmap keys) in table
        stringPropertiesTable.setItems(FXCollections.observableArrayList(configProperty.get().getStringProperties().keySet()));
        numberPropertiesTable.setItems(FXCollections.observableArrayList(configProperty.get().getNumberProperties().keySet()));

        //Column 1 is keys and is readonly
        TableColumn<String, String> keyColumn = new TableColumn<>();
        keyColumn.setText("Property");
        keyColumn.setEditable(false);
        keyColumn.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue()));
        stringPropertiesTable.getColumns().add(keyColumn);

        //Column 1 is keys and is readonly
        TableColumn<String, String> keyColumn2 = new TableColumn<>();
        keyColumn2.setText("Property");
        keyColumn2.setEditable(false);
        keyColumn2.setCellValueFactory(cd -> Bindings.createStringBinding(() -> cd.getValue()));
        numberPropertiesTable.getColumns().add(keyColumn2);

        //Column 2 is values and is editable
        TableColumn<String, String> valueColumn = new TableColumn<>();
        valueColumn.setText("Value");
        valueColumn.setEditable(true);
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        valueColumn.setCellValueFactory(cd -> Bindings.valueAt(configProperty.get().getStringProperties(), cd.getValue()));
        valueColumn.setOnEditCommit( (e) ->
                configProperty.get().setProperty((String)stringPropertiesTable.getItems().get(e.getTablePosition().getRow()), e.getNewValue())
        );
        stringPropertiesTable.getColumns().add(valueColumn);

        //Column 2 is values and is editable
        TableColumn<String, Number> valueColumn2 = new TableColumn<>();
        valueColumn2.setText("Value");
        valueColumn2.setEditable(true);
        valueColumn2.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        valueColumn2.setCellValueFactory(cd -> Bindings.valueAt(configProperty.get().getNumberProperties(), cd.getValue()));
        valueColumn2.setOnEditCommit( (e) ->
                configProperty.get().setProperty((String)numberPropertiesTable.getItems().get(e.getTablePosition().getRow()), e.getNewValue())
        );
        numberPropertiesTable.getColumns().add(valueColumn2);
    }

    /**
     * Creates an ObservableAutoPilotConfig version of the specified config and assigns it to configProperty.
     * The propertiesTable is refreshed to make sure the values displayed are correct and up-to-date.
     */
    private void loadConfig(AutopilotConfig config){
        configProperty.set(new ObservableAutoPilotConfig(config));
        stringPropertiesTable.refresh();
        numberPropertiesTable.refresh();
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

    private void setupDialogButtons(){
        okButton.setOnAction(e -> {
            okWasPressed = true;
            Stage stage = (Stage)okButton.getScene().getWindow();
            stage.close();
        });

        cancelButton.setOnAction(e -> {
            Stage stage = (Stage)cancelButton.getScene().getWindow();
            stage.close();
        });
    }
}
