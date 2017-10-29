package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.autopilot.Autopilot;
import be.kuleuven.cs.robijn.testbed.VirtualTestbed;
import p_en_o_cw_2017.AutopilotConfig;
import p_en_o_cw_2017.AutopilotInputs;
import p_en_o_cw_2017.AutopilotOutputs;
import java.util.ArrayList;
import java.util.function.BiConsumer;

/**
 * This class combines the testbed and autopilot into one runnable simulation.
 */
public class SimulationDriver {
    private TestBed testBed;
    private AutoPilot autoPilot;
    private boolean simulationPaused;
    private AutopilotInputs latestAutopilotInputs;
    private AutopilotOutputs latestAutopilotOutputs;

    //List of eventhandlers that are invoked when the simulation has updated.
    private ArrayList<BiConsumer<AutopilotInputs, AutopilotOutputs>> updateEventHandlers = new ArrayList<>();

    public SimulationDriver(AutopilotConfig config){
        testBed = new VirtualTestbed(config);
        autoPilot = new Autopilot(config);
        latestAutopilotInputs = testBed.getInputs();
    }

    /**
     * Runs the next iteration of the simulation.
     * If isSimulationPaused() is true, the simulation is not actually updated, but the update event handlers are still run.
     */
    public void runUpdate(){
        if(!simulationPaused){
            //Run the autopilot
            latestAutopilotOutputs = autoPilot.update(latestAutopilotInputs);
            //Run the testbed
            try {
            	testBed.update(latestAutopilotOutputs);
            } catch (IllegalStateException exc) {
            	simulationPaused = true;
            }
            latestAutopilotInputs = testBed.getInputs();
        }

        //Invokes the event handlers
        for (BiConsumer<AutopilotInputs, AutopilotOutputs> eventHandler : updateEventHandlers) {
            eventHandler.accept(latestAutopilotInputs, latestAutopilotOutputs);
        }
    }

    public TestBed getTestBed(){
        return testBed;
    }

    public AutoPilot getAutoPilot(){
        return autoPilot;
    }

    public void setSimulationPaused(boolean simulationPaused) {
        this.simulationPaused = simulationPaused;
    }

    public boolean isSimulationPaused() {
        return simulationPaused;
    }

    public void addOnUpdateEventHandler(BiConsumer<AutopilotInputs, AutopilotOutputs> eventHandler){
        updateEventHandlers.add(eventHandler);
    }

    private void removeOnUpdateEventHandler(BiConsumer<AutopilotInputs, AutopilotOutputs> eventHandler){
        updateEventHandlers.remove(eventHandler);
    }
}
