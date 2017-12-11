package be.kuleuven.cs.robijn.experiments;

public class DataPointsForChart {
    private double time;
    private double value;
    
    public DataPointsForChart (double time, double value) {
    	this.time = time;
    	this.value = value;
    	}	
    
    public double getTimeOfDataPoint() {
    	return this.time;
    }
    
    public double getValueOfDataPoint() {
    	return this.value;
    }
}
