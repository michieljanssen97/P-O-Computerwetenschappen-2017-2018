package be.kuleuven.cs.robijn.common;

import p_en_o_cw_2017.*;

public interface AutoPilot {
    /**
     * Provides the latest simulation data to the autopilot, and returns the adjustments made by the autopilot.
     * @param input the latest simulation data (not null)
     * @return the autopilot outputs (not null)
     */
    AutopilotOutputs update(AutopilotInputs input);
}
