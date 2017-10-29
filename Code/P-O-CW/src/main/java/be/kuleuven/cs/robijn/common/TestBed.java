package be.kuleuven.cs.robijn.common;

import p_en_o_cw_2017.*;

/**
 * Provides a simulation environment for the drone to fly in.
 */
public interface TestBed {
    /**
     * Runs the physics and graphics simulation.
	 * @param secondsSinceStart time since the start of the simulation, minus the time spent paused, in seconds.
	 * @param secondsSinceLastUpdate time since the last update, minus the time spent paused, in seconds.
     * @param output the drone parameters calculated by the autopilot
	 * @return a boolean that is true when the simulation is finished.
     */
	boolean update(float secondsSinceStart, float secondsSinceLastUpdate, AutopilotOutputs output);
	
	/**
	 * Returns the latest world state as autopilot inputs.
	 */
	AutopilotInputs getInputs();
	
    /**
     * Returns the renderer, which can be used to retrieve images of the simulation
     * @return the renderer used by the testbed.
     */
    Renderer getRenderer();

	/**
	 * Returns the internal representation of the world used for rendering and physics updates
	 */
	WorldObject getWorldRepresentation();
}
