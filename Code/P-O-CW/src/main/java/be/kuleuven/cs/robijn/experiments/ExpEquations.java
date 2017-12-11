package be.kuleuven.cs.robijn.experiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import be.kuleuven.cs.robijn.common.Box;
import be.kuleuven.cs.robijn.common.Drone;

public class ExpEquations {
	
	private static ArrayList<DataPointsForChart> allDataPoints = new ArrayList<DataPointsForChart>();
	private long startTime = 0;
	private boolean firstIterationToDraw = true;
	
	public void drawMain(String type) {
		File output = new File("invoer.txt");
		String enc = null;
		
		//write the tile to the output file
		try {
			FileUtils.writeStringToFile(output, type + System.lineSeparator(), enc, true);
		} catch (IOException e) {
			System.out.println("Error in drawMain()");
		}
		
		//write the points (time, value) to the output file
		ArrayList<DataPointsForChart> allPoints = getAllDataPoints();
		for (DataPointsForChart points:allPoints) {
			try {
				double time = points.getTimeOfDataPoint();
				double value = points.getValueOfDataPoint();
				String stringToWrite = time + " " + value + System.lineSeparator();
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
	
	public void updateValuesToDrawForFloat(String type, Box box, Drone drone) {
		float value = (float) getAnglesDifference(type,box, drone); //x-value
	
		
		if (firstIterationToDraw) {
			//delete the file of the previous run of the program
			File invoer = new File("invoer.txt");
			 if (invoer.exists()){
			     invoer.delete();
			 }  
			 
			startTime=System.currentTimeMillis();
			firstIterationToDraw = false;
		}
		
		long currentTime = System.currentTimeMillis();
		double timeDiff = (currentTime - startTime);
		DataPointsForChart point = new DataPointsForChart(timeDiff,value);
		addPointToListOfDataPoints(point);
}
	
	/**
	 * Get the angles from the current place of the plane to the closest cube
	 * @param inputs
	 * @return
	 */
	public double getAnglesDifference(String type, Box box, Drone drone) throws IllegalArgumentException {
//		double precision = 0; //minimum difference in degrees to be significant
		RealVector droneCo = drone.getWorldPosition();
		RealVector boxCo = box.getWorldPosition();
		
		//Vector containing the differences in x-,y- and z-coordinate between the drone and the cube
		RealVector distanceVector= new ArrayRealVector(new double[] {
				Math.abs(droneCo.getEntry(0) - boxCo.getEntry(0)),
				Math.abs(droneCo.getEntry(1) - boxCo.getEntry(1)),
				Math.abs(droneCo.getEntry(2) - boxCo.getEntry(2))
				},false);
		
		if (type == "heading") {
			double angle = Math.atan(distanceVector.getEntry(0) / Math.abs(distanceVector.getEntry(2)));
			double heading = drone.getHeading();
			if (heading > Math.PI) {
				heading -= 2*Math.PI;
			}
			double diffAngle = angle - heading;
//			if (diffAngle <= precision) {
//				return 0;
//			}
			return diffAngle;
		}
		
		else if (type == "pitch") {
			double angle = Math.atan(distanceVector.getEntry(1) / Math.abs(distanceVector.getEntry(2))); 
			double pitch = drone.getPitch();
			if (pitch > Math.PI) {
				pitch -= 2*Math.PI;
			}
			double diffAngle =  angle - pitch;
//			if (diffAngle <= precision) {
//				return 0;
//			}
			return diffAngle;
		}
		
		//else:
		throw new IllegalArgumentException();

	}	
	
    public static ArrayList<DataPointsForChart> getAllDataPoints(){
    	return allDataPoints;
    }
    
    public void addPointToListOfDataPoints(DataPointsForChart point) {
    	allDataPoints.add(point);
    }
    
}
