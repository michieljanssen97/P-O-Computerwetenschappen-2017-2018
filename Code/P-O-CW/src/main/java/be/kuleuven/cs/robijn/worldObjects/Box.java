package be.kuleuven.cs.robijn.worldObjects;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.awt.*;
import java.util.ArrayList;

public class Box extends WorldObject {
	private Color color = Color.RED;
	
	public Box() {
		this.setScale(new ArrayRealVector(new double[]{5, 5, 5}, false));
	}

	/**
	 * Returns the color of this box.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color of this box. Each side of the box will get the correct shade based on this color.
	 * @param color the new color of this box. Must not be null.
	 */
	public void setColor(Color color){

		if(color == null){
			throw new IllegalArgumentException("color cannot be null");
		}
		this.color = color;
	}

}
