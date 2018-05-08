package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.worldObjects.OrthographicCamera;
import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;

public class OrthoCameraZoomHandler implements EventHandler<ScrollEvent> {
    private static final int MIN_WHEEL_OFFSET = -20;
    private static final int MAX_WHEEL_OFFSET = 40;
    public static final int DEFAULT_ORTHO_CAM_WIDTH = 200;
    public static final int DEFAULT_ORTHO_CAM_HEIGHT = 50;
    private static final float ZOOM_MULTIPLIER_STEP = 0.9f;

    private OrthographicCamera camera;
    private int wheelOffset = 0;
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
        int newWheelOffset = wheelOffset + (int)(mouseDelta / 40);
        trySetWheelOffset(newWheelOffset);
    }

    public void zoomIn(){
        trySetWheelOffset(wheelOffset - 1);
    }

    public void zoomOut(){
        trySetWheelOffset(wheelOffset + 1);
    }

    private void trySetWheelOffset(int newWheelOffset){
        newWheelOffset = Math.max(newWheelOffset, MIN_WHEEL_OFFSET);
        newWheelOffset = Math.min(newWheelOffset, MAX_WHEEL_OFFSET);
        if(wheelOffset != newWheelOffset){
            wheelOffset = newWheelOffset;
            applyScale();
        }
    }

    private void applyScale(){
        // Calculate scaling factor
        scale = (float)Math.pow(ZOOM_MULTIPLIER_STEP, -wheelOffset);

        // Calculate new world space camera width and height
        camera.setWidth(DEFAULT_ORTHO_CAM_WIDTH * scale);
        camera.setHeight(DEFAULT_ORTHO_CAM_HEIGHT * scale);
    }

    public float getScale() {
        return scale;
    }
}
