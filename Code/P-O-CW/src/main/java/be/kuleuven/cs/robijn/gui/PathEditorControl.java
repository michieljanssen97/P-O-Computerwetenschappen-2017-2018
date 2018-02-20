package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.BoxFileLoader;
import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.WorldGenerator;
import interfaces.Path;
import interfaces.PathReader;
import interfaces.PathWriter;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableFloatArray;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.io.*;
import java.util.stream.Stream;

public class PathEditorControl {
    public static Path showDialog(Stage parentStage){
        Stage dialog = new Stage();
        //Load layout
        Parent root = null;
        PathEditorControl controller;
        try {
            FXMLLoader loader = new FXMLLoader(Resources.getResourceURL("/layouts/path_editor.fxml"));
            root = loader.load();
            controller = loader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        //Setup and display window
        controller.dialog = dialog;
        Scene scene = new Scene(root);
        dialog.setTitle("Path editor");
        dialog.setScene(scene);
        dialog.initOwner(parentStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();

        return controller.okWasPressed ? controller.getPath() : null;
    }

    @FXML
    private AnchorPane root;

    private Stage dialog;

    @FXML
    private TableView pathTable;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button loadButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button okButton;

    public boolean okWasPressed;

    @FXML
    private void initialize(){
        setupTableControls();
        setupDialogButtons();
    }

    private void setupTableControls(){
        addButton.setOnMouseClicked(e -> {
            ObservableFloatArray row = FXCollections.observableFloatArray(0, 0, 0);
            pathTable.getItems().add(row);
        });
        deleteButton.setOnMouseClicked(e -> {
            pathTable.getItems().remove(pathTable.getSelectionModel().getSelectedIndex());
        });
        deleteButton.disableProperty().bind(pathTable.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        
        loadButton.setOnAction(e -> {
            Platform.runLater(() -> {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Select the path file");
                chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Path file", "bin"));
                File chosenFile = chooser.showOpenDialog(dialog.getOwner());
                if(chosenFile == null){
                    return;
                }

                try(DataInputStream in = new DataInputStream(new FileInputStream(chosenFile))){
                    loadPath(PathReader.read(in));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Failed to load file");
                    alert.setHeaderText("Could not load path file");
                    alert.setContentText("An error occured while loading the file! ("+ex.getMessage()+")");
                    alert.showAndWait();
                }
            });
        });

        saveButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select where to save the box setup file");
            chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Box setup text file", "txt"));
            File targetFile = chooser.showSaveDialog(this.saveButton.getScene().getWindow());
            if(targetFile == null){
                return;
            }

            try(DataOutputStream out = new DataOutputStream(new FileOutputStream(targetFile))){
                PathWriter.write(out, getPath());
            } catch (IOException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Failed to save file");
                alert.setHeaderText("Could not save path file");
                alert.setContentText("An error occured while saving the file! ("+ex.getMessage()+")");
                alert.showAndWait();
            }
        });

        setupConfigTable();
    }

    private void setupConfigTable(){
        //Make table editable
        pathTable.setEditable(true);

        //Column 1 is X
        TableColumn<ObservableFloatArray, Number> xColumn = new TableColumn<>();
        xColumn.setText("X");
        xColumn.setEditable(true);
        xColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        xColumn.setCellValueFactory(cd -> Bindings.floatValueAt(cd.getValue(), 0));
        xColumn.setOnEditCommit( (e) -> {
            ObservableFloatArray row = (ObservableFloatArray) pathTable.getItems().get(e.getTablePosition().getRow());
            row.set(0, e.getNewValue().floatValue());
        });
        pathTable.getColumns().add(xColumn);

        //Column 2 is Y
        TableColumn<ObservableFloatArray, Number> yColumn = new TableColumn<>();
        yColumn.setText("Y");
        yColumn.setEditable(true);
        yColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        yColumn.setCellValueFactory(cd -> Bindings.floatValueAt(cd.getValue(), 1));
        yColumn.setOnEditCommit( (e) -> {
            ObservableFloatArray row = (ObservableFloatArray) pathTable.getItems().get(e.getTablePosition().getRow());
            row.set(1, e.getNewValue().floatValue());
        });
        pathTable.getColumns().add(yColumn);

        //Column 3 is Z
        TableColumn<ObservableFloatArray, Number> zColumn = new TableColumn<>();
        zColumn.setText("Z");
        zColumn.setEditable(true);
        zColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        zColumn.setCellValueFactory(cd -> Bindings.floatValueAt(cd.getValue(), 2));
        zColumn.setOnEditCommit( (e) -> {
            ObservableFloatArray row = (ObservableFloatArray) pathTable.getItems().get(e.getTablePosition().getRow());
            row.set(2, e.getNewValue().floatValue());
        });
        pathTable.getColumns().add(zColumn);
    }

    private void loadPath(Path path){
        pathTable.getItems().clear();

        ObservableFloatArray[] rows = new ObservableFloatArray[path.getX().length];
        for(int i = 0; i < rows.length; i++){
            rows[i] = FXCollections.observableFloatArray(path.getX()[i], path.getY()[i], path.getZ()[i]);
        }
    }

    private void setupDialogButtons() {
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

    public Path getPath(){
        Stream<Float> xStream = pathTable.getItems().stream().map(c -> ((ObservableFloatArray)c).get(0));
        Stream<Float> yStream = pathTable.getItems().stream().map(c -> ((ObservableFloatArray)c).get(1));
        Stream<Float> zStream = pathTable.getItems().stream().map(c -> ((ObservableFloatArray)c).get(2));

        Float[] xObjects = xStream.toArray(Float[]::new);
        Float[] yObjects = yStream.toArray(Float[]::new);
        Float[] zObjects = zStream.toArray(Float[]::new);

        float[] x = new float[xObjects.length];
        for (int i = 0; i < xObjects.length; i++){
            x[i] = xObjects[i];
        }

        float[] y = new float[yObjects.length];
        for (int i = 0; i < yObjects.length; i++){
            y[i] = yObjects[i];
        }

        float[] z = new float[zObjects.length];
        for (int i = 0; i < zObjects.length; i++){
            z[i] = zObjects[i];
        }

        return new Path() {
            @Override
            public float[] getX() {
                return x;
            }

            @Override
            public float[] getY() {
                return y;
            }

            @Override
            public float[] getZ() {
                return z;
            }
        };
    }
}
