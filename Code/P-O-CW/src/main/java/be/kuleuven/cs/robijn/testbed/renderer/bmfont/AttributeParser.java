package be.kuleuven.cs.robijn.testbed.renderer.bmfont;

public class AttributeParser {
    public static AttributesMap parse(String attributesLine){
        AttributeParser parser = new AttributeParser();
        parser.parseString(attributesLine);
        return parser.entries;
    }

    private final WaitingForKeyState waitingForKey = new WaitingForKeyState();
    private final ReadingKeyState readingKey = new ReadingKeyState();
    private final ReadingValueState readingValue = new ReadingValueState();

    private AttributeParserState currentState = waitingForKey;
    private AttributesMap entries = new AttributesMap();
    private StringBuilder curKey;
    private StringBuilder curValue;
    private boolean textQuoteMode;

    private AttributeParser(){ }

    private void parseString(String str){
        for (char c : str.toCharArray()) {
            currentState = currentState.parseCharacter(c);
        }
        currentState.parseCharacter(' '); //Flush state
    }

    private abstract class AttributeParserState {
        abstract AttributeParserState parseCharacter(char c);
    }

    private class WaitingForKeyState extends AttributeParserState {
        AttributeParserState parseCharacter(char c) {
            switch (c){
                case ' ':
                case '\t':
                    return waitingForKey;
                default:
                    curKey = new StringBuilder(Character.toString(c));
                    return readingKey;
            }
        }
    }

    private class ReadingKeyState extends AttributeParserState {
        AttributeParserState parseCharacter(char c) {
            switch (c){
                case '=':
                    curValue = new StringBuilder();
                    return readingValue;
                default:
                    curKey.append(c);
                    return readingKey;
            }
        }
    }

    private class ReadingValueState extends AttributeParserState {
        AttributeParserState parseCharacter(char c) {
            switch (c){
                case '"':
                    textQuoteMode = !textQuoteMode;
                    if(textQuoteMode){
                        return readingValue;
                    }
                case '\t':
                case ' ':
                    if(!textQuoteMode){
                        entries.put(curKey.toString(), curValue.toString());
                        return waitingForKey;
                    }
                default:
                    curValue.append(c);
                    return readingValue;
            }
        }
    }
}
