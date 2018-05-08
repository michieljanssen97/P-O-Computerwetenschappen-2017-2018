package be.kuleuven.cs.robijn.worldObjects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.WorldObject;
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
			Vector2D airport2DPos = new Vector2D(airportPos.getEntry(0), airportPos.getEntry(2));
			Vector2D airportSize = air.getSize();
			double angle = air.getAngle()+Math.PI;
			Vector2D tyrePos = new Vector2D(groundPosX, groundPosZ);
			
			Vector2D up = new Vector2D(airportSize.getX()/2, new Vector2D(-Math.cos(angle), -Math.sin(angle)));		
			Vector2D down = new Vector2D(airportSize.getX()/2, new Vector2D(Math.cos(angle), Math.sin(angle)));		
			Vector2D right = new Vector2D(airportSize.getY()/2, new Vector2D(-Math.sin(angle), Math.cos(angle)));		
			Vector2D left = new Vector2D(airportSize.getY()/2, new Vector2D(Math.sin(angle), -Math.cos(angle)));
			
			Vector2D leftUpCorner = airport2DPos.add(up).add(right);
			Vector2D leftDownCorner = airport2DPos.add(up).add(left);
			Vector2D rightDownCorner = airport2DPos.add(down).add(left);
			Vector2D rightUpCorner = airport2DPos.add(down).add(right);
			
			//The rectangle consists out of four vectors. If the tyre position is left of all four vectors, it's located within the rectangle's area.
			if ( Math.atan2(leftDownCorner.getY()-leftUpCorner.getY(), leftDownCorner.getX() - leftUpCorner.getX()) > Math.atan2(tyrePos.getY()-leftUpCorner.getY(), tyrePos.getX()-leftUpCorner.getX()) &&
					Math.atan2(rightDownCorner.getY()-leftDownCorner.getY(), rightDownCorner.getX() - leftDownCorner.getX()) > Math.atan2(tyrePos.getY()-leftDownCorner.getY(), tyrePos.getX()-leftDownCorner.getX()) &&
					Math.atan2(rightUpCorner.getY()-rightDownCorner.getY(), rightUpCorner.getX() - rightDownCorner.getX()) > Math.atan2(tyrePos.getY()-rightDownCorner.getY(), tyrePos.getX()-rightDownCorner.getX()) &&
					Math.atan2(leftUpCorner.getY()-rightUpCorner.getY(), leftUpCorner.getX() - rightUpCorner.getX()) > Math.atan2(tyrePos.getY()-rightUpCorner.getY(), tyrePos.getX()-rightUpCorner.getX()) )
				return false;
		}
		return true;
	}
	
}
