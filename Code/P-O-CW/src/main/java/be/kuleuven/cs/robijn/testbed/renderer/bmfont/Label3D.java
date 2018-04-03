package be.kuleuven.cs.robijn.testbed.renderer.bmfont;

import be.kuleuven.cs.robijn.common.WorldObject;

public class Label3D extends WorldObject {
    private String text = "";
    private Font font;

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
}
