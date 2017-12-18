package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.WorldGenerator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.io.UncheckedIOException;

public class WorldGeneratorSettingsController {
    public static WorldGenerator.WorldGeneratorSettings showDialog(Stage parentStage){
        Stage dialog = new Stage();
        //Load layout
        Parent root = null;
        WorldGeneratorSettingsController controller;
        try {
            FXMLLoader loader = new FXMLLoader(Resources.getResourceURL("/layouts/world_generator_settings.fxml"));
            root = loader.load();
            controller = loader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        //Setup and display window
        Scene scene = new Scene(root);
        dialog.setTitle("World generator settings");
        dialog.setScene(scene);
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();

        return controller.okWasPressed ? controller.getSettings() : null;
    }

    @FXML
    private TextField numberOfBoxesTextfield;
    private Property<Integer> numberOfBoxesProperty = new SimpleObjectProperty<>(5);

    @FXML
    private CheckBox randomizeColorsCheckBox;
    private BooleanProperty randomizeColorsProperty = new SimpleBooleanProperty(true);

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    public boolean okWasPressed;

    @FXML
    private void initialize(){
        //force the field to be numeric only
        numberOfBoxesTextfield.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                numberOfBoxesTextfield.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        numberOfBoxesTextfield.textProperty().bindBidirectional(numberOfBoxesProperty, new IntegerStringConverter());

        randomizeColorsCheckBox.selectedProperty().bindBidirectional(randomizeColorsProperty);

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

    public WorldGenerator.WorldGeneratorSettings getSettings(){
        WorldGenerator.WorldGeneratorSettings settings = new WorldGenerator.WorldGeneratorSettings();
        settings.setBoxCount(Integer.parseInt(numberOfBoxesTextfield.getText()));
        settings.setRandomizeColors(randomizeColorsProperty.get());
        return settings;
    }
}
