package be.kuleuven.cs.robijn.common;

import org.apache.commons.math3.linear.ArrayRealVector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BoxFileLoader {
    private BoxFileLoader(){ }

    /**
     * Reads all ASCII lines of the supplied InputStream and returns a list of boxes,
     * each defines by a line in the stream as follows:
     *
     * 1.000000 2.000000 3.000000
     *     ^        ^        ^
     *     X        Y        Z
     *
     * @param in the ASCII InputStream to read from.
     * @throws IOException thrown when the inputstream contains invalid data, or when reading failed.
     */
    public static List<Box> load(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));

        ArrayList<Box> boxes = new ArrayList<>();
        int i = 0;
        String line;
        while((line = reader.readLine()) != null){
            i++;
            line = line.trim(); //Remove extra whitespace at beginning and end of line
            String[] coordinateStrings = line.split(" "); //Split line into three strings, each with a coordinate
            if(coordinateStrings.length != 3){ //Check that there are 3 coordinates on this line
                throw new IOException("File layout error on line "+i);
            }

            float x, y, z;
            try {
                x = Float.parseFloat(coordinateStrings[0]);
                y = Float.parseFloat(coordinateStrings[1]);
                z = Float.parseFloat(coordinateStrings[2]);
            }catch (NumberFormatException ex){
                throw new IOException("Invalid coordinate value on line "+i, ex);
            }

            Box newBox = new Box();
            newBox.setRelativePosition(new ArrayRealVector(new double[]{x, y, z}, false));
            boxes.add(newBox);
        }
        return boxes;
    }
}
