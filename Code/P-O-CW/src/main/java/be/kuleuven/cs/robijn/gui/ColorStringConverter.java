package be.kuleuven.cs.robijn.gui;

import javafx.util.StringConverter;

import java.awt.*;

public class ColorStringConverter extends StringConverter<Color>{
    @Override
    public String toString(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    @Override
    public Color fromString(String s) {
        if(!s.startsWith("#") || s.length() != 7){
            return Color.RED;
        }
        try {
            int red = Integer.decode("0x" + s.substring(1, 3));
            int green = Integer.decode("0x" + s.substring(3, 5));
            int blue = Integer.decode("0x" + s.substring(5, 7));
            return new Color(red, green, blue);
        }catch (NumberFormatException ex){
            return Color.RED;
        }
    }
}
