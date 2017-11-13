package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.autopilot.Autopilot;
import be.kuleuven.cs.robijn.testbed.VirtualTestbed;
import interfaces.AutopilotConfig;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;
import java.util.TreeSet;
import org.apache.commons.math3.linear.*;

/**
 * This class combines the testbed and autopilot into one runnable simulation.
 */
public class SimulationDriver {
    private TestBed testBed;
    private Autopilot autoPilot;
    private boolean simulationPaused;
    private boolean simulationFinished;
    private boolean simulationCrashed;
    private final AutopilotConfig config;
    private AutopilotInputs latestAutopilotInputs;
    private AutopilotOutputs latestAutopilotOutputs;

    private long simulationStart = -1; //timestamp of when the simulation started
    private long totalTimeSpentPaused = 0; //total amount of time, between start and last update, that was spent paused (in ms)
    private long lastUpdate = -1; //timestamp of last update
    private long timeSpentPausedSinceLastUpdate = 0; //total amount of time, between last update and now, that was spent paused (in ms)

    //List of eventhandlers that are invoked when the simulation has updated.
    private TreeSet<UpdateEventHandler> updateEventHandlers = new TreeSet<>();

    public SimulationDriver(List<Box> boxes, AutopilotConfig config){
        this.config = config;
    	RealVector initialVelocity = new ArrayRealVector(new double[] {0, 0, -6.667}, false);
        testBed = new VirtualTestbed(boxes, config, initialVelocity);
        autoPilot = new Autopilot(config, initialVelocity);
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
        for (UpdateEventHandler eventHandler : updateEventHandlers) {
            eventHandler.getFunction().accept(latestAutopilotInputs, latestAutopilotOutputs);
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

    public AutopilotConfig getConfig() {
        return config;
    }

    public boolean isSimulationPaused() {
        return simulationPaused;
    }

    public boolean hasSimulationFinished() {return simulationFinished;}

    public boolean hasSimulationCrashed(){return simulationCrashed;}

    public TestBed getTestBed(){
        return testBed;
    }

    public Autopilot getAutoPilot(){
        return autoPilot;
    }

    public void addOnUpdateEventHandler(UpdateEventHandler handler){
        updateEventHandlers.add(handler);
    }

    private void removeOnUpdateEventHandler(UpdateEventHandler handler){
        updateEventHandlers.remove(handler);
    }
}
