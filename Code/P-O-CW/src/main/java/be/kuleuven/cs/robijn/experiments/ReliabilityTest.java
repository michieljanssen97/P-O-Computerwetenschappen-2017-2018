package be.kuleuven.cs.robijn.experiments;

import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.common.SimulationSettings.AirportDefinition;
import be.kuleuven.cs.robijn.common.SimulationSettings.DroneDefinition;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.AirportPackage;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.common.airports.generator.AirportGenerator;
import be.kuleuven.cs.robijn.common.stopwatch.ConstantIntervalStopwatch;
import be.kuleuven.cs.robijn.common.stopwatch.Stopwatch;
import interfaces.AutopilotConfig;
import interfaces.AutopilotConfigReader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.*;

public class ReliabilityTest {
    TestSetup getSimpleTest(){
        AirportDefinition airport0 = new AirportDefinition()
                .setCenterX(0)
                .setCenterZ(0)
                .setCenterToRunway0X(0)
                .setCenterToRunway0Z(-1);

        SimulationSettings settings = new SimulationSettings()
                .setGateLength(30)
                .setRunwayLength(100)
                .setAirports(new AirportDefinition[]{
                    airport0
                })
                .setDrones(new DroneDefinition[]{
                    new DroneDefinition(getDefaultConfig())
                        .setAirport(airport0)
                        .setGate(0)
                        .setRunwayToFace(1)
                });

        return new TestSetup()
                .setName("Simple test")
                .setUpdatesPerSecond(60)
                .setMaxSimulationRuntimeInSeconds(1000)
                .setSettings(settings)
                .setPacketAddingFunction(new Consumer<SimulationState>() {
                    boolean hasAddedPackage = false;
                    @Override
                    public void accept(SimulationState state) {
                        if(!hasAddedPackage){
                            Airport airport = state.driver.getTestBed().getWorldRepresentation().getChildrenOfType(Airport.class).get(0);
                            Gate source = airport.getGates()[0];
                            Gate dest = airport.getGates()[1];
                            state.driver.addPackage(source, dest);
                            hasAddedPackage = true;
                        }
                    }
                })
                .setTestFinishedPredicate(state -> {
                    return state.packages.get(0).hasBeenDelivered();
                });
    }

    public TestSetup[] getTests(){
        return new TestSetup[]{
            getSimpleTest(),
        };
    }

    public static void main(String[] args) {
        ReliabilityTest tester = new ReliabilityTest();
        tester.runTests();
    }

    public void runTests(){
        TestSetup[] tests = getTests();

        for (int i = 0; i < tests.length; i++){
            try{
                TestResult result = runTest(tests[i]);
                System.out.printf("%d/%d (%s): \n", i+1, tests.length, tests[i].name);
                System.out.printf("    Simulation duration: %f seconds\n", result.runtimeInSeconds);
                System.out.printf("     Simulation crashed: %s\n", ((Boolean)result.simulationThrewException).toString());
                System.out.printf("          Drone crashed: %s\n", ((Boolean)result.droneCrashed).toString());
                System.out.printf("     Package deliveries: \n");
                System.out.printf("        %-15s | %-15s | %-15s | %-15s | %-15s\n", "Source", "Destination", "Addition time", "Pickup time", "Delivery time");
                for(int dI = 0; dI < result.packetDeliveries.size(); dI++){
                    Delivery d = result.packetDeliveries.get(dI);
                    String origin = d.airportPackage.getOrigin().getUID();
                    String dest = d.airportPackage.getDestination().getUID();
                    System.out.printf("        %-6s | %-6s | %-15f | %-15f | %-15f\n", origin, dest, d.additionTime, d.pickupTime, d.deliveryTime);
                }
                System.out.println();
            }catch (Exception ex){
                System.err.printf("%d/%d (%s): CRITICAL_FAILURE \n", i+1, tests.length, tests[i].name, ex.getMessage());
            }
        }

        System.out.println("Tests finished");
    }

    private static TestResult runTest(TestSetup testSetup){
        TestResult result = new TestResult();

        //Setup simulation
        double interval = 1d/((double)testSetup.updatesPerSecond);
        List<AirportPackage> packages = new ArrayList<>();
        HashMap<AirportPackage, Delivery> pendingDeliveries = new HashMap<>();

        Stopwatch stopwatch = new ConstantIntervalStopwatch(interval);
        SimulationDriver driver = new SimulationDriver(testSetup.settings, stopwatch, true){
            @Override
            public AirportPackage addPackage(Gate sourceGate, Gate targetGate) {
                AirportPackage p = super.addPackage(sourceGate, targetGate);
                packages.add(p);
                Delivery d = new Delivery();
                d.airportPackage = p;
                d.additionTime = stopwatch.getSecondsSinceStart();
                pendingDeliveries.put(p, d);
                return p;
            }
        };

        //Run simulation
        int maxUpdates = testSetup.maxSimulationRuntimeInSeconds * testSetup.updatesPerSecond;
        int updateI = 0;
        for(; updateI < maxUpdates; updateI++){
            testSetup.packetAddingFunction.accept(new SimulationState(stopwatch.getSecondsSinceStart(), driver, packages));
            driver.runUpdate();

            result.runtimeInSeconds = stopwatch.getSecondsSinceStart();

            for(AirportPackage p : packages){
                Delivery d = pendingDeliveries.get(p);
                if(p.getState() == AirportPackage.State.IN_TRANSIT && d.pickupTime < 0){
                    d.deliveryTime = stopwatch.getSecondsSinceStart();
                }else if(p.hasBeenDelivered() && d.deliveryTime < 0){
                    d.deliveryTime = stopwatch.getSecondsSinceStart();
                    packages.remove(p);
                    pendingDeliveries.remove(d);
                    result.packetDeliveries.add(d);
                }
            }

            if(driver.hasSimulationFinished() || testSetup.testFinishedPredicate.test(new SimulationState(stopwatch.getSecondsSinceStart(), driver, packages))){
                break;
            }

            if(driver.hasSimulationCrashed()){
                result.droneCrashed = true;
                break;
            }

            if(driver.hasSimulationThrownException()){
                result.simulationThrewException = true;
                break;
            }
        }

        return result;
    }

    static class TestSetup {
        public String name = "No name";
        public SimulationSettings settings = new SimulationSettings();
        public int maxSimulationRuntimeInSeconds = 30;
        public int updatesPerSecond = 30;
        public Consumer<SimulationState> packetAddingFunction = state -> {};
        public Predicate<SimulationState> testFinishedPredicate;

        public TestSetup setName(String name) {
            this.name = name;
            return this;
        }

        public TestSetup setSettings(SimulationSettings settings) {
            this.settings = settings;
            return this;
        }

        public TestSetup setMaxSimulationRuntimeInSeconds(int maxSimulationRuntimeInSeconds) {
            this.maxSimulationRuntimeInSeconds = maxSimulationRuntimeInSeconds;
            return this;
        }

        public TestSetup setUpdatesPerSecond(int updatesPerSecond) {
            this.updatesPerSecond = updatesPerSecond;
            return this;
        }

        public TestSetup setPacketAddingFunction(Consumer<SimulationState> packetAddingFunction) {
            this.packetAddingFunction = packetAddingFunction;
            return this;
        }

        public TestSetup setTestFinishedPredicate(Predicate<SimulationState> testFinishedPredicate) {
            this.testFinishedPredicate = testFinishedPredicate;
            return this;
        }
    }

    static class TestResult {
        public boolean simulationThrewException;
        //public List<Drone> crashedDrones = new ArrayList<>();
        public boolean droneCrashed;
        public List<Delivery> packetDeliveries = new ArrayList<>();
        public double runtimeInSeconds;
    }

    static class Delivery {
        AirportPackage airportPackage;
        double additionTime = -1;
        double pickupTime = -1;
        double deliveryTime = -1;
    }

    static class SimulationState {
        double time;
        SimulationDriver driver;
        List<AirportPackage> packages;

        public SimulationState(double time, SimulationDriver driver, List<AirportPackage> packages) {
            this.time = time;
            this.driver = driver;
            this.packages = Collections.unmodifiableList(packages);
        }
    }

    private static AutopilotConfig getDefaultConfig(){
        try(DataInputStream in = new DataInputStream(Resources.getResourceStream("/default_autopilot_config.bin"))){
            return AutopilotConfigReader.read(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
