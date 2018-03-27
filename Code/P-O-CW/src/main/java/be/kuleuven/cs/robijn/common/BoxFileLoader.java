package be.kuleuven.cs.robijn.common;

import org.apache.commons.math3.linear.ArrayRealVector;

import be.kuleuven.cs.robijn.worldObjects.Box;

import java.io.*;
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
     * As a backwards-compatible extension to the specification, any text on a line after the # sign
     * is ignored to allow for comments. Blank lines are ignored aswell.
     *
     * @param in the ASCII InputStream to read from.
     * @throws IOException thrown when the inputstream contains invalid data, or when reading failed.
     * @throws IllegalArgumentException thrown when the inputstream is null
     */
    public static List<Box> load(InputStream in) throws IOException {
        if(in == null){
            throw new IllegalArgumentException("'in' must not be null");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));

        ArrayList<Box> boxes = new ArrayList<>();
        int i = 0;
        String line;
        while((line = reader.readLine()) != null){
            i++;
            line = line.trim(); //Remove extra whitespace at beginning and end of line

            if(line.contains("#")){
                line = line.split("#")[0].trim();
            }
            if(line.equals("")){
                continue;
            }

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

    /**
     * Write the specified list of boxes to a box setup text file
     * @param boxes the list of boxes to save
     * @param out the outputstream to write the box setup file data to.
     * @throws IOException thrown when an exception occurs during the writing to the stream.
     * @throws IllegalArgumentException thrown when 'boxes' or 'out' is null.
     */
    public static void write(List<Box> boxes, OutputStream out) throws IOException{
        if(boxes == null || out == null){
            throw new IllegalArgumentException("'boxes' and 'out' must not be null");
        }

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        for (Box box : boxes){
            double x = box.getRelativePosition().getEntry(0);
            double y = box.getRelativePosition().getEntry(1);
            double z = box.getRelativePosition().getEntry(2);
            writer.println(x + " " + y + " " + z);
        }
        writer.flush();
    }
}
