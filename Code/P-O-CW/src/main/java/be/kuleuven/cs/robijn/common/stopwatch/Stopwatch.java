package be.kuleuven.cs.robijn.common.stopwatch;

public interface Stopwatch {
	void reset();
    void tick();
    double getSecondsSinceStart();
    double getSecondsSinceLastUpdate();
    void setPaused(boolean paused);
    boolean isPaused();
    void setSpeedMultiplier(double speed);
    double getSpeedMultiplier();
}
