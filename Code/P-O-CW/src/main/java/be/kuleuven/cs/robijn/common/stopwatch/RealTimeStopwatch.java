package be.kuleuven.cs.robijn.common.stopwatch;

public class RealTimeStopwatch implements Stopwatch {
    private long simulationStart = -1; //timestamp of the first tick
    private long totalTimeSpentPaused = 0; //total amount of time, between start and last update, that was spent paused (in ms)
    private long lastUpdate = -1; //timestamp of last update
    private long timeSpentPausedSinceLastUpdate = 0; //total amount of time, between last update and now, that was spent paused (in ms)
    private long pauseTime = -1; //Timestamp at which the stopwatch was paused

    private double secondsSinceStart, secondsSinceLastUpdate;
    private boolean paused = false;

    @Override
    public void tick() {
        if(simulationStart == -1 || lastUpdate == -1){
            lastUpdate = simulationStart = System.currentTimeMillis()-5; //-5 so delta t is not zero
        }

        if(!paused){
            long now = System.currentTimeMillis();
            secondsSinceStart = ((double)((now - simulationStart) - totalTimeSpentPaused)/1000f);
            secondsSinceLastUpdate = ((double)((now - lastUpdate) - timeSpentPausedSinceLastUpdate)/1000f);
            timeSpentPausedSinceLastUpdate = 0;
            lastUpdate = now;
        }
    }

    @Override
    public double getSecondsSinceStart() {
        return secondsSinceStart;
    }

    @Override
    public double getSecondsSinceLastUpdate() {
        return secondsSinceLastUpdate;
    }

    @Override
    public void setPaused(boolean newPaused) {
        if(this.paused && !newPaused){
            long timePaused = System.currentTimeMillis() - pauseTime;
            totalTimeSpentPaused += timePaused;
            timeSpentPausedSinceLastUpdate += timePaused;
            pauseTime = -1;
        }else if(!this.paused && newPaused){
            pauseTime = System.currentTimeMillis();
        }
        this.paused = newPaused;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }
}
