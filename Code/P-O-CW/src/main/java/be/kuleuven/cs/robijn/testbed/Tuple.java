package be.kuleuven.cs.robijn.testbed;

import java.util.ArrayList;

public class Tuple {
	private static ArrayList<Tuple> allTuples = new ArrayList<Tuple>();
    private double time;
    private double value;
    
    public Tuple (double time, double value) {
    	this.time = time;
    	this.value = value;
    	}	
    
    public void addTupleToListOfTuples() {
    	allTuples.add(this);
    }
    
    public double getTimeOfTuple() {
    	return this.time;
    }
    
    public double getValueOfTuple() {
    	return this.value;
    }
    
    public static ArrayList<Tuple> getAllTuples(){
    	return allTuples;
    }
}
