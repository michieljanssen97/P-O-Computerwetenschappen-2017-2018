package be.kuleuven.cs.robijn.gui;

import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;

import java.util.function.Supplier;

/**
 * GUI component that allows the user to split the panes both horizontally and vertically.
 */
public class SplittablePane extends SplitPane {
    private Supplier<Parent> viewSupplier;

    /**
     * Splits the pane at the specified child.
     * A new child will be added either to the right or below the specified child. (depending on orientation)
     * @param host the child view that is split in two.
     * @param orientation the orientation of how to stack the two new views.
     *                    Horizontal for a left-right split, vertical for a top-bottom split.
     */
    public void split(SplittablePaneContentHost host, Orientation orientation){
        if(this.getOrientation() == orientation || this.getChildren().size() <= 1){
            //This pane either has no splits yet, or the splits are in the same orientation as the one requested.
            this.setOrientation(orientation);
            SplittablePaneContentHost newHost = getContentHostWithView();

            //Add new pane after the one where the button was clicked
            int index = this.getItems().indexOf(host)+1;
            this.getItems().add(index, newHost);
        }else{
            //Remove element at i
            int index = this.getItems().indexOf(host);
            this.getItems().remove(index);

            //Add new childPane after the host where the button was clicked
            SplittablePane childPane = new SplittablePane();
            childPane.setOrientation(orientation);
            this.getItems().add(index, childPane);

            //add host where the button was clicked to the new splittablepane
            //the new view will be created by the child pane itself on initialize
            childPane.setViewSupplier(viewSupplier);
            childPane.getItems().add(host);
            childPane.initialize();
        }
    }

    /**
     * Creates a new SplittablePaneContentHost with a new view supplied by viewSupplier
     */
    private SplittablePaneContentHost getContentHostWithView(){
        SplittablePaneContentHost host = new SplittablePaneContentHost();
        host.setContent(viewSupplier.get());
        host.setOnSplitHorizontally(e -> {
            split(host, Orientation.HORIZONTAL);
        });
        host.setOnSplitVertically(e -> {
            split(host, Orientation.VERTICAL);
        });
        return host;
    }

    public void initialize(){
        this.getItems().add(getContentHostWithView());
    }

    /**
     * Sets the factory that creates the GUI controls that will be shown inside the SplittablePaneContentHosts in this control.
     * @param viewSupplier
     */
    public void setViewSupplier(Supplier<Parent> viewSupplier){
        this.viewSupplier = viewSupplier;
    }

    /**
     * Returns the child GUI controls factory
     * @return
     */
    public Supplier<Parent> getViewSupplier() {
        return viewSupplier;
    }
}
