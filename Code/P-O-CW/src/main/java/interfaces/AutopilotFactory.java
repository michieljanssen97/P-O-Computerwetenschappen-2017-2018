package interfaces;

public class AutopilotFactory {
    public static Autopilot createAutopilot(){
        return new be.kuleuven.cs.robijn.autopilot.Autopilot();
    }
}
