package be.kuleuven.cs.robijn.experiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.Drone;

public class ExpPosition {
	private static ArrayList<DataPointsForPosition> allDataPointsPosition = new ArrayList<DataPointsForPosition>();
	private long startTime = 0;
	private boolean firstIterationToDraw = true;
	
	public void drawMain(String type) {
//		File output = null;
		String enc = null;
//		if (type == "Our") {
			File output = new File("invoer.txt");
//		}
//		else if (type == "Provided") {
//			output = new File("invoerProvidedTB.txt");
//		}
		
		//write the tile to the output file
		try {
			FileUtils.writeStringToFile(output, type + System.lineSeparator(), enc, true);
		} catch (IOException e) {
			System.out.println("Error in drawMain()");
		}
		
		//write the points (time, value) to the output file
		ArrayList<DataPointsForPosition> allPoints = getAllDataPoints();
		for (DataPointsForPosition points:allPoints) {
			try {
				double time = points.getTimeOfDataPoint();
				double xValue = points.getXValueOfDataPoint();
				double yValue = points.getYValueOfDataPoint();
				double zValue = points.getZValueOfDataPoint();
				String stringToWrite = time + " " + xValue + " " + yValue + " " + zValue + System.lineSeparator();
				FileUtils.writeStringToFile(output, stringToWrite, enc, true);
			}
			catch (IOException e) {
				System.out.println("Error in for loop drawMain()");
			}
		}		
	}
	
	/////////////////////////////
	/// Experiments for Chart ///
	/////////////////////////////
	
	public void updateValuesToDrawForFloat(Drone drone) {		
		if (firstIterationToDraw) {
			//delete the file of the previous run of the program
			File invoer = new File("invoerOurTB.txt");
			 if (invoer.exists()){
			     invoer.delete();
			 }  
			 
			startTime=System.currentTimeMillis();
			firstIterationToDraw = false;
		}
		
		long currentTime = System.currentTimeMillis();
		double timeDiff = (currentTime - startTime);
		RealVector position = drone.getWorldPosition();
		//RealVector speed = drone.getVelocity();
		DataPointsForPosition point = new DataPointsForPosition(timeDiff, position.getEntry(0), position.getEntry(1), position.getEntry(2));
		addPointToListOfDataPoints(point);
	}
	
    public static ArrayList<DataPointsForPosition> getAllDataPoints(){
    	return allDataPointsPosition;
    }
    
    public void addPointToListOfDataPoints(DataPointsForPosition point) {
    	allDataPointsPosition.add(point);
    }
}
