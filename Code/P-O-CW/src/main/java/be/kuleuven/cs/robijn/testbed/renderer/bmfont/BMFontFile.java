package be.kuleuven.cs.robijn.testbed.renderer.bmfont;

import java.util.HashMap;
import java.util.List;

public class BMFontFile {
    public static class Info {
        /** This is the name of the true type font. */
        public String faceName;
        /** The name of the OEM charset used (when not unicode). */
        public String charset;
        /** The size of the true type font. */
        public int size;
        /** The font height stretch in percentage. 100% means no stretch. */
        public int stretchHeight;
        /** The supersampling level used. 1 means no supersampling was used. */
        public int supersamplingLevels;
        /** The font is bold. */
        public boolean isBold;
        /** The font is italic. */
        public boolean isItalic;
        /** True if it is the unicode charset. */
        public boolean isUnicode;
        /** True if smoothing was turned on. */
        public boolean isSmooth;
        /** The padding for each character. */
        public int paddingTop, paddingRight, paddingBottom, paddingLeft;
        /** The spacing for each character. */
        public int spacingHorizontal, spacingVertical;
        /** The outline thickness for the characters. */
        public int outlineThickness;
    }

    public static class Common {
        /** This is the distance in pixels between each line of text. */
        public int lineHeight;
        /** The number of pixels from the absolute top of the line to the base of the characters. */
        public int base;
        /** The width of the texture, normally used to scale the x pos of the character image. */
        public int scaleWidth;
        /** The height of the texture, normally used to scale the y pos of the character image. */
        public int scaleHeight;
        /** The number of texture pages included in the font. */
        public int pages;
        /**
         * True if the monochrome characters have been packed into each of the texture channels.
         * In this case alphaChannel describes what is stored in each channel.
         */
        public boolean packed;
        /**
         * Set to 0 if the channel holds the glyph data, 1 if it holds the outline,
         * 2 if it holds the glyph and the outline, 3 if its set to zero, and 4 if its set to one.
         */
        public int alphaChannel, redChannel, greenChannel, blueChannel;
    }

    public static class Page {
        /** The page id. */
        public int id;
        /** The texture file name. */
        public String fileName;
    }

    public static class Char {
        /** The character id. */
        public int id;
        /** The left position of the character image in the texture. */
        public int x;
        /** The top position of the character image in the texture. */
        public int y;
        /** The width of the character image in the texture. */
        public int width;
        /** The height of the character image in the texture. */
        public int height;
        /** How much the current position should be offset when copying the image from the texture to the screen. */
        public int xOffset, yOffset;
        /** How much the current position should be advanced after drawing the character. */
        public int xAdvance;
        /** The texture page where the character image is found. */
        public int page;
        /** The texture channel where the character image is found (1 = blue, 2 = green, 4 = red, 8 = alpha, 15 = all channels). */
        public int channel;
    }

    public static class Kerning {
        /** The first character id. */
        public int firstCharId;
        /** The second character id. */
        public int secondCharId;
        /** How much the x position should be adjusted when drawing the second character immediately following the first. */
        public int amount;
    }

    /** Holds information on how the font was generated. */
    public Info info;

    /** Holds information common to all characters. */
    public Common common;

    /** Gives the name of a texture file. */
    public List<Page> pages;

    /** Describes a character in the font. */
    public HashMap<Integer, Char> characters;

    /**
     * The kerning information is used to adjust the distance between certain characters,
     * e.g. some characters should be placed closer to each other than others.
     */
    public HashMap<CharPair, Kerning> kernings;
}
