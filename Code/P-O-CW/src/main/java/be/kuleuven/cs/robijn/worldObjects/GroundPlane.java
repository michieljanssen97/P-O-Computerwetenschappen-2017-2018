package be.kuleuven.cs.robijn.worldObjects;

import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.WorldObject;
import be.kuleuven.cs.robijn.common.airports.Airport;

public class GroundPlane extends WorldObject{
	
	public GroundPlane() {}
	
	public boolean isGrass(RealVector location) {
		Airport airport = this.getParent().getFirstChildOfType(Airport.class);
		for (Airport air : airport.getAllAirports()) {
			if(air.isOnAirport(location)) {
				return false;
			}
		}
		return true;
	}
	
}
