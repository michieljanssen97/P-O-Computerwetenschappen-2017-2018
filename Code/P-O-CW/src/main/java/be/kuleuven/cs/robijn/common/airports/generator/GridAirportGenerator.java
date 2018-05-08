package be.kuleuven.cs.robijn.common.airports.generator;

import be.kuleuven.cs.robijn.common.SimulationSettings;

public class GridAirportGenerator implements AirportGenerator{
    @Override
    public SimulationSettings.AirportDefinition[] generate(Settings settings) {
        SimulationSettings.AirportDefinition[] airports = new SimulationSettings.AirportDefinition[settings.getAirportCount()];
        int rowLength = (int)Math.sqrt(airports.length);
        for (int i = 0; i < airports.length; i++){
            airports[i] = new SimulationSettings.AirportDefinition();

            //Set position
            int columnI = i % rowLength;
            int rowI = i / rowLength;
            airports[i].setCenterX(rowI * settings.getXDistBetweenAirports());
            airports[i].setCenterZ(columnI * settings.getZDistBetweenAirports());

            //Set rotation
            if(settings.isRandomizeRotationEnabled()){
                double angle = Math.random() * Math.PI*2;
                double angleX = Math.cos(angle);
                double angleZ = Math.sin(angle);
                airports[i].setCenterToRunway0X((float)angleX);
                airports[i].setCenterToRunway0Z((float)angleZ);
            }else{
                airports[i].setCenterToRunway0X(0);
                airports[i].setCenterToRunway0Z(-1.0f);
            }
        }

        return airports;
    }

    @Override
    public String getPatternName() {
        return "Grid";
    }
}
