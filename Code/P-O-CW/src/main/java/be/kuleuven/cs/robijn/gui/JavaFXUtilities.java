package be.kuleuven.cs.robijn.gui;

import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;

@SuppressWarnings("restriction")
public class JavaFXUtilities {
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void enableApplySpinnerValueOnFocusLost(Spinner spinner){
        // hook in a formatter with the same properties as the factory
        TextFormatter formatter = new TextFormatter(spinner.getValueFactory().getConverter(), spinner.getValueFactory().getValue());
        spinner.getEditor().setTextFormatter(formatter);
        // bidi-bind the values
        spinner.getValueFactory().valueProperty().bindBidirectional(formatter.valueProperty());
    }
}
