package be.kuleuven.cs.robijn.common.stopwatch;

public interface Stopwatch {
    void tick();
    double getSecondsSinceStart();
    double getSecondsSinceLastUpdate();
    void setPaused(boolean paused);
    boolean isPaused();
}
