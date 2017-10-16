package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.common.math.Vector3f;

public interface FrameBuffer {
    void readPixels(byte[] data);
}
