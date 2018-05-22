package be.kuleuven.cs.robijn.common.airports.generator;

import be.kuleuven.cs.robijn.common.SimulationSettings.AirportDefinition;
import be.kuleuven.cs.robijn.common.math.ScalarMath;
import org.apache.commons.math3.distribution.NormalDistribution;

public class RandomAirportGenerator implements AirportGenerator {
    @Override
    public AirportDefinition[] generate(Settings settings) {
        AirportDefinition[] airports = new AirportDefinition[settings.getAirportCount()];
        for (int i = 0; i < airports.length; i++){
            airports[i] = new AirportDefinition();

            //Set position
            float x, z;
            boolean isTaken;
            NormalDistribution normal = new NormalDistribution(0, 2000);
            do {
                //Generate random coordinate
                //x = (float)(Math.random() * 60000d);
                //z = (float)(Math.random() * 60000d);
                x = ScalarMath.betweenBoundaries((float)normal.sample(), 30000, -30000);
                z = ScalarMath.betweenBoundaries((float)normal.sample(), 30000, -30000);

                //Check if it is already taken
                isTaken = false;
                for(int j = 0; j < i; j++){
                    if(Math.abs(airports[j].getCenterX() - x) < settings.getXDistBetweenAirports()){
                        isTaken = true;
                        break;
                    }

                    if(Math.abs(airports[j].getCenterZ() - z) < settings.getZDistBetweenAirports()){
                        isTaken = true;
                        break;
                    }
                }
            } while(isTaken);

            airports[i].setCenterX(x);
            airports[i].setCenterZ(z);

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
        return "Random";
    }
}
