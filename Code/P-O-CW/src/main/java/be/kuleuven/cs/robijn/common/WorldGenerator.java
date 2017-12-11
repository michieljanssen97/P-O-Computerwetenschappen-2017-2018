package be.kuleuven.cs.robijn.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.linear.ArrayRealVector;

public class WorldGenerator {
	/**
	 * Generate N boxes in the given testbed
	 */
	public static List<Box> generateBoxes(WorldGeneratorSettings settings) {
		ArrayList<Box> boxes = new ArrayList<>(settings.getBoxCount());
		Random rand = new Random();

		for(int i = 1; i <= settings.getBoxCount(); i++) {
			Box box = new Box();

			double radius = 10.0; //radius around z-axis in which cubes can be generated
			double x = ((rand.nextDouble() * 2.0) - 1.0) * radius;
			double maxY = Math.sqrt(Math.pow(radius, 2) - Math.pow(x, 2));
			double y = ((rand.nextDouble() * 2.0) - 1.0) * maxY;
			double z = i * -40; //given in the assignment

			box.setRelativePosition(new ArrayRealVector(new double[] {x, y, z}, false));
			if(settings.areColorsRandomized()){
				box.setColor(ColorGenerator.random());
			}
			boxes.add(box);
		}
		return boxes;
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