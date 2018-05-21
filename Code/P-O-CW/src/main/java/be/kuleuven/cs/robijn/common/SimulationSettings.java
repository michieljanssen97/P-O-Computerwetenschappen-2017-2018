package be.kuleuven.cs.robijn.common;

import be.kuleuven.cs.robijn.gui.ObservableAutoPilotConfig;
import interfaces.AutopilotConfig;
import interfaces.AutopilotConfigReader;
import interfaces.AutopilotConfigWriter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

public class SimulationSettings {
    private float runwayLength;
    private float gateLength;
    private AirportDefinition[] airports = new AirportDefinition[0];
    private DroneDefinition[] drones = new DroneDefinition[0];

    public float getRunwayLength() {
        return runwayLength;
    }

    public SimulationSettings setRunwayLength(float runwayLength) {
        this.runwayLength = runwayLength;
        return this;
    }

    public float getGateLength() {
        return gateLength;
    }

    public SimulationSettings setGateLength(float gateLength) {
        this.gateLength = gateLength;
        return this;
    }

    public AirportDefinition[] getAirports() {
        return airports;
    }

    public SimulationSettings setAirports(AirportDefinition[] airports) {
        this.airports = airports;
        return this;
    }

    public DroneDefinition[] getDrones() {
        return drones;
    }

    public SimulationSettings setDrones(DroneDefinition[] drones) {
        this.drones = drones;
        return this;
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeFloat(runwayLength);
        out.writeFloat(gateLength);
        out.writeInt(airports.length);
        for(AirportDefinition airport : airports){
            airport.write(out);
        }
        out.writeInt(drones.length);
        for(DroneDefinition drone : drones){
            drone.write(out);
        }
    }

    public void read(DataInputStream in) throws IOException {
        runwayLength = in.readFloat();
        gateLength = in.readFloat();
        airports = new AirportDefinition[in.readInt()];
        for(int i = 0; i < airports.length; i++){
            airports[i] = new AirportDefinition();
            airports[i].read(in);
        }
        drones = new DroneDefinition[in.readInt()];
        for(int i = 0; i < drones.length; i++){
            drones[i] = new DroneDefinition();
            drones[i].read(in);
        }
    }

    public static class AirportDefinition {
        private float centerX;
        private float centerZ;
        private float centerToRunway0X;
        private float centerToRunway0Z;

        public void write(DataOutputStream out) throws IOException {
            out.writeFloat(centerX);
            out.writeFloat(centerZ);
            out.writeFloat(centerToRunway0X);
            out.writeFloat(centerToRunway0Z);
        }

        public void read(DataInputStream in) throws IOException {
            centerX = in.readFloat();
            centerZ = in.readFloat();
            centerToRunway0X = in.readFloat();
            centerToRunway0Z = in.readFloat();
        }

        public float getCenterX() {
            return centerX;
        }

        public AirportDefinition setCenterX(float centerX) {
            this.centerX = centerX;
            return this;
        }

        public float getCenterZ() {
            return centerZ;
        }

        public AirportDefinition setCenterZ(float centerZ) {
            this.centerZ = centerZ;
            return this;
        }

        public float getCenterToRunway0X() {
            return centerToRunway0X;
        }

        public AirportDefinition setCenterToRunway0X(float centerToRunway0X) {
            this.centerToRunway0X = centerToRunway0X;
            return this;
        }

        public float getCenterToRunway0Z() {
            return centerToRunway0Z;
        }

        public AirportDefinition setCenterToRunway0Z(float centerToRunway0Z) {
            this.centerToRunway0Z = centerToRunway0Z;
            return this;
        }
    }

    public static class GateDefinition{
        private AirportDefinition airport;
        private int id;

        public GateDefinition(AirportDefinition airport, int id){
            this.airport = airport;
            this.id = id;
        }

        public AirportDefinition getAirport() {
            return airport;
        }

        public int getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GateDefinition that = (GateDefinition) o;
            return id == that.id &&
                    Objects.equals(airport, that.airport);
        }

        @Override
        public int hashCode() {

            return Objects.hash(airport, id);
        }
    }

    public static class DroneDefinition {
        private ObservableAutoPilotConfig config;
        private AirportDefinition airport;
        private int gate;
        private int runwayToFace;

        DroneDefinition (){ }

        public DroneDefinition(AutopilotConfig config) {
            this(new ObservableAutoPilotConfig(config));
        }

        public DroneDefinition(ObservableAutoPilotConfig config) {
            this.config = config;
        }

        public DroneDefinition(ObservableAutoPilotConfig config, AirportDefinition airport, int gate, int runwayToFace) {
            this.config = config;
            this.airport = airport;
            this.gate = gate;
            this.runwayToFace = runwayToFace;
        }

        public void write(DataOutputStream out) throws IOException {
            AutopilotConfigWriter.write(out, config);
            //airport.write(out);
            out.writeInt(gate);
            out.writeInt(runwayToFace);
        }

        public void read(DataInputStream in) throws IOException {
            config = new ObservableAutoPilotConfig(AutopilotConfigReader.read(in));
            //airport = new AirportDefinition();
            //airport.read(in);
            gate = in.readInt();
            runwayToFace = in.readInt();
        }

        public boolean isValid(){
            return this.config != null && this.airport != null;
        }

        public ObservableAutoPilotConfig getConfig() {
            return config;
        }

        public DroneDefinition setConfig(ObservableAutoPilotConfig config) {
            this.config = config;
            return this;
        }

        public AirportDefinition getAirport() {
            return airport;
        }

        public DroneDefinition setAirport(AirportDefinition airport) {
            this.airport = airport;
            return this;
        }

        public int getGate() {
            return gate;
        }

        public DroneDefinition setGate(int gate) {
            this.gate = gate;
            return this;
        }

        public int getRunwayToFace() {
            return runwayToFace;
        }

        public DroneDefinition setRunwayToFace(int runwayToFace) {
            this.runwayToFace = runwayToFace;
            return this;
        }
    }
}
