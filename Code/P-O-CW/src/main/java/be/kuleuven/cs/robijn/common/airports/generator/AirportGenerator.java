package be.kuleuven.cs.robijn.common.airports.generator;

import be.kuleuven.cs.robijn.common.SimulationSettings.AirportDefinition;

public interface AirportGenerator {
    AirportGenerator[] GENERATORS = new AirportGenerator[]{
        new RandomAirportGenerator(),
        new GridAirportGenerator()
    };

    AirportDefinition[] generate(Settings settings);
    String getPatternName();

    class Settings {
        private int airportCount;
        private boolean randomizeRotation;
        private float xDistBetweenAirports;
        private float zDistBetweenAirports;

        public int getAirportCount() {
            return airportCount;
        }

        public void setAirportCount(int airportCount) {
            this.airportCount = airportCount;
        }

        public boolean isRandomizeRotationEnabled() {
            return randomizeRotation;
        }

        public void setRandomizeRotationEnabled(boolean randomizeRotation) {
            this.randomizeRotation = randomizeRotation;
        }

        public float getXDistBetweenAirports() {
            return xDistBetweenAirports;
        }

        public void setXDistBetweenAirports(float xDistBetweenAirports) {
            this.xDistBetweenAirports = xDistBetweenAirports;
        }

        public float getZDistBetweenAirports() {
            return zDistBetweenAirports;
        }

        public void setZDistBetweenAirports(float zDistBetweenAirports) {
            this.zDistBetweenAirports = zDistBetweenAirports;
        }
    }
}
