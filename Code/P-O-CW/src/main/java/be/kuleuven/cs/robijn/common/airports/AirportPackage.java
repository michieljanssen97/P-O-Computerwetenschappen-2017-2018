package be.kuleuven.cs.robijn.common.airports;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public class AirportPackage {
    private final Gate origin, destination;
    private ArrayList<Consumer<AirportPackage>> deliveryEventHandlers = new ArrayList<>();
    private boolean hasBeenDelivered;

    public AirportPackage(Gate origin, Gate destination){
        if(origin == null || destination == null){
            throw new IllegalArgumentException();
        }

        this.origin = origin;
        this.destination = destination;
    }

    public Gate getOrigin(){
        return origin;
    }

    public Gate getDestination() {
        return destination;
    }

    public void addDeliveryEventHandler(Consumer<AirportPackage> deliveryHandler){
        deliveryEventHandlers.add(deliveryHandler);
    }

    public void markAsDelivered(){
        if(!hasBeenDelivered){
            hasBeenDelivered = true;
            for(Consumer<AirportPackage> handler : deliveryEventHandlers){
                handler.accept(this);
            }
        }
    }

    public boolean hasBeenDelivered(){
        return hasBeenDelivered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AirportPackage that = (AirportPackage) o;
        return Objects.equals(origin, that.origin) &&
                Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination);
    }

    @Override
    public String toString() {
        return "Package{" +
                "origin=" + origin.getAirport().getId() + ":" + origin.getId() +
                ", destination=" + destination.getAirport().getId() + ":" + destination.getId() +
                '}';
    }
}
