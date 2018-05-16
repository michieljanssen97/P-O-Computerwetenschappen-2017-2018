package be.kuleuven.cs.robijn.autopilot;

import be.kuleuven.cs.robijn.common.SimulationBuilder;
import be.kuleuven.cs.robijn.common.SimulationSettings;
import be.kuleuven.cs.robijn.common.SimulationSettings.AirportDefinition;
import be.kuleuven.cs.robijn.common.SimulationSettings.DroneDefinition;
import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.AirportPackage;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.gui.ObservableAutoPilotConfig;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import interfaces.AutopilotConfig;
import interfaces.AutopilotInputs;
import interfaces.AutopilotOutputs;

import java.util.ArrayList;
import java.util.List;

public class AutopilotModuleAdapter implements interfaces.AutopilotModule {
    private WorldObject world;
    private AutopilotModule module;

    private float length, width;
    private List<AirportDefinition> airportDefinitions = new ArrayList<>();
    private List<DroneDefinition> droneDefinitions = new ArrayList<>();

    private List<Airport> airports = new ArrayList<>();
    private List<Drone> drones = new ArrayList<>();

    @Override
    public void defineAirportParams(float length, float width) {
        this.length = length;
        this.width = width;
    }

    @Override
    public void defineAirport(float centerX, float centerZ, float centerToRunway0X, float centerToRunway0Z) {
        AirportDefinition def = new AirportDefinition();
        def.setCenterX(centerX);
        def.setCenterZ(centerZ);
        def.setCenterToRunway0X(centerToRunway0X);
        def.setCenterToRunway0Z(centerToRunway0Z);
        airportDefinitions.add(def);
    }

    @Override
    public void defineDrone(int airport, int gate, int pointingToRunway, AutopilotConfig config) {
        DroneDefinition def = new DroneDefinition(
                new ObservableAutoPilotConfig(config),
                airportDefinitions.get(airport),
                gate,
                pointingToRunway
        );
        droneDefinitions.add(def);
    }

    private void buildWorldIfNull(){
        if(world == null){
            world = new WorldObject();
            SimulationSettings settings = new SimulationSettings();
            settings.setRunwayLength(length);
            settings.setGateLength(width);
            settings.setAirports(airportDefinitions.toArray(new AirportDefinition[airportDefinitions.size()]));
            settings.setDrones(droneDefinitions.toArray(new DroneDefinition[droneDefinitions.size()]));
            SimulationBuilder.buildSimulation(settings, world);

            airports = world.getChildrenOfType(Airport.class);
            drones = world.getChildrenOfType(Drone.class);
            module = new AutopilotModule(world);
        }
    }

    @Override
    public void deliverPackage(int fromAirportIndex, int fromGateIndex, int toAirportIndex, int toGateIndex) {
        buildWorldIfNull();

        Airport fromAirport = airports.get(fromAirportIndex);
        Gate fromGate = fromAirport.getGates()[fromGateIndex];
        Airport toAirport = airports.get(toAirportIndex);
        Gate toGate = toAirport.getGates()[toGateIndex];
        
        if(! fromGate.hasPackage()){ //Er mag maar 1 pakket beschikbaar zijn per Gate
	        AirportPackage newPackage = new AirportPackage(fromGate, toGate, module);
	        fromGate.setPackage(newPackage);
	
	        module.deliverPackage(fromAirport, fromGate, toAirport, toGate);
        }
    }

    @Override
    public void startTimeHasPassed(int droneIndex, AutopilotInputs inputs) {
        buildWorldIfNull();
        
		AirportPackage airpPack = this.getWorld().getFirstChildOfType(AirportPackage.class);
		if(airpPack != null) {
			airpPack.assignPackages();
		}

        Drone drone = drones.get(droneIndex);
        module.startTimeHasPassed(drone, inputs);
    }

    @Override
    public AutopilotOutputs completeTimeHasPassed(int droneIndex) {
        Drone drone = drones.get(droneIndex);
        return module.completeTimeHasPassed(drone);
    }

    @Override
    public void simulationEnded() {
        module.simulationEnded();
    }
    
    public AutopilotModule getAutopilotModule() {
    	return this.module;
    }

	public WorldObject getWorld() {
		return this.world;
	}
}
