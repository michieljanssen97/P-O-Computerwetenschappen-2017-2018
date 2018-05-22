package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.common.stopwatch.ConstantIntervalStopwatch;
import be.kuleuven.cs.robijn.worldObjects.Box;
import interfaces.AutopilotConfig;
import interfaces.AutopilotConfigReader;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {
    /*@Test
    public void testSingleRedBoxForward(){
        //Config
        int maxSimulationRuntimeInSeconds = 30;

        //Boxes
        Box box = new Box();
        box.setRelativePosition(new ArrayRealVector(new double[]{0, 0, -30}, false));
        List<Box> boxes = Arrays.asList(box);

        //Run test
        testWithDefaultSettings(boxes, maxSimulationRuntimeInSeconds);
    }

    private static void testWithDefaultSettings(List<Box> boxes, int maxSimulationRuntimeInSeconds){
        //Config
        AutopilotConfig config = getDefaultConfig();
        int updatesPerSecond = 30;

        //Run simulation
        SimulationDriver driver = new SimulationDriver(boxes, config, new ConstantIntervalStopwatch(1d/((double)updatesPerSecond)));

        int maxUpdates = maxSimulationRuntimeInSeconds * updatesPerSecond;
        int updateI = 0;
        for(; updateI < maxUpdates && !driver.hasSimulationFinished() && !driver.hasSimulationCrashed(); updateI++){
            driver.runUpdate();
        }

        assertFalse(driver.hasSimulationCrashed(), "Simulation crashed!");
        assertTrue(driver.hasSimulationFinished(), "Simulation did not finish in time!");
    }

    private static AutopilotConfig getDefaultConfig(){
        try(DataInputStream in = new DataInputStream(Resources.getResourceStream("/default_autopilot_config.bin"))){
            return AutopilotConfigReader.read(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }*/
}
