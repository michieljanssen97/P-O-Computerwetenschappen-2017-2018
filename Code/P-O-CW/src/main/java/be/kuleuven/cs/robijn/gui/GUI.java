package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Main GUI class. Use this to start the GUI.
 */
public class GUI extends Application {
    private static MainController controller;

    @Override
    public void start(Stage stage) {
        //Load layout
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(Resources.getResourceURL("/layouts/main_view.fxml"));
            root = loader.load();
            controller = loader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        //Setup and display window
        Scene scene = new Scene(root);
        stage.setTitle("P&O Robijn Testbed");
        stage.setScene(scene);
        stage.setWidth(1600);
        stage.setHeight(900);
        stage.show();
    }

    public static void println(String line){
        controller.addLineToOutput(line);
    }
}
