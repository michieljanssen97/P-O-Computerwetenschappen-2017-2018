package be.kuleuven.cs.robijn.worldObjects;

import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;

public class Package extends WorldObject{

	private final Airport fromAirport;
	private final Gate fromGate;
    private final Airport toAirport;
    private final Gate toGate;
    private Boolean delivered;
    

    public Package(Airport fromAirport, Gate fromGate, Airport toAirport, Gate toGate){
        this.fromAirport = fromAirport;
        this.fromGate = fromGate;
        this.toAirport = toAirport;
        this.toGate = toGate;
        this.delivered = false;
    }
    
    public Airport getFromAirport(){
        return this.fromAirport;
    }
    
    public Gate getFromGate(){
        return this.fromGate;
    }
    
    public Airport getToAirport(){
        return this.toAirport;
    }
    
    public Gate getToGate(){
        return this.toGate;
    }
    
    public Boolean isDelivered() {
    	return this.delivered;
    }
    
    public void setDelivered() {
    	this.delivered = true;
    }
}
