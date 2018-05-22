package be.kuleuven.cs.robijn.testbed.renderer.bmfont;

import java.util.HashMap;
import java.util.Optional;

public class AttributesMap extends HashMap<String, String> {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2311857764553985679L;

	public String getString(String tag){
        String value = get(tag);
        if(value == null) {
            throw new IllegalStateException("Missing string attribute '"+tag+"'");
        }

        return value;
    }

    public int getInteger(String tag){
        String value = get(tag);
        if(value != null) {
            try {
                return Integer.parseInt(value);
            }catch (NumberFormatException ex){
                throw new IllegalStateException("Invalid integer attribute value '"+value+"' for '"+tag+"'");
            }
        }
        throw new IllegalStateException("Missing integer attribute '"+tag+"'");
    }

    public Optional<Integer> tryGetInteger(String tag){
        String value = get(tag);
        if(value != null) {
            try {
                return Optional.of(Integer.parseInt(value));
            }catch (NumberFormatException ex){ }
        }
        return Optional.empty();
    }

    public int[] getIntegerArray(String tag){
        String value = get(tag);
        if(value != null) {
            String[] parts = value.split(",");
            int[] values = new int[parts.length];
            for(int i = 0; i < parts.length; i++){
                try {
                    values[i] = Integer.parseInt(parts[i]);
                }catch (NumberFormatException ex){
                    throw new IllegalStateException("Invalid int[] attribute value '"+value+"' for '"+tag+"'");
                }
            }
            return values;
        }
        throw new IllegalStateException("Missing int[] attribute '"+tag+"'");
    }

    public boolean getBoolean(String tag){
        String value = get(tag);
        if(value != null) {
            try {
                int intValue = Integer.parseInt(value);
                if(intValue == 0){
                    return false;
                }else if(intValue == 1){
                    return true;
                }
            }catch (NumberFormatException ex){
                throw new IllegalStateException("Invalid boolean attribute '"+tag+"'");
            }
        }
        throw new IllegalStateException("Missing boolean attribute '"+tag+"'");
    }
}
