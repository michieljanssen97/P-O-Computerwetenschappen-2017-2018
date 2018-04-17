package be.kuleuven.cs.robijn.gui;

import be.kuleuven.cs.robijn.common.Box;
import be.kuleuven.cs.robijn.common.SimulationSettings;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import interfaces.AutopilotConfig;

import java.util.List;

import be.kuleuven.cs.robijn.worldObjects.Box;

public class SimulationSettingsConfirmEvent extends Event {
    public static EventType<SimulationSettingsConfirmEvent> CONFIRM = new EventType<>(Event.ANY, "CONFIRM");

    private SimulationSettings simulationSettings;

    public SimulationSettingsConfirmEvent() {
        super(CONFIRM);
    }

    public SimulationSettingsConfirmEvent(Object source, EventTarget target) {
        super(source, target, CONFIRM);
    }

    public SimulationSettings getSimulationSettings() {
        return simulationSettings;
    }

    public void setSimulationSettings(SimulationSettings simulationSettings) {
        this.simulationSettings = simulationSettings;
    }
}
