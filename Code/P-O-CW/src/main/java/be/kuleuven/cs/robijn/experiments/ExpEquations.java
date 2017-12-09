package be.kuleuven.cs.robijn.experiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import be.kuleuven.cs.robijn.testbed.Tuple;

//import java.io.IOException;
//
//import org.jfree.chart.ChartFactory; 
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.data.xy.XYDataset; 
//import org.jfree.ui.ApplicationFrame; 
//import org.jfree.ui.RefineryUtilities;
//
//import be.kuleuven.cs.robijn.testbed.VirtualTestbed;



public class ExpEquations {
	
	public static void drawMain(String type) {
		File output = new File("invoer.txt");
		String enc = null;
		
		//write the tile to the output file
		try {
			FileUtils.writeStringToFile(output, type + System.lineSeparator(), enc, true);
		} catch (IOException e) {
			System.out.println("Error in drawMain()");
		}
		
		//write the points (time, value) to the output file
		ArrayList<Tuple> allPoints = Tuple.getAllTuples();
		for (Tuple points:allPoints) {
			try {
				double time = points.getTimeOfTuple();
				double value = points.getValueOfTuple();
				String stringToWrite = time + " " + value + System.lineSeparator();
				FileUtils.writeStringToFile(output, stringToWrite, enc, true);
			}
			catch (IOException e) {
				System.out.println("Error in for loop drawMain()");
			}
		}
	}
	
//	private static final long serialVersionUID = 1L; //TODO waarom????
//
//	public ExpEquations(final String title, String type) throws IOException {
//		super(title);
//		final XYDataset dataset = createDataset( );         
//		final JFreeChart chart = createChart( dataset, type );         
//	    final ChartPanel chartPanel = new ChartPanel( chart );         
//	    chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 315 ) ); //16 by 9 aspect ratio 
//	    chartPanel.setMouseZoomable( true , false );         
//	    setContentPane( chartPanel );
//	}
//
//	private JFreeChart createChart(XYDataset dataset,String type) throws IOException {
//		JFreeChart timeChart =  ChartFactory.createTimeSeriesChart(             
//			"Convergence of " + type + " from Autopilot", 
//	    	"Seconds",              
//	    	"Value of " + type,              
//	    	dataset,             
//	    	false,              
//	    	false,              
//	    	false);
//		
//		//save the image
////		int width = 560;   // Width of the image
////	    int height = 315;  // Height of the image
////	    File timeChartImage = new File( "TimeChart.jpeg" ); 
////	    ChartUtilities.saveChartAsJPEG( timeChartImage, timeChart, width, height );
//	    return timeChart;
//	}
//
//	private XYDataset createDataset() {
//			return VirtualTestbed.createDatasetForChart();
//	   }
//	
//   public static void drawMain(String type) {
//	    final String title = "Convergence of Equations from Autopilot";         
//	    ExpEquations demo;
//		try {
//			demo = new ExpEquations( title , type);
//		    demo.pack( );         
//		    RefineryUtilities.positionFrameRandomly( demo );         
//		    demo.setVisible( true );
//		} catch (IOException e) {
//			// do nothing
//		}         
//   }
	
}
