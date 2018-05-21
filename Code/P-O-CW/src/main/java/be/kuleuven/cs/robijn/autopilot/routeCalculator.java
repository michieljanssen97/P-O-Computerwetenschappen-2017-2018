package be.kuleuven.cs.robijn.autopilot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.common.airports.Runway;
import be.kuleuven.cs.robijn.worldObjects.Drone;

public class routeCalculator {
	
	public routeCalculator() {
	}
	
	public static RealVector getAscendRoute(Drone drone, Airport fromAirport, Gate fromGate, Runway fromRunway, float hight) {
		RealVector orientation = fromRunway.getWorldPosition().subtract(fromAirport.getWorldPosition());
    	//orientation = orientation.add(
    	//		fromGate.getWorldPosition().subtract(fromAirport.getWorldPosition()));
    	if (orientation.getNorm() != 0)
    		orientation = orientation.mapMultiply(1/orientation.getNorm());
    	//orientation = new ArrayRealVector(new double[] {0, 0, -1}, false);
    			
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
    		solution = new RealVector[5+1];
    	else if (hight < 3*hightInterval)
    		solution = new RealVector[6+1];
    	else
    		solution = new RealVector[7+1];	
    	
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
    	
//    	for (int i = 7;i>=3;i--) {
//    		solution[i] = orientation.mapMultiply(
//    				750
//    				).add(solution[i+1]);
//    		solution[i].setEntry(1, hight);
//    	}
    	solution[3] = orientation.mapMultiply(
    			1000
    			).add(solution[4]);
    	solution[3].setEntry(1, hight);
    	solution[2] = orientation.mapMultiply(
    			1000
    			).add(solution[3]);
    	solution[2].setEntry(1, hight);
    	solution[1] = orientation.mapMultiply(
    			1000
    			).add(solution[2]);
    	solution[1].setEntry(1, hight);
	
    	return solution;
	}
	
	public static Object[] getBestRunway(Drone drone, Airport fromAirport, Airport toAirport, Gate fromGate, Gate toGate,
			Runway fromRunway, Runway toRunway1, Runway toRunway2, float height) {
		RealVector ascendRoute = routeCalculator.getAscendRoute(drone, fromAirport, fromGate, fromRunway, height);
		RealVector[] landRoute1 = routeCalculator.getLandRoute(drone, toAirport, toGate, toRunway1, height);
		RealVector[] landRoute2 = routeCalculator.getLandRoute(drone, toAirport, toGate, toRunway2, height);
		
		if (landRoute1[1].getDistance(ascendRoute) < landRoute2[1].getDistance(ascendRoute))
			return new Object[] {toRunway1, landRoute1[1].getDistance(ascendRoute)};
		return new Object[] {toRunway1, landRoute2[1].getDistance(ascendRoute)};
	}
	
	public static Object[] getBestRunway(Drone drone, Airport fromAirport, Airport toAirport, Gate fromGate, Gate toGate, float height) {
		Runway fromRunway = routeCalculator.getFromRunway(drone, fromGate);
		Runway toRunway1 = toAirport.getRunways()[0];
		Runway toRunway2 = toAirport.getRunways()[1];
		RealVector ascendRoute = routeCalculator.getAscendRoute(drone, fromAirport, fromGate, fromRunway, height);
		RealVector[] landRoute1 = routeCalculator.getLandRoute(drone, toAirport, toGate, toRunway1, height);
		RealVector[] landRoute2 = routeCalculator.getLandRoute(drone, toAirport, toGate, toRunway2, height);
		
		if (landRoute1[1].getDistance(ascendRoute) < landRoute2[1].getDistance(ascendRoute))
			return new Object[] {toRunway1, landRoute1[1].getDistance(ascendRoute)};
		return new Object[] {toRunway1, landRoute2[1].getDistance(ascendRoute)};
	}
	
	public static List<Airport> orderAirports(Drone drone, Airport fromAirport, Gate fromGate, float height) {
		WorldObject world = fromAirport.getParent();
		List<Airport> bestAirports = new ArrayList<>();
		HashMap<Airport, Float> pairs = new HashMap<>();
		for (Airport airport: world.getChildrenOfType(Airport.class)) {
			Runway fromRunway = routeCalculator.getFromRunway(drone, fromGate);
			Object[] first = routeCalculator.getBestRunway(drone, fromAirport, airport, fromGate, airport.getGates()[0], fromRunway, 
					airport.getRunways()[0], airport.getRunways()[1], height);
			Object[] second = routeCalculator.getBestRunway(drone, fromAirport, airport, fromGate, airport.getGates()[1], fromRunway, 
					airport.getRunways()[0], airport.getRunways()[1], height);
			float distance1 = (float) first[1];
			float distance2 = (float) second[1];
			float bestDistance;
			if (distance1 <= distance2)
				bestDistance = distance1;
			else
				bestDistance = distance2;
			pairs.put(airport, bestDistance);
			Airport port = null;
			boolean flag = false;
			for (int i = 0; i< bestAirports.size(); i++) {
				port = bestAirports.get(i);
				if (pairs.get(port) > bestDistance) {
					bestAirports.add(i, airport);
					flag = true;
					break;
				}
			}
			if (!flag)
				bestAirports.add(airport);
		}
		return bestAirports;
			
	}
	
	public static RealVector[] calculateRoute(Drone drone, Gate fromGate, Gate toGate, float height) {    
		Airport fromAirport = fromGate.getAirport();
    	Airport toAirport = toGate.getAirport();
    	
    	Runway fromRunway = getFromRunway(drone, fromGate);
    			
    	RealVector firstTarget = routeCalculator.getAscendRoute(drone, fromAirport, fromGate, fromRunway, height);
    		
    	Runway toRunway = getToRunway(drone, fromGate, toGate, fromRunway, height);
    	
    	RealVector[] nextTargets = routeCalculator.getLandRoute(drone, toAirport, toGate, toRunway, height);
    	nextTargets[0] = firstTarget;
    	return nextTargets;
    }
	
	public static Runway getFromRunway(Drone drone, Gate fromGate) {
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
    	
    	return fromRunway;
	}
	
	public static Runway getToRunway(Drone drone, Gate fromGate, Gate toGate, Runway fromRunway, float height) {
		Airport fromAirport = fromGate.getAirport();
    	Airport toAirport = toGate.getAirport();
    	Runway toRunway1 = toAirport.getRunways()[0];
    	Runway toRunway2 = toAirport.getRunways()[1];
    	
    	return (Runway) routeCalculator.getBestRunway(drone, fromAirport, toAirport, fromGate, toGate, fromRunway, toRunway1, toRunway2, height)[0];
	}
}
