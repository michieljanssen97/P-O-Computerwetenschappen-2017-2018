package be.kuleuven.cs.robijn.experiments;

public class DataPointsForPosition {
    private double time;
    private double xValue;
    private double yValue;
    private double zValue;
    
    public DataPointsForPosition (double time, double xValue, double yValue, double zValue) {
    	this.time = time;
    	this.xValue = xValue;
    	this.yValue = yValue;
    	this.zValue = zValue;
    	}	
    
    public double getTimeOfDataPoint() {
    	return this.time;
    }
    
    public double getXValueOfDataPoint() {
    	return this.xValue;
    }
    
    public double getYValueOfDataPoint() {
    	return this.yValue;
    }
    
    public double getZValueOfDataPoint() {
    	return this.zValue;
    }
}