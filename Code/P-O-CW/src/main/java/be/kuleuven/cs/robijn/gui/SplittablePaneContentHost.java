package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Resources;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * This GUI component wraps another GUI control and adds two buttons for splitting the panes of a SplittablePane
 */
public class SplittablePaneContentHost extends AnchorPane {
    @FXML
    private AnchorPane contentHost;

    @FXML
    private Button splitHorizontallyButton;

    @FXML
    private Button splitVerticallyButton;

    private ObjectProperty<EventHandler<ActionEvent>> splitHorizontallyHandlerProperty
            = new SimpleObjectProperty<>(this, "onSplitHorizontally", null);
    private ObjectProperty<EventHandler<ActionEvent>> splitVerticallyHandlerProperty
            = new SimpleObjectProperty<>(this, "onSplitVertically", null);

    public SplittablePaneContentHost(){
        //Loads the layout associated with this control.
        FXMLLoader fxmlLoader = new FXMLLoader(Resources.getResourceURL("/layouts/splittable_pane_content_host.fxml"));
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
        splitHorizontallyButton.onActionProperty().bind(splitHorizontallyHandlerProperty);
        splitVerticallyButton.onActionProperty().bind(splitVerticallyHandlerProperty);
    }

    /**
     * Sets the GUI control that is shown inside this control.
     */
    public void setContent(Parent control){
        contentHost.getChildren().clear();
        contentHost.getChildren().add(control);

        AnchorPane.setLeftAnchor(control, 0d);
        AnchorPane.setTopAnchor(control, 0d);
        AnchorPane.setRightAnchor(control, 0d);
        AnchorPane.setBottomAnchor(control, 0d);
    }

    /**
     * Sets the eventhandler that will be invoked when the "split horizontally" button is pressed.
     */
    public void setOnSplitHorizontally(EventHandler<ActionEvent> splitHorizontallyHandlerProperty) {
        this.splitHorizontallyHandlerProperty.set(splitHorizontallyHandlerProperty);
    }

    /**
     * Sets the eventhandler that will be invoked when the "split vertically" button is pressed.
     */
    public void setOnSplitVertically(EventHandler<ActionEvent> splitVerticallyHandlerProperty) {
        this.splitVerticallyHandlerProperty.set(splitVerticallyHandlerProperty);
    }
}
