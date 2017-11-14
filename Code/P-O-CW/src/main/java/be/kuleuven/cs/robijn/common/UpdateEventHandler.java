package be.kuleuven.cs.robijn.common;


import p_en_o_cw_2017.AutopilotInputs;
import p_en_o_cw_2017.AutopilotOutputs;

import java.util.function.BiConsumer;


public class UpdateEventHandler implements Comparable<UpdateEventHandler>{

    public static final int HIGH_PRIORITY = 100;
    public static final int MEDIUM_PRIORITY = 50;
    public static final int LOW_PRIORITY = 0;

    private BiConsumer<AutopilotInputs, AutopilotOutputs> function;
    private int priority;

    public UpdateEventHandler(BiConsumer<AutopilotInputs, AutopilotOutputs> function, int priority){

        this.function = function;
        this.priority = priority;
    }


    public BiConsumer<AutopilotInputs, AutopilotOutputs> getFunction() {
        return function;
    }

    public int getPriority() {
        return priority;
    }


    @Override
    public int compareTo(UpdateEventHandler updateEventHandler) {
        int comparison = updateEventHandler.getPriority() - getPriority();
        if(comparison == 0){
            comparison = Integer.compare(this.hashCode(), updateEventHandler.hashCode());
        }
        return comparison;
    }
}
