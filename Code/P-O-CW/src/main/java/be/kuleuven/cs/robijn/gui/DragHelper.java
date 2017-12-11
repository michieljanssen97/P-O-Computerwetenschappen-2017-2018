package be.kuleuven.cs.robijn.gui;

import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

public class DragHelper {
    private final Node node;
    private double lastX, lastY;

    public DragHelper(Node node){
        if(node == null){
            throw new IllegalArgumentException();
        }
        this.node = node;
    }

    public void addOnDragEventHandler(Consumer<ExtendedDragEvent> handler){
        node.setOnMousePressed(e -> {
            lastX = e.getX();
            lastY = e.getY();
        });
        node.setOnMouseDragged(e -> {
            double deltaX = e.getX() - lastX;
            double deltaY = e.getY() - lastY;

            handler.accept(new ExtendedDragEvent(e, deltaX, deltaY));

            lastX = e.getX();
            lastY = e.getY();
        });
    }

    public static class ExtendedDragEvent {
        private final MouseEvent event;
        private final double deltaX;
        private final double deltaY;

        public ExtendedDragEvent(MouseEvent event, double deltaX, double deltaY) {
            this.event = event;
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }

        public MouseEvent getEvent() {
            return event;
        }

        public double getDeltaX() {
            return deltaX;
        }

        public double getDeltaY() {
            return deltaY;
        }
    }
}
