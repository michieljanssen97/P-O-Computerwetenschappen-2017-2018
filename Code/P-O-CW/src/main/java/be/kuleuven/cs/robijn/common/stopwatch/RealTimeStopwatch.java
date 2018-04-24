package be.kuleuven.cs.robijn.common.stopwatch;

public class RealTimeStopwatch implements Stopwatch {
    private long lastTickTimestamp = System.currentTimeMillis();
    private long totalMilliSeconds;

    private double timeMultiplier = 1.0; //Each second in real time is multiplied by this value to get the actual value

    private boolean paused = false;

    @Override
    public void tick() {
        if(!paused){
            long now = System.currentTimeMillis();
            totalMilliSeconds += ((double)(now - lastTickTimestamp)) * timeMultiplier;
            lastTickTimestamp = now;
        }
    }

    @Override
    public double getSecondsSinceLastUpdate() {
        return (lastTickTimestamp/1000d) * timeMultiplier;
    }

    @Override
    public double getSecondsSinceStart() {
        return totalMilliSeconds /1000d;
    }

    @Override
    public void setPaused(boolean newPaused) {
        if(this.paused && !newPaused){
            lastTickTimestamp = System.currentTimeMillis();
        }
        this.paused = newPaused;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void setSpeedMultiplier(double speed) {
        this.timeMultiplier = speed;
    }

    @Override
    public double getSpeedMultiplier() {
        return timeMultiplier;
    }
}
