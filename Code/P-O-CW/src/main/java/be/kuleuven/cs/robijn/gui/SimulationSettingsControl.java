package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.*;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import p_en_o_cw_2017.AutopilotConfig;
import p_en_o_cw_2017.AutopilotConfigReader;
import p_en_o_cw_2017.AutopilotConfigWriter;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controller for the simulation settings overlay
 */
public class SimulationSettingsControl extends AnchorPane {

    /// CONFIG

    @FXML
    private Button loadConfigFileButton;

    @FXML
    private Button saveConfigFileButton;

    @FXML
    private Button loadConfigDefaultsButton;

    @FXML
    private TableView propertiesTable;

    /// BOXES

    @FXML
    private Button loadBoxFileButton;

    @FXML
    private Button saveBoxFileButton;

    @FXML
    private Button loadBoxDefaultsButton;

    @FXML
    private Button addBoxButton;

    @FXML
    private Button removeBoxButton;

    @FXML
    private Button randomBoxesButton;

    @FXML
    private TableView boxTable;

    /// MISC

    @FXML
    private Button okButton;

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
        setupConfigControls();
        setupBoxControls();

        //User can only click OK if an AutoPilotConfig is loaded.
        okButton.disableProperty().bind(configProperty.isNull());
        //Fire a SimulationSettingsConfirmEvent when the user clicks the OK button
        //This event is observed in the MainController, where the overlay is hidden and the simulation is started.
        okButton.setOnAction(e -> {
            SimulationSettingsConfirmEvent confirmEvent = new SimulationSettingsConfirmEvent();
            confirmEvent.setSettings(configProperty.get());
            confirmEvent.setBoxes(getBoxes());
            fireEvent(confirmEvent);
        });

        //Setup the simulation properties table
        setupConfigTable();
        setupBoxTable();
    }

    private void setupConfigControls(){
        //Load the initial, default values for the AutoPilotConfig
        loadDefaultConfig();

        //When the "load file" button is clicked, show a dialog and load the config file.
        loadConfigFileButton.setOnAction(e -> {
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
        saveConfigFileButton.setOnAction(e -> {
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
        loadConfigDefaultsButton.setOnAction(e -> {
            loadDefaultConfig();
        });
    }

    private void setupBoxControls(){
        //Load the initial, default box setup
        loadDefaultBoxSetup();

        //When the "load file" button is clicked, show a dialog and load the config file.
        loadBoxFileButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select the box setup file");
            chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Box setup text file", "txt"));
            File chosenFile = chooser.showOpenDialog(this.getScene().getWindow());
            if(chosenFile == null){
                return;
            }

            try(DataInputStream in = new DataInputStream(new FileInputStream(chosenFile))){
                loadBoxSetup(BoxFileLoader.load(in));
            } catch (IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Failed to load file");
                alert.setHeaderText("Could not load box setup file");
                alert.setContentText("An error occured while loading the file! ("+ex.getMessage()+")");
                alert.showAndWait();
            }
        });

        //When the "save file" button is clicked, show a dialog and save the config file.
        saveBoxFileButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select where to save the box setup file");
            chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Box setup text file", "txt"));
            File targetFile = chooser.showSaveDialog(this.getScene().getWindow());
            if(targetFile == null){
                return;
            }

            try(DataOutputStream out = new DataOutputStream(new FileOutputStream(targetFile))){
                BoxFileLoader.write(getBoxes(), out);
            } catch (IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Failed to save file");
                alert.setHeaderText("Could not save box file");
                alert.setContentText("An error occured while saving the file! ("+ex.getMessage()+")");
                alert.showAndWait();
            }
        });

        addBoxButton.setOnAction(e -> {
            boxTable.getItems().add(generateNextBox());
        });

        removeBoxButton.setOnAction(e -> {
            boxTable.getItems().remove(boxTable.getSelectionModel().getSelectedIndex());
        });
        removeBoxButton.disableProperty().bind(boxTable.getSelectionModel().selectedItemProperty().isNull());

        //When the "load default settings" button is clicked, load the default settings.
        loadBoxDefaultsButton.setOnAction(e -> {
            loadDefaultBoxSetup();
        });

        randomBoxesButton.setOnAction(e -> {
            WorldGenerator.WorldGeneratorSettings settings = WorldGeneratorSettingsController.showDialog((Stage)this.addBoxButton.getScene().getWindow());
            if(settings != null){
                boxTable.getItems().addAll(WorldGenerator.generateBoxes(settings));
            }
        });
    }

    private Box generateNextBox(){
        Box newBox = new Box();
        newBox.setColor(ColorGenerator.sequentialEvenDistribution(boxTable.getItems().size()));
        return newBox;
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

    private void setupConfigTable(){
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

    private void setupBoxTable(){
        //Make table editable
        boxTable.setEditable(true);

        //Column 1 is box colors and is readonly
        TableColumn<Box, Color> colorColumn = new TableColumn<>();
        colorColumn.setText("Box color");
        colorColumn.setCellValueFactory(cd -> Bindings.createObjectBinding(() -> cd.getValue().getColor()));
        colorColumn.setCellFactory(x -> new TextFieldTableCell(new ColorStringConverter()){
            @Override
            public void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else {
                    // Format date.
                    Color color = (Color)item;
                    String colorHex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
                    setStyle("-fx-background-color: "+colorHex);
                }
            }
        });
        colorColumn.setOnEditCommit((e) -> {
            Box curBox = (Box)boxTable.getItems().get(e.getTablePosition().getRow());
            Color color = e.getNewValue();
            float[] hsv = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
            hsv[1] = hsv[2] = 1.0f;
            color = Color.getHSBColor(hsv[0], hsv[1], hsv[2]);
            curBox.setColor(color);
            boxTable.refresh();
        });
        boxTable.getColumns().add(colorColumn);

        //Column 2 is X-coordinates and is editable
        TableColumn<Box, Number> xColumn = new TableColumn<>();
        xColumn.setText("X");
        xColumn.setEditable(true);
        xColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        xColumn.setCellValueFactory(cd -> Bindings.createDoubleBinding(() -> cd.getValue().getRelativePosition().getEntry(0)));
        xColumn.setOnEditCommit((e) -> {
            Box curBox = (Box)boxTable.getItems().get(e.getTablePosition().getRow());
            double newX = e.getNewValue().doubleValue();
            double newY = curBox.getRelativePosition().getEntry(1);
            double newZ = curBox.getRelativePosition().getEntry(2);
            curBox.setRelativePosition(new ArrayRealVector(new double[]{newX, newY, newZ}, false));
        });
        boxTable.getColumns().add(xColumn);

        //Column 2 is Y-coordinates and is editable
        TableColumn<Box, Number> yColumn = new TableColumn<>();
        yColumn.setText("Y");
        yColumn.setEditable(true);
        yColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        yColumn.setCellValueFactory(cd -> Bindings.createDoubleBinding(() -> cd.getValue().getRelativePosition().getEntry(1)));
        yColumn.setOnEditCommit((e) -> {
            Box curBox = (Box)boxTable.getItems().get(e.getTablePosition().getRow());
            double newX = curBox.getRelativePosition().getEntry(0);
            double newY = e.getNewValue().doubleValue();
            double newZ = curBox.getRelativePosition().getEntry(2);
            curBox.setRelativePosition(new ArrayRealVector(new double[]{newX, newY, newZ}, false));
        });
        boxTable.getColumns().add(yColumn);

        //Column 2 is Z-coordinates and is editable
        TableColumn<Box, Number> zColumn = new TableColumn<>();
        zColumn.setText("Z");
        zColumn.setEditable(true);
        zColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        zColumn.setCellValueFactory(cd -> Bindings.createDoubleBinding(() -> cd.getValue().getRelativePosition().getEntry(2)));
        zColumn.setOnEditCommit((e) -> {
            Box curBox = (Box)boxTable.getItems().get(e.getTablePosition().getRow());
            double newX = curBox.getRelativePosition().getEntry(0);
            double newY = curBox.getRelativePosition().getEntry(1);
            double newZ = e.getNewValue().doubleValue();
            curBox.setRelativePosition(new ArrayRealVector(new double[]{newX, newY, newZ}, false));
        });
        boxTable.getColumns().add(zColumn);
    }

    private List<Box> getBoxes(){
        Stream<Box> boxStream = boxTable.getItems().stream().map(o -> (Box)o);
        return boxStream.collect(Collectors.toList());
    }

    /**
     * Loads the default_autopilot_config.bin config file from the application resources
     */
    private void loadDefaultBoxSetup(){
        try(DataInputStream in = new DataInputStream(Resources.getResourceStream("/default_boxes.txt"))){
            loadBoxSetup(BoxFileLoader.load(in));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void loadBoxSetup(List<Box> boxes){
        for(int i = 0; i < boxes.size(); i++){
            boxes.get(i).setColor(ColorGenerator.sequentialEvenDistribution(i));
        }

        boxTable.getItems().clear();
        boxTable.getItems().addAll(boxes);
    }
}
