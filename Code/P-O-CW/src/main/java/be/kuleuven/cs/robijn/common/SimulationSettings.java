package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.gui.ObservableAutoPilotConfig;
import interfaces.AutopilotConfig;

public class SimulationSettings {
    private float runwayLength;
    private float gateLength;
    private AirportDefinition[] airports = new AirportDefinition[0];
    private DroneDefinition[] drones = new DroneDefinition[0];

    public float getRunwayLength() {
        return runwayLength;
    }

    public void setRunwayLength(float runwayLength) {
        this.runwayLength = runwayLength;
    }

    public float getGateLength() {
        return gateLength;
    }

    public void setGateLength(float gateLength) {
        this.gateLength = gateLength;
    }

    public AirportDefinition[] getAirports() {
        return airports;
    }

    public void setAirports(AirportDefinition[] airports) {
        this.airports = airports;
    }

    public DroneDefinition[] getDrones() {
        return drones;
    }

    public void setDrones(DroneDefinition[] drones) {
        this.drones = drones;
    }

    public static class AirportDefinition {
        private float centerX;
        private float centerZ;
        private float centerToRunway0X;
        private float centerToRunway0Z;

        public float getCenterX() {
            return centerX;
        }

        public void setCenterX(float centerX) {
            this.centerX = centerX;
        }

        public float getCenterZ() {
            return centerZ;
        }

        public void setCenterZ(float centerZ) {
            this.centerZ = centerZ;
        }

        public float getCenterToRunway0X() {
            return centerToRunway0X;
        }

        public void setCenterToRunway0X(float centerToRunway0X) {
            this.centerToRunway0X = centerToRunway0X;
        }

        public float getCenterToRunway0Z() {
            return centerToRunway0Z;
        }

        public void setCenterToRunway0Z(float centerToRunway0Z) {
            this.centerToRunway0Z = centerToRunway0Z;
        }
    }

    public static class DroneDefinition {
        private ObservableAutoPilotConfig config;
        private AirportDefinition airport;
        private int gate;
        private int runwayToFace;

        public DroneDefinition(ObservableAutoPilotConfig config) {
            this.config = config;
        }

        public DroneDefinition(ObservableAutoPilotConfig config, AirportDefinition airport, int gate, int runwayToFace) {
            this.config = config;
            this.airport = airport;
            this.gate = gate;
            this.runwayToFace = runwayToFace;
        }

        public boolean isValid(){
            return this.config != null && this.airport != null;
        }

        public ObservableAutoPilotConfig getConfig() {
            return config;
        }

        public void setConfig(ObservableAutoPilotConfig config) {
            this.config = config;
        }

        public AirportDefinition getAirport() {
            return airport;
        }

        public void setAirport(AirportDefinition airport) {
            this.airport = airport;
        }

        public int getGate() {
            return gate;
        }

        public void setGate(int gate) {
            this.gate = gate;
        }

        public int getRunwayToFace() {
            return runwayToFace;
        }

        public void setRunwayToFace(int runwayToFace) {
            this.runwayToFace = runwayToFace;
        }
    }
}
