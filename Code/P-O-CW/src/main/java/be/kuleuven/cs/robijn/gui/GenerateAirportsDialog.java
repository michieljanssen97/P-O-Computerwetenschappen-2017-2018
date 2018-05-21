package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.SimulationSettings.AirportDefinition;
import be.kuleuven.cs.robijn.common.airports.generator.AirportGenerator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.*;

@SuppressWarnings("restriction")
public class GenerateAirportsDialog {
    public static AirportDefinition[] showDialog(Stage parentStage){
        Stage dialog = new Stage();
        //Load layout
        Parent root = null;
        GenerateAirportsDialog controller;
        try {
            FXMLLoader loader = new FXMLLoader(Resources.getResourceURL("/layouts/generate_airports_dialog.fxml"));
            root = loader.load();
            controller = loader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        //Setup and display window
        Scene scene = new Scene(root);
        dialog.setTitle("Generate airports");
        dialog.setScene(scene);
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();

        return controller.okWasPressed ? controller.airports : null;
    }

    @FXML
    public Spinner<Integer> airportCountSpinner;

    @FXML
    public ComboBox<AirportGenerator> patternComboBox;

    @FXML
    public CheckBox randomRotationCheckBox;

    @FXML
    public Spinner<Double> xOffsetSpinner;

    @FXML
    public Spinner<Double> zOffsetSpinner;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    public boolean okWasPressed;

    public AirportDefinition[] airports;

    @FXML
    private void initialize(){
        setupPatternComboBox();
        setupSpinners();
        setupDialogButtons();
    }

    private void setupPatternComboBox(){
        patternComboBox.getItems().addAll(AirportGenerator.GENERATORS);

        //Display the pattern name in the combobox
        Callback<ListView<AirportGenerator>, ListCell<AirportGenerator>> cellFactory = data -> new ListCell<AirportGenerator>(){
            public void updateItem(AirportGenerator item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item == null){
                    setText("");
                }else {
                    setText(item.getPatternName());
                }
            }
        };
        patternComboBox.setCellFactory(cellFactory);
        patternComboBox.getSelectionModel().select(AirportGenerator.GENERATORS[0]);
        patternComboBox.setButtonCell(cellFactory.call(null));
    }

    private void setupSpinners(){
        JavaFXUtilities.enableApplySpinnerValueOnFocusLost(airportCountSpinner);
        JavaFXUtilities.enableApplySpinnerValueOnFocusLost(xOffsetSpinner);
        JavaFXUtilities.enableApplySpinnerValueOnFocusLost(zOffsetSpinner);
    }

    private void setupDialogButtons(){
        okButton.setOnAction(e -> {
            AirportGenerator.Settings settings = new AirportGenerator.Settings();
            settings.setAirportCount(airportCountSpinner.getValue());
            settings.setRandomizeRotationEnabled(randomRotationCheckBox.isSelected());
            settings.setXDistBetweenAirports(xOffsetSpinner.getValue().floatValue());
            settings.setZDistBetweenAirports(zOffsetSpinner.getValue().floatValue());
            airports = patternComboBox.getSelectionModel().getSelectedItem().generate(settings);

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
