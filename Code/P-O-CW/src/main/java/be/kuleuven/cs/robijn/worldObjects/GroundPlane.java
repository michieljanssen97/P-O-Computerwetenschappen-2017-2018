package be.kuleuven.cs.robijn.worldObjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.airports.Airport;

public class GroundPlane extends WorldObject{
	
	private List<Airport> airports = new ArrayList<Airport>();
	
	public GroundPlane() {}
	
	public void addAirport(Airport airport) {
		airports.add(airport);
	}
	
	public boolean isGrass(double groundPosX, double groundPosZ) {
		for (Airport air : this.airports) {
			RealVector airportPos = air.getWorldPosition();
			Vector2D airportSize = air.getSize();
			
			if ( (groundPosX >= airportPos.getEntry(0)-(airportSize.getX()/2)) && 
					(groundPosX <= airportPos.getEntry(0)+(airportSize.getX()/2)) && 
					(groundPosZ >= airportPos.getEntry(2)-(airportSize.getY()/2)) && 
					(groundPosZ <= airportPos.getEntry(2)+(airportSize.getY()/2)) )
				return false;
		}
		return true;
	}
	
}
