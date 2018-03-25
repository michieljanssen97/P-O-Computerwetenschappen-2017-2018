package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;
import org.apache.commons.math3.exception.NoBracketingException;

import be.kuleuven.cs.robijn.common.Drone;
import interfaces.AutopilotConfig;

public class EquationSolver {
	
	public EquationSolver(Drone drone, AutopilotConfig config, FunctionCalculator functionCalculator) {
		this.drone = drone;
		this.config = config;
		this.functionCalculator = functionCalculator;
	}
	
	private Drone drone;
	
	private AutopilotConfig config;
	
	private UnivariateSolver solver = new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-8, 5);
	
	private FunctionCalculator functionCalculator;
	
	public float getMaxInclinationWing() {
		return this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 1);
	}
	
	public float getMinInclinationWing() {
		return this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 1);
	}
	
	public float getMaxInclinationHorStab() {
		return this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 2);
	}
	
	public float getMinInclinationHorStab() {
		return this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 2);
	}
	
	public float getMaxInclinationVerStab() {
		return this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), true, (float) Math.toRadians(1.0), drone, 3);
	}
	
	public float getMinInclinationVerStab() {
		return this.minMaxInclination((float)(Math.PI/2), (float)(-Math.PI/2), false, (float) Math.toRadians(1.0), drone, 3);
	}
	
	public float minMaxInclination(float upperBound, float lowerBound, boolean max, float accuracy, Drone drone, int airfoil)
			throws IllegalStateException {
		float inclination;
		if (max == true) {
			inclination = upperBound;
		}
		else {
			inclination = lowerBound;
		}
		while (this.hasCrash(inclination, drone, airfoil)) {
			if (max == true) {
				inclination -= accuracy;
				if (inclination < lowerBound) {
					throw new IllegalStateException("simulation failed!");
				}
			}
			else {
				inclination += accuracy;
				if (inclination > upperBound)
					throw new IllegalStateException("simulation failed!");
			}
		}
		return inclination;
	}
	
	public boolean hasCrash(float inclination, Drone drone, int airfoil) {
		boolean crash = false;
		if (airfoil == 1) {
			float AOALeftWing = drone.calculateAOA(drone.getNormalHor(inclination),
					drone.getProjectedVelocityLeftWing(), drone.getAttackVectorHor(inclination));
			if ((AOALeftWing > this.config.getMaxAOA()) || (AOALeftWing < -this.config.getMaxAOA()))
				crash = true;
		}
		if ((airfoil == 1) && (crash == false)) {
			float AOARightWing = drone.calculateAOA(drone.getNormalHor(inclination),
					drone.getProjectedVelocityRightWing(), drone.getAttackVectorHor(inclination));
			if ((AOARightWing > this.config.getMaxAOA()) || (AOARightWing < -this.config.getMaxAOA()))
				crash = true;
		}
		if ((airfoil == 2) && (crash == false)) {
			float AOAHorStab = drone.calculateAOA(drone.getNormalHor(inclination),
					drone.getProjectedVelocityHorStab(), drone.getAttackVectorHor(inclination));
			if ((AOAHorStab > this.config.getMaxAOA()) || (AOAHorStab < -this.config.getMaxAOA()))
				crash = true;
		}
		if ((airfoil == 3) && (crash == false)) {
			float AOAVerStab = drone.calculateAOA(drone.getNormalVer(inclination),
					drone.getProjectedVelocityVerStab(), drone.getAttackVectorVer(inclination));
			if ((AOAVerStab > this.config.getMaxAOA()) || (AOAVerStab < -this.config.getMaxAOA()))
				crash = true;
		}
		return crash;	
	}
	
	public float solverForHeading() {
		UnivariateFunction function = this.functionCalculator.functionForHeading();
		try {
			double solution = solver.solve(100, function, getMinInclinationVerStab(), getMaxInclinationVerStab());
			return (float) solution;
		} catch (NoBracketingException exc) {
			return Float.NaN;
		}
	}
	
	public float solverForYVelocity(float verStabInclination) {
		UnivariateFunction function = this.functionCalculator.functionForYVelocity(verStabInclination);
		try {
			double solution = solver.solve(100, function, getMinInclinationWing(), getMaxInclinationWing());
			return (float) solution;
		} catch (NoBracketingException exc) {
			return Float.NaN;
		}
	}
	
	public float solverForXVelocity(float verStabInclination, float wingInclination) {
		UnivariateFunction function = this.functionCalculator.functionForXVelocity(verStabInclination, wingInclination);
		try {
			double solution = solver.solve(100, function, -Math.PI/2, Math.PI/2);
			return (float) solution;
		} catch (NoBracketingException exc) {
			return Float.NaN;
		}
	}
	
	public float solverForRoll(float wingInclination) {
		UnivariateFunction function = this.functionCalculator.functionForRoll(wingInclination);
		try {
			double solution = solver.solve(100, function, Math.max(getMinInclinationWing() - wingInclination, wingInclination - getMaxInclinationWing()),
					Math.min(getMaxInclinationWing() - wingInclination, wingInclination - getMinInclinationWing()));
			return (float) solution;
		} catch (NoBracketingException exc) {
			return Float.NaN;
		}
	}
	
	public float solverForYVelocityWithRoll(float verStabInclination, float rollInclination) {
		UnivariateFunction function = this.functionCalculator.functionForYVelocityWithRoll(verStabInclination, rollInclination);
		try {
			double solution = solver.solve(100, function, getMinInclinationWing() + Math.abs(rollInclination), getMaxInclinationWing() - Math.abs(rollInclination));
			return (float) solution;
		} catch (NoBracketingException exc) {
			return Float.NaN;
		}
	}
	
	public float solverForPitch() {
		UnivariateFunction function = this.functionCalculator.functionForPitch();
		try {
			double solution = solver.solve(100, function, getMinInclinationHorStab(), getMaxInclinationHorStab());
			return (float) solution;
		} catch (NoBracketingException exc) {
			return Float.NaN;
		}
	}
}
