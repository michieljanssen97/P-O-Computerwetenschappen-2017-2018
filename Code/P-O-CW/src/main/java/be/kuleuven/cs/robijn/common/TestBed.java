package be.kuleuven.cs.robijn.common;

import p_en_o_cw_2017.AutopilotInputs;
import p_en_o_cw_2017.AutopilotOutputs;

/**
 * Provides a simulation environment for the drone to fly in.
 */
public interface TestBed {
    /**
     * Runs the physics and graphics simulation to provide simulation data for the autopilot.
     * @param output the drone parameters calculated by the autopilot
     * @return the latest simulation data
     */
    AutopilotInputs update(AutopilotOutputs output);

    /**
     * Returns the renderer, which can be used to retrieve images of the simulation
     * @return the renderer used by the testbed.
     */
    Renderer getRenderer();
}
