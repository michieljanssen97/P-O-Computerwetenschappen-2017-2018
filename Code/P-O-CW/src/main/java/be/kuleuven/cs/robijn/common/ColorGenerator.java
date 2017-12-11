package be.kuleuven.cs.robijn.common;

import java.awt.*;

public class ColorGenerator {
    public static Color random(){
        float hue = (float)Math.random();
        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }

    /**
     * Generates the color in the even sequential distribution at index i.
     * Colors in this distribution have a hue with a maximal distance from the previous colors in the sequence.
     * @param i the index in the sequence to get the color of.
     * @return the color at index i in this sequence.
     */
    public static Color sequentialEvenDistribution(int i){
        float hue = getMaximalRadianDistanceSequenceElement(i)/(2f*(float)Math.PI);
        return Color.getHSBColor(hue, 1.0f, 1.0f);
    }

    /**
     * Returns element i from the sequence that is constructed as follows:
     * Given a circle, return the coordinate of the point that has a maximal distance from the n-1 previous points in this sequence.
     * If multiple coordinates are possible, the smallest one is chosen.
     * @param i the index in the sequence
     * @return the coordinate of the point on the circle, in radians
     */
    private static float getMaximalRadianDistanceSequenceElement(int i){
        if(i == 0){
            return 0;
        }

        int powOf2 = roundDownToPowerOfTwo(i);
        int y = i % powOf2;
        int x = (y * 2) + 1;
        return (float)((double)x * Math.PI / (double)powOf2);
    }

    private static int roundDownToPowerOfTwo(int value){
        for(int i = 1 << 30; i>0; i = i >> 1){
            if((value & i) > 0){
                return i;
            }
        }
        return 0;
    }
}
