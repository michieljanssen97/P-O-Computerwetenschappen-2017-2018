package be.kuleuven.cs.robijn.testbed;

import java.util.Random;

import org.apache.commons.math3.linear.ArrayRealVector;

import be.kuleuven.cs.robijn.common.Box;
public class WorldGenerator {
	
	/**
	 * Generate N boxes in the given testbed
	 * @param vTestBed
	 *        The testbed for which to generate the boxes
	 */
	public static void generateBoxes(VirtualTestbed vTestBed) {
		int amountBoxes = 5;
		
		for(int i = 1; i <= amountBoxes; i++) {
			Box box = new Box();
			
			float x = Float.MAX_VALUE;
			float y = Float.MAX_VALUE;
			
			
			//dimensions of the rectangular cuboid
			int minx = -10;
			int miny = 0;
			int max = 10;
			
			while (Math.sqrt(Math.pow(x,2) + Math.pow(x,2)) > 10) { //given in the assignment
				Random rand1 = new Random();
				Random rand2 = new Random();
				
				x = rand1.nextFloat() * (max - minx) + minx;
				y = rand2.nextFloat() * (max - miny) + miny;
			}
			
			float z = i * -40; //given in the assignment
			box.setRelativePosition(new ArrayRealVector(new double[] {x, y, z}, false));
			//System.out.println(box.getRelativePosition().getEntry(0)+" "+box.getRelativePosition().getEntry(1)+" "+box.getRelativePosition().getEntry(2));
			vTestBed.addChild(box);
		}
		
		vTestBed.randomCubesGenerated = true; //Notify the Testbed that there are random generated cubes, so no input file must be read
	}
	
}