package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.autopilot.Autopilot;
import be.kuleuven.cs.robijn.common.exceptions.CrashException;
import be.kuleuven.cs.robijn.common.stopwatch.RealTimeStopwatch;
import be.kuleuven.cs.robijn.common.stopwatch.Stopwatch;
import be.kuleuven.cs.robijn.testbed.VirtualTestbed;
import interfaces.AutopilotConfig;
import interfaces.AutopilotInputs;
import interfaces.AutopilotModule;
import interfaces.AutopilotOutputs;

import java.util.List;
import java.util.TreeSet;
import org.apache.commons.math3.linear.*;

/**
 * This class combines the testbed and autopilot into one runnable simulation.
 */
public class SimulationDriver {
    private TestBed testBed;
    private AutopilotModule autoPilotModule;
    private boolean simulationFinished;
    private boolean simulationCrashed;
    private boolean simulationThrewException;

    private Stopwatch stopwatch;

    private boolean simulationStarted = false;

    //List of eventhandlers that are invoked when the simulation has updated.
    private TreeSet<UpdateEventHandler> updateEventHandlers = new TreeSet<>();

    public SimulationDriver(SimulationSettings settings, Stopwatch stopwatch){
        this.stopwatch = stopwatch;

        //testBed = new VirtualTestbed(boxes, config, initialVelocity);
        //autoPilotModule = new AutopilotModule();
    }

    /**
     * Runs the next iteration of the simulation.
     * If isSimulationPaused() is true, the simulation is not actually updated, but the update event handlers are still run.
     */
    public void runUpdate(){
        stopwatch.tick();

        if(!isSimulationPaused() && !simulationFinished && !simulationCrashed && !simulationThrewException){
        	try {
        	    //Reset renderer
                testBed.getRenderer().clearDebugObjects();

        		//Run the autopilotmodule
                /*if (simulationStarted == false ){
                    latestAutopilotOutputs = autoPilot.simulationStarted(config,latestAutopilotInputs);
                    simulationStarted = true;
                }
        		else {
                    latestAutopilotOutputs = autoPilot.timePassed(latestAutopilotInputs);
                }*/
        		//Run the testbed
                /*simulationFinished = testBed.update((float)stopwatch.getSecondsSinceStart(),
                        (float)stopwatch.getSecondsSinceLastUpdate(), latestAutopilotOutputs);

                latestAutopilotInputs = testBed.getInputs();*/
        	} catch (CrashException exc1){
                simulationCrashed = true;
                System.err.println("Plane crashed!");
        		exc1.printStackTrace();
        	} catch (IllegalArgumentException exc2) {
        		simulationCrashed = true;
        		System.err.println("Autopilot failed!");
        		exc2.printStackTrace();
        	} catch (NullPointerException exc3) {
        		simulationCrashed = true;
        		System.err.println("Autopilot failed!");
        		exc3.printStackTrace();
        	}
        }

        //Invokes the event handlers
        for (UpdateEventHandler eventHandler : updateEventHandlers) {
            //eventHandler.getFunction().accept(latestAutopilotInputs, latestAutopilotOutputs);
        }
    }

    public void setSimulationPaused(boolean simulationPaused) {
        stopwatch.setPaused(simulationPaused);
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

    public AutopilotModule getAutoPilotModule() {
        return autoPilotModule;
    }

    public void addOnUpdateEventHandler(UpdateEventHandler handler){
        updateEventHandlers.add(handler);
    }

    private void removeOnUpdateEventHandler(UpdateEventHandler handler){
        updateEventHandlers.remove(handler);
    }
}
