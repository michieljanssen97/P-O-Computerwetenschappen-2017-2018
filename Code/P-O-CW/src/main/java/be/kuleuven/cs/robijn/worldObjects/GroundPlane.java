package be.kuleuven.cs.robijn.worldObjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.airports.Airport;

public class GroundPlane extends WorldObject{
	
	private List<Airport> airports = new ArrayList<Airport>();
	
	public GroundPlane() {}
	
	public void addAirport(Airport airport) {
		airports.add(airport);
	}
	
	public boolean isGrass(RealVector location) {
		for (Airport air : this.airports) {
			if(air.isOnAirport(location)) {
				return false;
			}
		}
		return true;
	}
	
}
