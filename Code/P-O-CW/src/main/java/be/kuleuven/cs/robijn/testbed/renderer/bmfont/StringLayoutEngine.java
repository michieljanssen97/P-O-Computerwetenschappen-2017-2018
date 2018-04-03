package be.kuleuven.cs.robijn.testbed.renderer.bmfont;

import java.util.ArrayList;
import java.util.List;

public class StringLayoutEngine {
    private final Font font;

    StringLayoutEngine(Font font){
        this.font = font;
    }

    public RenderableString layoutString(String text){
        ArrayList<RenderableString.CharQuad> quads = new ArrayList<>();

        int cursorX = 0;
        int cursorY = font.getInfo().common.base;

        int[] codePoints = text.codePoints().toArray();
        for (int i = 0; i < codePoints.length; i++) {
            int code = codePoints[i];

            if(code == '\n'){
                cursorX = 0;
                cursorY += font.getInfo().common.lineHeight;
                continue;
            }

            BMFontFile.Char c = font.getInfo().characters.get(code);
            if(c == null){
                continue;
            }

            int quadX = cursorX + c.xOffset;
            int quadY = cursorY - font.getInfo().common.base + c.yOffset;

            quads.add(new RenderableString.CharQuad(c, quadX, quadY));

            cursorX += c.xAdvance;

            //Kerning
            if(i+1 < codePoints.length){
                BMFontFile.Kerning k = font.getInfo().kernings.get(new CharPair(code, codePoints[i+1]));
                if(k != null){
                    cursorX += k.amount;
                }
            }
        }

        return new RenderableString(font, normalizeQuads(quads));
    }

    private List<RenderableString.CharQuad> normalizeQuads(List<RenderableString.CharQuad> quads){
        ArrayList<RenderableString.CharQuad> result = new ArrayList<>(quads.size());

        int xOffset = -1 * quads.get(0).getX();

        int minHeight = Integer.MAX_VALUE;
        for (RenderableString.CharQuad quad : quads) {
            minHeight = Math.min(minHeight, quad.getY());
        }
        int yOffset = -1 * minHeight;

        for (RenderableString.CharQuad quad : quads) {
            result.add(new RenderableString.CharQuad(
                    quad.getCharacter(),
                    quad.getX() + xOffset,
                    quad.getY() + yOffset
            ));
        }

        return result;
    }
}
