package be.kuleuven.cs.robijn.experiments;

import be.kuleuven.cs.robijn.common.Box;
import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.SimulationDriver;
import be.kuleuven.cs.robijn.common.WorldGenerator;
import be.kuleuven.cs.robijn.common.stopwatch.ConstantIntervalStopwatch;
import interfaces.AutopilotConfig;
import interfaces.AutopilotConfigReader;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class ReliabilityTest {
    public enum TestResult{
        SUCCESS, CRASH, DID_NOT_REACH_TARGET
    }

    public static void main(String[] args) {
        ReliabilityTest tester = new ReliabilityTest();
        tester.runTests();
    }

    public void runTests(){
        //Config
        int timesToRunTest = 100;
        AutopilotConfig config = getDefaultConfig();
        int maxSimulationRuntimeInSeconds = 30;
        int updatesPerSecond = 30;
        WorldGenerator.WorldGeneratorSettings settings = new WorldGenerator.WorldGeneratorSettings();
        settings.setRandomizeColors(true);
        settings.setBoxCount(2);

        for (int i = 0; i < timesToRunTest; i++){
            List<Box> boxes = WorldGenerator.generateBoxes(settings);
            try{
                TestResult result = runTest(boxes, config, maxSimulationRuntimeInSeconds, updatesPerSecond);
                System.out.printf("%d/%d: %s\n", i, timesToRunTest, result.toString());
            }catch (Exception ex){
                System.err.printf("%d/%d: CRITICAL_FAILURE\n", i, timesToRunTest);
            }
        }
    }

    private static TestResult runTest(List<Box> boxes, AutopilotConfig config, int maxSimulationRuntimeInSeconds, int updatesPerSecond){
        //Run simulation
        SimulationDriver driver = new SimulationDriver(boxes, config, new ConstantIntervalStopwatch(1d/((double)updatesPerSecond)));

        int maxUpdates = maxSimulationRuntimeInSeconds * updatesPerSecond;
        int updateI = 0;
        for(; updateI < maxUpdates && !driver.hasSimulationFinished() && !driver.hasSimulationCrashed(); updateI++){
            driver.runUpdate();
        }

        if(driver.hasSimulationCrashed()){
            return TestResult.CRASH;
        }
        return driver.hasSimulationFinished() ? TestResult.SUCCESS : TestResult.DID_NOT_REACH_TARGET;
    }

    private static AutopilotConfig getDefaultConfig(){
        try(DataInputStream in = new DataInputStream(Resources.getResourceStream("/default_autopilot_config.bin"))){
            return AutopilotConfigReader.read(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
