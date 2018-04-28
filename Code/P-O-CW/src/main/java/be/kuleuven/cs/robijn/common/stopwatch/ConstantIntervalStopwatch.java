package be.kuleuven.cs.robijn.common.stopwatch;

public class ConstantIntervalStopwatch implements Stopwatch{
    private double interval;
    private double timePassed;
    private boolean paused = false;

    private double timeMultiplier = 1.0; //Each interval is multiplied by this value to get the actual value

    public ConstantIntervalStopwatch(){
        this(0.03d);
    }

    public ConstantIntervalStopwatch(double interval){
        if(interval <= 0 || Double.isNaN(interval) || Double.isInfinite(interval)){
            throw new IllegalArgumentException("interval must be finite, positive and non-zero");
        }
        this.interval = interval;
    }

    public double getInterval(){
        return interval;
    }

    public void setInterval(double interval){
        if(interval <= 0 || Double.isNaN(interval) || Double.isInfinite(interval)){
            throw new IllegalArgumentException("interval must be finite, positive and non-zero");
        }
        this.interval = interval;
    }

    @Override
    public void tick() {
        if(!paused){
            timePassed += (interval * timeMultiplier);
        }
    }

    @Override
    public double getSecondsSinceStart() {
        return timePassed;
    }

    @Override
    public double getSecondsSinceLastUpdate() {
        return interval;
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
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

	@Override
	public void reset() {
		timePassed = 0;
	}
}
