package be.kuleuven.cs.robijn.testbed.renderer.bmfont;

import java.util.List;

public class RenderableString {
    public static class CharQuad{
        private final BMFontFile.Char character;
        private final int x, y;

        public CharQuad(BMFontFile.Char character, int x, int y) {
            this.character = character;
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public BMFontFile.Char getCharacter() {
            return character;
        }
    }

    private final BMFont font;
    private final CharQuad[] characters;
    private int width = -1;
    private int height = -1;

    RenderableString(BMFont font, List<CharQuad> characters){
        this.font = font;
        this.characters = characters.toArray(new RenderableString.CharQuad[characters.size()]);
    }

    public BMFont getFont() {
        return font;
    }

    public int getWidth(){
        if(width == -1){
            int leftMostX = Integer.MAX_VALUE;
            for (CharQuad quad : characters) {
                leftMostX = Integer.min(leftMostX, quad.getX());
            }

            int rightMostX = Integer.MIN_VALUE;
            for (CharQuad quad : characters) {
                rightMostX = Integer.max(rightMostX, quad.getX() + quad.character.width);
            }

            width = rightMostX - leftMostX;
        }
        return width;
    }

    public int getHeight(){
        if(height == -1){
            int topMostY = Integer.MAX_VALUE;
            for (CharQuad quad : characters) {
                topMostY = Integer.min(topMostY, quad.getY());
            }

            int bottomMostY = Integer.MIN_VALUE;
            for (CharQuad quad : characters) {
                bottomMostY = Integer.max(bottomMostY, quad.getY() + quad.character.height);
            }

            height = bottomMostY - topMostY;
        }
        return height;
    }

    public CharQuad[] getCharacters() {
        return characters;
    }
}
