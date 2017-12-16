package be.kuleuven.cs.robijn.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;

public class WorldGenerator {
	/**
	 * Generate N boxes in the given testbed
	 */
	public static List<Box> generateBoxes(WorldGeneratorSettings settings) {
		ArrayList<Box> boxes = new ArrayList<>(settings.getBoxCount());
        NormalDistribution dist = new NormalDistribution(0, 1d/2.575829303d); //99% in [-1; 1]

		for(int i = 1; i <= settings.getBoxCount(); i++) {
			Box box = new Box();

			double radius = 10.0; //radius around z-axis in which cubes can be generated
			double x = getBoundedMultiplier(dist) * radius;
			double maxY = Math.sqrt(Math.pow(radius, 2) - Math.pow(x, 2));
			double y = getBoundedMultiplier(dist) * maxY;
			double z = i * -40; //given in the assignment

			box.setRelativePosition(new ArrayRealVector(new double[] {x, y, z}, false));
			if(settings.areColorsRandomized()){
				box.setColor(ColorGenerator.random());
			}
			boxes.add(box);
		}
		return boxes;
	}

    /**
     * Returns a sample from the provided normal distribution, guaranteed to have an absolute value less than 1.
     * This makes the normal distribution slightly inaccurate. (depending on % of values outside of [-1;1])
     */
	private static double getBoundedMultiplier(NormalDistribution dist){
	    double factor;
	    while(Math.abs(factor = dist.sample()) > 1); //99% chance of false
	    return factor;
    }

	public static class WorldGeneratorSettings {
		private int boxCount = 5;
		private boolean randomizeColors = true;

		public int getBoxCount() {
			return boxCount;
		}

		public void setBoxCount(int boxCount) {
			this.boxCount = boxCount;
		}

		public boolean areColorsRandomized() {
			return randomizeColors;
		}

		public void setRandomizeColors(boolean randomizeColors) {
			this.randomizeColors = randomizeColors;
		}
	}
}