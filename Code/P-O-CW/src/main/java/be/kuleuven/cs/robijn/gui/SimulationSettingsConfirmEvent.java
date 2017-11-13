package be.kuleuven.cs.robijn.gui;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

import interfaces.AutopilotConfig;

public class SimulationSettingsConfirmEvent extends Event {
    public static EventType<SimulationSettingsConfirmEvent> CONFIRM = new EventType<>(Event.ANY, "CONFIRM");

    private AutopilotConfig config;

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
}
