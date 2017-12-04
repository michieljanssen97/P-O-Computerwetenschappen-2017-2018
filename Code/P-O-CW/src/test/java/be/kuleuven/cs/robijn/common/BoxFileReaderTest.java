package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.common.math.VectorMath;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
}
