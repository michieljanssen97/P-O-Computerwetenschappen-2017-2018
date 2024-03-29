package interfaces;

public interface Autopilot {
    AutopilotOutputs simulationStarted(AutopilotConfig config, AutopilotInputs inputs);
    AutopilotOutputs timePassed(AutopilotInputs inputs);
    void setPath(Path path);
    void simulationEnded();
}
