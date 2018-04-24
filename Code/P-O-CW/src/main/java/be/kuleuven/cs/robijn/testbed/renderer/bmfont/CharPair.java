package be.kuleuven.cs.robijn.testbed.renderer.bmfont;

import java.util.Objects;

public class CharPair {
    private final int c1, c2;

    public CharPair(int c1, int c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public int getChar1() {
        return c1;
    }

    public int getChar2() {
        return c2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CharPair charPair = (CharPair) o;
        return c1 == charPair.c1 &&
                c2 == charPair.c2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(c1, c2);
    }
}
