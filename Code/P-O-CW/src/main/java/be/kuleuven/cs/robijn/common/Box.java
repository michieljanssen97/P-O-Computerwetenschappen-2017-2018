package be.kuleuven.cs.robijn.common;

import java.awt.*;
import java.util.ArrayList;

public class Box extends WorldObject {
	private Color color = Color.RED;
	
	public Box() {}

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
