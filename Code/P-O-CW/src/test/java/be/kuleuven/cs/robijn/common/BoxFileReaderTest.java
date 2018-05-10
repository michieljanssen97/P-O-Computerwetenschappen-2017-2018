package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.common.math.VectorMath;
import be.kuleuven.cs.robijn.worldObjects.Box;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoxFileReaderTest {

    @Test
    public void testEmpty() throws IOException {
        String input = "";
        try(InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII))){
            List<Box> boxes = BoxFileLoader.load(stream);
            assertEquals(0, boxes.size());
        }
    }

    @Test
    public void testComment() throws IOException {
        String input = " # Dit is een test\n";
        try(InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII))){
            List<Box> boxes = BoxFileLoader.load(stream);
            assertEquals(0, boxes.size());
        }
    }

    @Test
    public void testBlankLine() throws IOException {
        String input = "\n\n";
        try(InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII))){
            List<Box> boxes = BoxFileLoader.load(stream);
            assertEquals(0, boxes.size());
        }
    }

    @Test
    public void test3Boxes() throws IOException {
        //Define test vectors
        RealVector[] vectors = new RealVector[]{
                new ArrayRealVector(new double[]{   1.0,    2.5,  5.0}, false),
                new ArrayRealVector(new double[]{  -2.0,   -3.0, -4.0}, false),
                new ArrayRealVector(new double[]{1324.0, 4321.0,  0.0}, false),
        };

        //Construct input
        StringBuilder builder = new StringBuilder();
        for(RealVector vector : vectors){
            builder.append(vector.getEntry(0)).append(' ')
                    .append(vector.getEntry(1)).append(' ')
                    .append(vector.getEntry(2)).append('\n');
        }
        String input = builder.toString();

        //Test loader
        try(InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII))){
            List<Box> boxes = BoxFileLoader.load(stream);
            for (int i = 0; i < vectors.length; i++){
                boolean vectorsEqual = VectorMath.fuzzyEquals(boxes.get(i).getRelativePosition(), vectors[i]);
                assertTrue(vectorsEqual);
            }
        }
    }

    @Test
    public void testInvalidInput_letters() throws IOException {
        String input = "1.0000 abc 2.0000\n";

        //Test loader
        assertThrows(IOException.class, () -> {
            try(InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII))){
                List<Box> boxes = BoxFileLoader.load(stream);
            }
        });
    }

    @Test
    public void testInvalidInput_missing_coordinate() throws IOException {
        String input = "1.0000 2.0000\n";

        //Test loader
        assertThrows(IOException.class, () -> {
            try(InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII))){
                List<Box> boxes = BoxFileLoader.load(stream);
            }
        });
    }

    @Test
    public void testInputNull() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            BoxFileLoader.load(null);
        });
    }

    @Test
    public void testWriting() throws IOException {
        //Define test boxes
        List<Box> boxes = new ArrayList<>();
        boxes.add(new Box());
        boxes.add(new Box());
        boxes.add(new Box());
        boxes.get(0).setRelativePosition(new ArrayRealVector(new double[]{   1.0,    2.5,  5.0}, false));
        boxes.get(1).setRelativePosition(new ArrayRealVector(new double[]{  -2.0,   -3.0, -4.0}, false));
        boxes.get(2).setRelativePosition(new ArrayRealVector(new double[]{1324.0, 4321.0,  0.0}, false));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BoxFileLoader.write(boxes, out);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())))) {
            String line1 = reader.readLine();
            String line2 = reader.readLine();
            String line3 = reader.readLine();

            assertEquals("1.0 2.5 5.0", line1);
            assertEquals("-2.0 -3.0 -4.0", line2);
            assertEquals("1324.0 4321.0 0.0", line3);
        }
    }

    @Test
    public void testWriting_BoxesNull() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            BoxFileLoader.write(null, new ByteArrayOutputStream());
        });
    }

    @Test
    public void testWriting_OutputNull() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> {
            BoxFileLoader.write(new ArrayList<>(), null);
        });
    }
}
