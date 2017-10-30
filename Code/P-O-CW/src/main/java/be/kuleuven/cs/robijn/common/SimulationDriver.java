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
    private boolean simulationFinished;
    private boolean simulationCrashed;
    private AutopilotInputs latestAutopilotInputs;
    private AutopilotOutputs latestAutopilotOutputs;

    private long simulationStart = -1; //timestamp of when the simulation started
    private long totalTimeSpentPaused = 0; //total amount of time, between start and last update, that was spent paused (in ms)
    private long lastUpdate = -1; //timestamp of last update
    private long timeSpentPausedSinceLastUpdate = 0; //total amount of time, between last update and now, that was spent paused (in ms)

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
        if(simulationStart == -1 || lastUpdate == -1){
            lastUpdate = simulationStart = System.currentTimeMillis();
        }

        if(!simulationPaused && !simulationFinished && !simulationCrashed){
        	try {
        		//Run the autopilot
        		latestAutopilotOutputs = autoPilot.update(latestAutopilotInputs);
        		//Run the testbed
                long now = System.currentTimeMillis();
                float secondsSinceStart = ((float)((now - simulationStart) - totalTimeSpentPaused)/1000f);
                float secondsSinceLastUpdate = ((float)((now - lastUpdate) - timeSpentPausedSinceLastUpdate)/1000f);
                simulationFinished = testBed.update(secondsSinceStart, secondsSinceLastUpdate, latestAutopilotOutputs);
                timeSpentPausedSinceLastUpdate = 0;
                lastUpdate = now;
                latestAutopilotInputs = testBed.getInputs();
        	} catch (IllegalStateException exc){
                simulationCrashed = true;
                System.err.println("Simulation failed!");
        		exc.printStackTrace();
        	}
        }

        //Invokes the event handlers
        for (BiConsumer<AutopilotInputs, AutopilotOutputs> eventHandler : updateEventHandlers) {
            eventHandler.accept(latestAutopilotInputs, latestAutopilotOutputs);
        }
    }

    long pauseTime = -1; //timestamp of when the simulation was paused
    public void setSimulationPaused(boolean simulationPaused) {
        if(this.simulationPaused && !simulationPaused){
            long timePaused = System.currentTimeMillis() - pauseTime;
            totalTimeSpentPaused += timePaused;
            timeSpentPausedSinceLastUpdate += timePaused;
            pauseTime = -1;
        }else if(!this.simulationPaused && simulationPaused){
            pauseTime = System.currentTimeMillis();
        }
        this.simulationPaused = simulationPaused;
    }

    public boolean isSimulationPaused() {
        return simulationPaused;
    }

    public boolean hasSimulationFinished() {return simulationFinished;}

    public boolean hasSimulationCrashed(){return simulationCrashed;}

    public TestBed getTestBed(){
        return testBed;
    }

    public AutoPilot getAutoPilot(){
        return autoPilot;
    }

    public void addOnUpdateEventHandler(BiConsumer<AutopilotInputs, AutopilotOutputs> eventHandler){
        updateEventHandlers.add(eventHandler);
    }

    private void removeOnUpdateEventHandler(BiConsumer<AutopilotInputs, AutopilotOutputs> eventHandler){
        updateEventHandlers.remove(eventHandler);
    }
}
