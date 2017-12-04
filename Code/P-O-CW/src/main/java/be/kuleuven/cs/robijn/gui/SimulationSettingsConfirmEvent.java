package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Box;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

import p_en_o_cw_2017.AutopilotConfig;

import java.util.List;

public class SimulationSettingsConfirmEvent extends Event {
    public static EventType<SimulationSettingsConfirmEvent> CONFIRM = new EventType<>(Event.ANY, "CONFIRM");

    private AutopilotConfig config;
    private List<Box> boxes;

    public SimulationSettingsConfirmEvent() {
        super(CONFIRM);
    }

    public SimulationSettingsConfirmEvent(Object source, EventTarget target) {
        super(source, target, CONFIRM);
    }

    public void setSettings(AutopilotConfig config) {
        this.config = config;
    }

    public AutopilotConfig getSettings() {
        return config;
    }

    public void setBoxes(List<Box> boxes) {
        this.boxes = boxes;
    }

    public List<Box> getBoxes() {
        return boxes;
    }
}
