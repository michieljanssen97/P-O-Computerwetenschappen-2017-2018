package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.autopilot.AutopilotModuleAdapter;
import be.kuleuven.cs.robijn.common.airports.AirportPackage;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.common.exceptions.CrashException;
import be.kuleuven.cs.robijn.common.stopwatch.ConstantIntervalStopwatch;
import be.kuleuven.cs.robijn.common.stopwatch.RealTimeStopwatch;
import be.kuleuven.cs.robijn.common.stopwatch.Stopwatch;
import be.kuleuven.cs.robijn.testbed.VirtualTestbed;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * This class combines the testbed and autopilot into one runnable simulation.
 */
public class SimulationDriver {
    private final SimulationSettings settings;
    private final TestBed testBed;
    private final AutopilotModuleAdapter autoPilotModule;
    private final Stopwatch stopwatch;

    private boolean simulationFinished;
    private boolean simulationCrashed;
    private boolean outOfControl;
    private boolean simulationThrewException;

    private AutopilotInputs[] latestAutopilotInputs;
    private AutopilotOutputs[] latestAutopilotOutputs;

    //List of eventhandlers that are invoked when the simulation has updated.
    private final TreeSet<UpdateEventHandler> updateEventHandlers = new TreeSet<>();

    private Queue<AirportPackage> newPackages = new LinkedList<>();
    
    public SimulationDriver(SimulationSettings settings){
        this(settings, new ConstantIntervalStopwatch(0.03d));
    }
    
    public SimulationDriver(SimulationSettings settings, Stopwatch stopwatch){
        this.settings = settings;
        this.stopwatch = stopwatch;

        latestAutopilotInputs = new AutopilotInputs[settings.getDrones().length];
        latestAutopilotOutputs = new AutopilotOutputs[settings.getDrones().length];
        testBed = new VirtualTestbed(settings);
        autoPilotModule = new AutopilotModuleAdapter();
        initializeAutopilotModule(settings);
        initializeAutopilotInputs();
        stopwatch.reset();
    }

    private void initializeAutopilotModule(SimulationSettings settings){
        //Define airport parameters
        autoPilotModule.defineAirportParams(settings.getRunwayLength(), settings.getGateLength());

        //Define airports
        for(SimulationSettings.AirportDefinition airport : settings.getAirports()){
            autoPilotModule.defineAirport(
                airport.getCenterX(), airport.getCenterZ(),
                airport.getCenterToRunway0X(), airport.getCenterToRunway0Z()
            );
        }

        //Define drones
        List<SimulationSettings.AirportDefinition> airportsList = Arrays.asList(settings.getAirports());
        for(SimulationSettings.DroneDefinition drone : settings.getDrones()){
            int airportIndex = airportsList.indexOf(drone.getAirport());
            autoPilotModule.defineDrone(
                airportIndex, drone.getGate(), drone.getRunwayToFace(), drone.getConfig()
            );
        }
    }

    private void initializeAutopilotInputs(){
        for(int i = 0; i < settings.getDrones().length; i++){
            latestAutopilotInputs[i] = testBed.getInputs(i);
        }
    }

    /**
     * Runs the next iteration of the simulation.
     * If isSimulationPaused() is true, the simulation is not actually updated, but the update event handlers are still run.
     */
    public void runUpdate(){
        stopwatch.tick();

        if(!isSimulationPaused() && !simulationFinished && !simulationCrashed && !simulationThrewException){
            //Reset renderer
            testBed.getRenderer().clearDebugObjects();

            //Run the autopilotmodule update
            try {
                while(!newPackages.isEmpty()){
                    AirportPackage newPack = newPackages.poll();
                    autoPilotModule.deliverPackage(
                        newPack.getOrigin().getAirport().getId(), newPack.getOrigin().getId(),
                        newPack.getDestination().getAirport().getId(), newPack.getDestination().getId()
                    );
                }
            	
                for(int i = 0; i < settings.getDrones().length; i++){
                    autoPilotModule.startTimeHasPassed(i, latestAutopilotInputs[i]);
                }

                for(int i = 0; i < settings.getDrones().length; i++){
                    latestAutopilotOutputs[i] = autoPilotModule.completeTimeHasPassed(i);
                }
        	} catch (IllegalArgumentException ex) {
        		simulationCrashed = true;
        		System.err.println("Autopilot failed!");
        		ex.printStackTrace();
        	} catch (Exception ex) {
                simulationThrewException = true;
        		System.err.println("Autopilot module threw an unexpected exception!");
        		ex.printStackTrace();
        	}

            //Run the testbed update
        	try {
                simulationFinished = testBed.update(
                        (float)stopwatch.getSecondsSinceStart(),
                        (float)stopwatch.getSecondsSinceLastUpdate(),
                        latestAutopilotOutputs
                );

                for(int i = 0; i < settings.getDrones().length; i++){
                    latestAutopilotInputs[i] = testBed.getInputs(i);
                }
            } catch (CrashException ex){
                simulationCrashed = true;
                System.err.println("Plane crashed!");
                ex.printStackTrace();
            } catch (Exception ex) {
                simulationThrewException = true;
                System.err.println("Testbed threw an unexpected exception!");
                ex.printStackTrace();
            }
        }

        //Invoke the event handlers
        for (UpdateEventHandler eventHandler : updateEventHandlers) {
            eventHandler.getFunction().accept(latestAutopilotInputs, latestAutopilotOutputs);
        }
    }

    public AirportPackage addPackage(Gate sourceGate, Gate targetGate){
        if(sourceGate.hasPackage()){
            throw new IllegalStateException("The source gate already has a package");
        }

        AirportPackage pack = new AirportPackage(sourceGate, targetGate, this.autoPilotModule.getAutopilotModule());
        newPackages.add(pack);
        return pack;
    }

    public void setSimulationPaused(boolean simulationPaused) {
        stopwatch.setPaused(simulationPaused);
    }

    public boolean isSimulationPaused() {
        return stopwatch.isPaused();
    }

    public void setSimulationSpeedMultiplier(double speed) {
        stopwatch.setSpeedMultiplier(speed);
    }

    public double getSimulationSpeedMultiplier() {
        return stopwatch.getSpeedMultiplier();
    }

    public boolean hasSimulationFinished() {return simulationFinished;}

    public boolean hasSimulationCrashed(){return simulationCrashed;}

    public boolean hasSimulationThrownException(){return simulationThrewException;}
    
    public boolean isOutOfControl(){return outOfControl;}

    public TestBed getTestBed(){
        return testBed;
    }

    public AutopilotModuleAdapter getAutoPilotModule() {
        return autoPilotModule;
    }

    public void addOnUpdateEventHandler(UpdateEventHandler handler){
        updateEventHandlers.add(handler);
    }

    private void removeOnUpdateEventHandler(UpdateEventHandler handler){
        updateEventHandlers.remove(handler);
    }
}
