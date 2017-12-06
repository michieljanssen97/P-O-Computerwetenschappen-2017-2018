package be.kuleuven.cs.robijn.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import interfaces.AutopilotConfig;

/**
 * An implementation of AutopilotConfig that is driven by an ObservableMap.
 * This allows easy use and modification of the config in GUI controls.
 */
public class ObservableAutoPilotConfig implements AutopilotConfig {
    //Keys of the various settings in the ObservableMap
    public static final String GRAVITY_KEY = "GRAVITY";
    public static final String WING_X_KEY = "WING_X";
    public static final String TAIL_SIZE_KEY = "TAIL_SIZE";
    public static final String ENGINE_MASS_KEY = "ENGINE_MASS";
    public static final String WING_MASS_KEY = "WING_MASS";
    public static final String TAIL_MASS_KEY = "TAIL_MASS";
    public static final String MAX_THRUST_KEY = "MAX_THRUST";
    public static final String MAX_AOA_KEY = "MAX_AOA";
    public static final String WING_LIFT_SLOPE_KEY = "WING_LIFT_SLOPE";
    public static final String HOR_STAB_LIFT_SLOPE_KEY = "HOR_STAB_LIFT_SLOPE";
    public static final String VER_STAB_LIFT_SLOPE_KEY = "VER_STAB_LIFT_SLOPE";
    public static final String HORIZONTAL_ANGLE_OF_VIEW_KEY = "HORIZONTAL_ANGLE_OF_VIEW";
    public static final String VERTICAL_ANGLE_OF_VIEW_KEY = "VERTICAL_ANGLE_OF_VIEW";
    public static final String NB_COLUMNS_KEY = "NB_COLUMNS";
    public static final String NB_ROWS_KEY = "NB_ROWS";

    private ObservableMap<String, Number> properties = FXCollections.observableHashMap();

    public ObservableAutoPilotConfig(AutopilotConfig sourceConfig){
        properties.put(GRAVITY_KEY, sourceConfig.getGravity());
        properties.put(WING_X_KEY, sourceConfig.getWingX());
        properties.put(TAIL_SIZE_KEY, sourceConfig.getTailSize());
        properties.put(ENGINE_MASS_KEY, sourceConfig.getEngineMass());
        properties.put(WING_MASS_KEY, sourceConfig.getWingMass());
        properties.put(TAIL_MASS_KEY, sourceConfig.getTailMass());
        properties.put(MAX_THRUST_KEY, sourceConfig.getMaxThrust());
        properties.put(MAX_AOA_KEY, sourceConfig.getMaxAOA());
        properties.put(WING_LIFT_SLOPE_KEY, sourceConfig.getWingLiftSlope());
        properties.put(HOR_STAB_LIFT_SLOPE_KEY, sourceConfig.getHorStabLiftSlope());
        properties.put(VER_STAB_LIFT_SLOPE_KEY, sourceConfig.getVerStabLiftSlope());
        properties.put(HORIZONTAL_ANGLE_OF_VIEW_KEY, sourceConfig.getHorizontalAngleOfView());
        properties.put(VERTICAL_ANGLE_OF_VIEW_KEY, sourceConfig.getVerticalAngleOfView());
        properties.put(NB_COLUMNS_KEY, sourceConfig.getNbColumns());
        properties.put(NB_ROWS_KEY, sourceConfig.getNbRows());
    }

    /**
     * Returns the ObservableMap that contains the values of this object.
     */
    public ObservableMap<String, Number> getProperties() {
        return properties;
    }

    /**
     * Returns the value of the specified property.
     * @param key the key of the property to retrieve the value of. (ex. GRAVITY_KEY)
     * @return the current numeric value of specified property
     */
    public Number getProperty(String key){
        return properties.get(key);
    }

    /**
     * Assigns the specified value to the specified property.
     * The Number argument is automatically converted to the correct type.
     * @param key the key of the property to set the value of. (ex. GRAVITY_KEY)
     * @param value the value to assign to this property.
     */
    public void setProperty(String key, Number value){
        switch (key){
            case NB_COLUMNS_KEY:
            case NB_ROWS_KEY:
                properties.put(key, value.intValue());
                break;
            default:
                properties.put(key, value.floatValue());
                break;
        }
    }

    @Override
    public float getGravity() {
        return (float)properties.get(GRAVITY_KEY);
    }

    @Override
    public float getWingX() {
        return (float)properties.get(WING_X_KEY);
    }

    @Override
    public float getTailSize() {
        return (float)properties.get(TAIL_SIZE_KEY);
    }

    @Override
    public float getEngineMass() {
        return (float)properties.get(ENGINE_MASS_KEY);
    }

    @Override
    public float getWingMass() {
        return (float)properties.get(WING_MASS_KEY);
    }

    @Override
    public float getTailMass() {
        return (float)properties.get(TAIL_MASS_KEY);
    }

    @Override
    public float getMaxThrust() {
        return (float)properties.get(MAX_THRUST_KEY);
    }

    @Override
    public float getMaxAOA() {
        return (float)properties.get(MAX_AOA_KEY);
    }

    @Override
    public float getWingLiftSlope() {
        return (float)properties.get(WING_LIFT_SLOPE_KEY);
    }

    @Override
    public float getHorStabLiftSlope() {
        return (float)properties.get(HOR_STAB_LIFT_SLOPE_KEY);
    }

    @Override
    public float getVerStabLiftSlope() {
        return (float)properties.get(VER_STAB_LIFT_SLOPE_KEY);
    }

    @Override
    public float getHorizontalAngleOfView() {
        return (float)properties.get(HORIZONTAL_ANGLE_OF_VIEW_KEY);
    }

    @Override
    public float getVerticalAngleOfView() {
        return (float)properties.get(VERTICAL_ANGLE_OF_VIEW_KEY);
    }

    @Override
    public int getNbColumns() {
        return (int)properties.get(NB_COLUMNS_KEY);
    }

    @Override
    public int getNbRows() {
        return (int)properties.get(NB_ROWS_KEY);
    }
}
