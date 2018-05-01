package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.worldObjects.OrthographicCamera;
import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;

public class OrthoCameraZoomHandler implements EventHandler<ScrollEvent> {
    private static final int MIN_WHEEL_OFFSET = 1;
    private static final int MAX_WHEEL_OFFSET = 60;
    private static final int DEFAULT_ORTHO_CAM_WIDTH = 200;
    private static final int DEFAULT_ORTHO_CAM_HEIGHT = 50;

    private OrthographicCamera camera;
    private int wheelOffset = 6;
    private float scale = 1.0f;

    public OrthoCameraZoomHandler(OrthographicCamera camera){
        if(camera == null){
            throw new IllegalArgumentException("camera cannot be null");
        }

        this.camera = camera;
    }

    @Override
    public void handle(ScrollEvent event) {
        double mouseDelta = -event.getDeltaY();

        // Don't zoom in/out if at the zoom is already at max/min value
        if((wheelOffset == MIN_WHEEL_OFFSET && mouseDelta < 0) ||
                (wheelOffset == MAX_WHEEL_OFFSET && mouseDelta > 0)){
            return;
        }

        // Calculate new scroll wheel offset and clamp value between max and min value
        wheelOffset += mouseDelta / 40;
        wheelOffset = Math.max(wheelOffset, MIN_WHEEL_OFFSET);
        wheelOffset = Math.min(wheelOffset, MAX_WHEEL_OFFSET);

        // Calculate logaritmic scaling factor
        scale = (float)Math.log(1 + (wheelOffset / 10d)) * 2f;

        // Calculate new world space camera width and height
        camera.setWidth(DEFAULT_ORTHO_CAM_WIDTH * scale);
        camera.setHeight(DEFAULT_ORTHO_CAM_HEIGHT * scale);
    }

    public float getScale() {
        return scale;
    }
}
