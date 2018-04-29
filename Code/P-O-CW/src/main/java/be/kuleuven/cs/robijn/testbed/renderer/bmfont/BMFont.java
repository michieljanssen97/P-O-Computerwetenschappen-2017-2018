package be.kuleuven.cs.robijn.testbed.renderer.bmfont;

import be.kuleuven.cs.robijn.common.Font;
import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.testbed.renderer.Texture;

import java.awt.image.BufferedImage;

public class BMFont implements Font {
    /**
     * Loads a font from the application resources.
     * This function assumes a .fnt and .png file with the specified name are in the /fonts/ folder
     * @param name the name of the font files (without extensions)
     * @return the font
     */
    public static BMFont loadFont(String name){
        BMFontFile fontInfo = BMFontFileReader.readFromResources("/fonts/"+name+".fnt");
        BufferedImage fontAtlas = Resources.loadImageResource("/fonts/"+name+".png");
        Texture atlasTexture = Texture.load(fontAtlas);

        return new BMFont(fontInfo, atlasTexture);
    }

    public static BMFont loadDefaultFont(){
        return loadFont("opensans_outline");
    }

    private final BMFontFile info;
    private final Texture atlas;
    private final StringLayoutEngine layoutEngine;
    private final StringRenderer renderer;

    BMFont(BMFontFile info, Texture atlas){
        this.info = info;
        this.atlas = atlas;
        this.layoutEngine = new StringLayoutEngine(this);
        this.renderer = new StringRenderer();
    }

    BMFontFile getInfo() {
        return info;
    }

    Texture getAtlas() {
        return atlas;
    }

    public RenderableString layoutString(String text){
        return layoutEngine.layoutString(text);
    }

    public StringRenderer getRenderer(){
        return renderer;
    }

    @Override
    public void close() {
        renderer.close();
    }
}
