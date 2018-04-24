package be.kuleuven.cs.robijn.common;

public interface RenderTask {
    boolean isDone();
    void waitUntilFinished();
}
