package be.kuleuven.cs.robijn.testbed.renderer.bmfont;

import be.kuleuven.cs.robijn.common.Resources;

import java.util.*;

public class BMFontFileReader {
    public static BMFontFile readFromResources(String resourceName){
        BMFontFile result = new BMFontFile();
        result.pages = new ArrayList<>();

        Scanner scanner = new Scanner(Resources.getResourceStream(resourceName));
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();

            int tagEndIndex = line.indexOf(" ");
            String tag = tagEndIndex != -1 ? line.substring(0, tagEndIndex) : line;

            AttributesMap attributes = AttributeParser.parse(line.substring(tagEndIndex+1));

            switch (tag){
                case "info":
                    result.info = readInfo(attributes);
                    break;
                case "common":
                    result.common = readCommon(attributes);
                    break;
                case "page":
                    result.pages.add(readPage(attributes));
                    break;
                case "chars":
                    result.characters = readChars(attributes, scanner);
                    break;
                case "kernings":
                    result.kernings = readKernings(attributes, scanner);
                    break;
            }
        }

        return result;
    }

    private static BMFontFile.Info readInfo(AttributesMap attributes) {
        BMFontFile.Info result = new BMFontFile.Info();

        result.faceName = attributes.getString("face");
        result.size = attributes.getInteger("size");
        result.isBold = attributes.getBoolean("bold");
        result.isItalic = attributes.getBoolean("italic");
        result.charset = attributes.getString("charset");
        result.isUnicode = attributes.getBoolean("unicode");
        result.stretchHeight = attributes.getInteger("stretchH");
        result.isSmooth = attributes.getBoolean("smooth");
        result.supersamplingLevels = attributes.getInteger("aa");
        result.outlineThickness = attributes.tryGetInteger("outline").orElse(0);

        int[] padding = attributes.getIntegerArray("padding");
        if(padding.length != 4){
            throw new IllegalStateException("Invalid padding length "+padding.length+". Expected 4.");
        }

        result.paddingTop = padding[0];
        result.paddingRight = padding[1];
        result.paddingBottom = padding[2];
        result.paddingLeft = padding[3];

        int[] spacing = attributes.getIntegerArray("spacing");
        if(spacing.length != 2){
            throw new IllegalStateException("Invalid spacing length "+spacing.length+". Expected 4.");
        }
        result.spacingHorizontal = spacing[0];
        result.spacingVertical = spacing[1];

        return result;
    }

    private static BMFontFile.Common readCommon(AttributesMap attributes) {
        BMFontFile.Common result = new BMFontFile.Common();

        result.lineHeight = attributes.getInteger("lineHeight");
        result.base = attributes.getInteger("base");
        result.scaleWidth = attributes.getInteger("scaleW");
        result.scaleHeight = attributes.getInteger("scaleH");
        result.pages = attributes.getInteger("pages");
        result.packed = attributes.getBoolean("packed");
        result.alphaChannel = attributes.tryGetInteger("alphaChnl").orElse(0);
        result.redChannel = attributes.tryGetInteger("redChnl").orElse(0);
        result.greenChannel = attributes.tryGetInteger("greenChnl").orElse(0);
        result.blueChannel = attributes.tryGetInteger("blueChnl").orElse(0);

        return result;
    }

    private static BMFontFile.Page readPage(AttributesMap attributes) {
        BMFontFile.Page result = new BMFontFile.Page();

        result.id = attributes.getInteger("id");
        result.fileName = attributes.getString("file");

        return result;
    }

    private static HashMap<Integer, BMFontFile.Char> readChars(AttributesMap attributes, Scanner scanner) {
        int count = attributes.getInteger("count");
        HashMap<Integer, BMFontFile.Char> characters = new HashMap<>(count);

        for(int i = 0; i < count; i++){
            String line = scanner.nextLine();
            int tagEndIndex = line.indexOf(" ");
            AttributesMap charAttr = AttributeParser.parse(line.substring(tagEndIndex+1));

            BMFontFile.Char cData = readChar(charAttr);
            characters.put(cData.id, cData);
        }

        return characters;
    }

    private static BMFontFile.Char readChar(AttributesMap attributes) {
        BMFontFile.Char c = new BMFontFile.Char();

        c.id = attributes.getInteger("id");
        c.x = attributes.getInteger("x");
        c.y = attributes.getInteger("y");
        c.width = attributes.getInteger("width");
        c.height = attributes.getInteger("height");
        c.xOffset = attributes.getInteger("xoffset");
        c.yOffset = attributes.getInteger("yoffset");
        c.xAdvance = attributes.getInteger("xadvance");
        c.page = attributes.getInteger("page");
        c.channel = attributes.getInteger("chnl");

        return c;
    }

    private static HashMap<CharPair, BMFontFile.Kerning> readKernings(AttributesMap attributes, Scanner scanner) {
        int count = attributes.getInteger("count");
        HashMap<CharPair, BMFontFile.Kerning> kernings = new HashMap<>(count);

        for(int i = 0; i < count; i++){
            String line = scanner.nextLine();
            int tagEndIndex = line.indexOf(" ");
            AttributesMap charAttr = AttributeParser.parse(line.substring(tagEndIndex+1));

            BMFontFile.Kerning kerning = readKerning(charAttr);
            kernings.put(new CharPair(kerning.firstCharId, kerning.secondCharId), kerning);
        }

        return kernings;
    }

    private static BMFontFile.Kerning readKerning(AttributesMap attributes) {
        BMFontFile.Kerning kerning = new BMFontFile.Kerning();

        kerning.firstCharId = attributes.getInteger("first");
        kerning.secondCharId = attributes.getInteger("second");
        kerning.amount = attributes.getInteger("amount");

        return kerning;
    }
}
