package be.kuleuven.cs.robijn.autopilot;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.common.airports.Runway;
import be.kuleuven.cs.robijn.worldObjects.Drone;

public class routeCalculator {
	
	public routeCalculator() {
	}
	
	public static RealVector getAscendRoute(Drone drone, Airport fromAirport, Gate fromGate, Runway fromRunway, float hight) {
		RealVector orientation = fromRunway.getWorldPosition().subtract(fromAirport.getWorldPosition());
//    	orientation = orientation.add(
//    			fromGate.getWorldPosition().subtract(fromAirport.getWorldPosition()));
    	if (orientation.getNorm() != 0)
    		orientation = orientation.mapMultiply(1/orientation.getNorm());
    			
    	RealVector solution = orientation.mapMultiply(
    			(fromAirport.getSize().getX()/2) + (hight/Math.tan(Math.toRadians(5)))
    			).add(drone.getWorldPosition());
    	solution.setEntry(1, hight);
    	return solution;
	}
	
	public static RealVector[] getLandRoute(Drone drone, Airport toAirport, Gate toGate, Runway toRunway, float hight) {
		AutopilotSettings settings = new AutopilotSettings();
		float hightInterval = settings.getHeight();
		RealVector orientation = toRunway.getWorldPosition().subtract(toAirport.getWorldPosition());
//    	orientation = orientation.add(
//    			toGate.getWorldPosition().subtract(toAirport.getWorldPosition()));
    	if (orientation.getNorm() != 0)
    		orientation = orientation.mapMultiply(1/orientation.getNorm());
    	
    	RealVector[] solution;
    	if (hight < hightInterval)
    		solution = new RealVector[5];
    	else if (hight < 3*hightInterval)
    		solution = new RealVector[6];
    	else
    		solution = new RealVector[7];	
    	
    	solution[solution.length-1] = orientation.mapMultiply(
    			(toAirport.getSize().getX()/2) - (drone.getTailSize()/2)
    			).add(toGate.getWorldPosition());
    	solution[solution.length-1].setEntry(1, -drone.getConfig().getWheelY() + drone.getConfig().getTyreRadius());
    	
    	if (hight < hightInterval) {
    		solution[solution.length-2] = orientation.mapMultiply(
        			(hight/Math.tan(Math.toRadians(1)))
        			).add(solution[solution.length-1]);
    		solution[solution.length-2].setEntry(1, hight);
    	}
    	else if (hight < 3*hightInterval) {
    		solution[solution.length-2] = orientation.mapMultiply(
        			(hightInterval/Math.tan(Math.toRadians(1)))
        			).add(solution[solution.length-1]);
    		solution[solution.length-2].setEntry(1, hightInterval);
    		solution[solution.length-3] = orientation.mapMultiply(
        			((hight - hightInterval)/Math.tan(Math.toRadians(3)))
        			).add(solution[solution.length-2]);
    		solution[solution.length-3].setEntry(1, hight);
    	}
    	else {
    		solution[solution.length-2] = orientation.mapMultiply(
        			(hightInterval/Math.tan(Math.toRadians(1)))
        			).add(solution[solution.length-1]);
    		solution[solution.length-2].setEntry(1, hightInterval);
    		solution[solution.length-3] = orientation.mapMultiply(
        			((2*hightInterval)/Math.tan(Math.toRadians(3)))
        			).add(solution[solution.length-2]);
    		solution[solution.length-3].setEntry(1, 3*hightInterval);
    		solution[solution.length-4] = orientation.mapMultiply(
        			((hight-(3*hightInterval))/Math.tan(Math.toRadians(5)))
        			).add(solution[solution.length-3]);
    		solution[solution.length-4].setEntry(1, hight);
    	}
    	
    	solution[2] = orientation.mapMultiply(
    			750
    			).add(solution[3]);
    	solution[2].setEntry(1, hight);
    	solution[1] = orientation.mapMultiply(
    			1000
    			).add(solution[2]);
    	solution[1].setEntry(1, hight);
	
    	return solution;
	}
	
	public static Runway getBestRunway(Drone drone, Airport fromAirport, Airport toAirport, Gate fromGate, Gate toGate,
			Runway fromRunway, Runway toRunway1, Runway toRunway2, float hight) {
		RealVector ascendRoute = routeCalculator.getAscendRoute(drone, fromAirport, fromGate, fromRunway, hight);
		RealVector[] landRoute1 = routeCalculator.getLandRoute(drone, toAirport, toGate, toRunway1, hight);
		RealVector[] landRoute2 = routeCalculator.getLandRoute(drone, toAirport, toGate, toRunway2, hight);
		
		if (landRoute1[1].getDistance(ascendRoute) < landRoute2[1].getDistance(ascendRoute))
			return toRunway1;
		return toRunway2;		
	}
	
	public static RealVector[] calculateRoute(Drone drone, Gate fromGate, Gate toGate, float hight) {
    	Airport fromAirport = fromGate.getAirport();
    	Runway fromRunway1 = fromAirport.getRunways()[0];
    	RealVector vector1 = fromRunway1.getWorldPosition();
    	Runway fromRunway2 = fromAirport.getRunways()[1];
    	RealVector vector2 = fromRunway2.getWorldPosition();
    	
    	Runway fromRunway;
    	RealVector droneHeading = drone.transformationToWorldCoordinates(new ArrayRealVector(new double[] {0, 0, -50}, false))
    			.add(drone.getWorldPosition());
    	if (vector1.getDistance(droneHeading) < vector2.getDistance(droneHeading))
    		fromRunway = fromRunway1;
    	else {
    		fromRunway = fromRunway2;
    	}
    			
    	RealVector firstTarget = routeCalculator.getAscendRoute(drone, fromAirport, fromGate, fromRunway, hight);
    		
    	Airport toAirport = toGate.getAirport();
    	Runway toRunway1 = toAirport.getRunways()[0];
    	Runway toRunway2 = toAirport.getRunways()[1];
    	Runway toRunway = routeCalculator.getBestRunway(drone, fromAirport, toAirport, fromGate, toGate, fromRunway, toRunway1, toRunway2, hight);
    	
    	RealVector[] nextTargets = routeCalculator.getLandRoute(drone, toAirport, toGate, toRunway, hight);
    	nextTargets[0] = firstTarget;
    	return nextTargets;
    }
}
