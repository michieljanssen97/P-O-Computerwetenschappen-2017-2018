package be.kuleuven.cs.robijn.testbed.renderer.bmfont;

import be.kuleuven.cs.robijn.common.WorldObject;

import java.awt.*;
import java.util.ArrayList;

public class Label3D extends WorldObject {
    private String text = "";
    private Font font;
    private ArrayList<Color> colors = new ArrayList<>(0);
    public static final Color DEFAULT_COLOR = Color.BLACK;
    public static final int MAX_COLORS = 256;

    public Label3D(){}

    public Label3D(String text){
        setText(text);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if(text == null){
            throw new IllegalArgumentException("text cannot be null");
        }
        this.text = text;
    }

    public Font getFont() {
        if(font == null){
            font = Font.loadDefaultFont();
        }
        return font;
    }

    public void setFont(Font font) {
        if(font == null){
            throw new IllegalArgumentException("font cannot be null");
        }
        this.font = font;
    }

    public Color getColor(int index){
        if(index < 0 || index >= MAX_COLORS){
            throw new IndexOutOfBoundsException("index must be in [0;"+MAX_COLORS+"]");
        }

        if(index >= colors.size()){
            return DEFAULT_COLOR;
        }else{
            return colors.get(index);
        }
    }

    public void setColor(int index, Color color){
        if(index < 0 || index >= MAX_COLORS){
            throw new IndexOutOfBoundsException("index must be in [0;"+MAX_COLORS+"]");
        }

        if(index >= colors.size()){
            colors.ensureCapacity(index+1);
            for(int i = colors.size(); i < index; i++){
                colors.add(DEFAULT_COLOR);
            }
            colors.add(color);
        }else{
            colors.set(index, color);
        }
    }

    public void setColors(int startI, int length, Color color){
        for (int i = startI; i < startI+length; i++){
            setColor(i, color);
        }
    }
}
