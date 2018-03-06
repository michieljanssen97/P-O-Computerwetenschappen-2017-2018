package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.autopilot.Autopilot;
import be.kuleuven.cs.robijn.common.stopwatch.RealTimeStopwatch;
import be.kuleuven.cs.robijn.common.stopwatch.Stopwatch;
import be.kuleuven.cs.robijn.testbed.VirtualTestbed;
import interfaces.AutopilotConfig;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

import java.util.List;
import java.util.TreeSet;
import org.apache.commons.math3.linear.*;

/**
 * This class combines the testbed and autopilot into one runnable simulation.
 */
public class SimulationDriver {
    private TestBed testBed;
    private Autopilot autoPilot;
    private boolean simulationFinished;
    private boolean simulationCrashed;
    private boolean simulationThrewException;
    private AutopilotInputs latestAutopilotInputs;
    private AutopilotOutputs latestAutopilotOutputs;

    private Stopwatch stopwatch;

    private boolean simulationStarted = false;
    private final AutopilotConfig config;

    //List of eventhandlers that are invoked when the simulation has updated.
    private TreeSet<UpdateEventHandler> updateEventHandlers = new TreeSet<>();

    public SimulationDriver(List<Box> boxes, AutopilotConfig config){
        this(boxes, config, new RealTimeStopwatch());
    }

    public SimulationDriver(List<Box> boxes, AutopilotConfig config, Stopwatch stopwatch){
        this.config = config;
        this.stopwatch = stopwatch;
    	RealVector initialVelocity = new ArrayRealVector(new double[] {0, 0, -56.0}, false);
        testBed = new VirtualTestbed(boxes, config, initialVelocity);
        autoPilot = new Autopilot();
        latestAutopilotInputs = testBed.getInputs();
    }

    /**
     * Runs the next iteration of the simulation.
     * If isSimulationPaused() is true, the simulation is not actually updated, but the update event handlers are still run.
     */
    public void runUpdate(){
        stopwatch.tick();

        if(!stopwatch.isPaused() && !simulationFinished && !simulationCrashed && !simulationThrewException){
        	try {
        	    //Reset renderer
                testBed.getRenderer().clearDebugObjects();

        		//Run the autopilot
                if (simulationStarted == false ){
                    latestAutopilotOutputs = autoPilot.simulationStarted(config,latestAutopilotInputs);
                    simulationStarted = true;
                }
        		else {
                    latestAutopilotOutputs = autoPilot.timePassed(latestAutopilotInputs);
                }
        		//Run the testbed
                simulationFinished = testBed.update((float)stopwatch.getSecondsSinceStart(),
                        (float)stopwatch.getSecondsSinceLastUpdate(), latestAutopilotOutputs);

                latestAutopilotInputs = testBed.getInputs();
        	} catch (IllegalStateException exc){
                simulationCrashed = true;
                System.err.println("Simulation failed!");
        		exc.printStackTrace();
        	} catch (Exception ex){
                simulationThrewException = true;
                System.err.println("A critical error occurred in the autopilot!");
            }
        }

        //Invokes the event handlers
        for (UpdateEventHandler eventHandler : updateEventHandlers) {
            eventHandler.getFunction().accept(latestAutopilotInputs, latestAutopilotOutputs);
        }
    }

    public void setSimulationPaused(boolean simulationPaused) {
        stopwatch.setPaused(simulationPaused);
    }

    public AutopilotConfig getConfig() {
        return config;
    }

    public boolean isSimulationPaused() {
        return stopwatch.isPaused();
    }

    public boolean hasSimulationFinished() {return simulationFinished;}

    public boolean hasSimulationCrashed(){return simulationCrashed;}

    public boolean hasSimulationThrownException(){return simulationThrewException;}

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
