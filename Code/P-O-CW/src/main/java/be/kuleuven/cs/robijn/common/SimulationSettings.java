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
