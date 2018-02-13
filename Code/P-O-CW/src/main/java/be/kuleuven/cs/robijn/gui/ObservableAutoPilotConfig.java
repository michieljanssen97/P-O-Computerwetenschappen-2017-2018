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
    public static final String DRONE_ID_KEY = "DRONE_ID";

    public static final String GRAVITY_KEY = "GRAVITY";
    public static final String WING_X_KEY = "WING_X";
    public static final String TAIL_SIZE_KEY = "TAIL_SIZE";
    public static final String WHEEL_Y_KEY = "WHEEL_Y";
    public static final String FRONT_WHEEL_Z_KEY = "FRONT_WHEEL_Z";
    public static final String REAR_WHEEL_Z_KEY = "REAR_WHEEL_Z";
    public static final String REAR_WHEEL_X_KEY = "REAR_WHEEL_X";
    public static final String TYRE_SLOPE_KEY = "TYRE_SLOPE";
    public static final String DAMP_SLOPE_KEY = "DAMP_SLOPE";
    public static final String TYRE_RADIUS_KEY = "TYRE_RADIUS";
    public static final String R_MAX_KEY = "R_MAX";
    public static final String FC_MAX_KEY = "FC_MAX";
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

    private ObservableMap<String, String> stringProperties = FXCollections.observableHashMap();
    private ObservableMap<String, Number> numberProperties = FXCollections.observableHashMap();

    public ObservableAutoPilotConfig(AutopilotConfig sourceConfig){
        stringProperties.put(DRONE_ID_KEY, sourceConfig.getDroneID());

        numberProperties.put(GRAVITY_KEY, sourceConfig.getGravity());
        numberProperties.put(WING_X_KEY, sourceConfig.getWingX());
        numberProperties.put(TAIL_SIZE_KEY, sourceConfig.getTailSize());
        numberProperties.put(WHEEL_Y_KEY, sourceConfig.getWheelY());
        numberProperties.put(FRONT_WHEEL_Z_KEY, sourceConfig.getFrontWheelZ());
        numberProperties.put(REAR_WHEEL_Z_KEY, sourceConfig.getRearWheelZ());
        numberProperties.put(REAR_WHEEL_X_KEY, sourceConfig.getRearWheelX());
        numberProperties.put(TYRE_SLOPE_KEY, sourceConfig.getTyreSlope());
        numberProperties.put(DAMP_SLOPE_KEY, sourceConfig.getDampSlope());
        numberProperties.put(TYRE_RADIUS_KEY, sourceConfig.getTyreRadius());
        numberProperties.put(R_MAX_KEY, sourceConfig.getRMax());
        numberProperties.put(FC_MAX_KEY, sourceConfig.getFcMax());
        numberProperties.put(ENGINE_MASS_KEY, sourceConfig.getEngineMass());
        numberProperties.put(WING_MASS_KEY, sourceConfig.getWingMass());
        numberProperties.put(TAIL_MASS_KEY, sourceConfig.getTailMass());
        numberProperties.put(MAX_THRUST_KEY, sourceConfig.getMaxThrust());
        numberProperties.put(MAX_AOA_KEY, sourceConfig.getMaxAOA());
        numberProperties.put(WING_LIFT_SLOPE_KEY, sourceConfig.getWingLiftSlope());
        numberProperties.put(HOR_STAB_LIFT_SLOPE_KEY, sourceConfig.getHorStabLiftSlope());
        numberProperties.put(VER_STAB_LIFT_SLOPE_KEY, sourceConfig.getVerStabLiftSlope());
        numberProperties.put(HORIZONTAL_ANGLE_OF_VIEW_KEY, sourceConfig.getHorizontalAngleOfView());
        numberProperties.put(VERTICAL_ANGLE_OF_VIEW_KEY, sourceConfig.getVerticalAngleOfView());
        numberProperties.put(NB_COLUMNS_KEY, sourceConfig.getNbColumns());
        numberProperties.put(NB_ROWS_KEY, sourceConfig.getNbRows());
    }

    /**
     * Returns the ObservableMap that contains the values of this object.
     */
    public ObservableMap<String, Number> getNumberProperties() {
        return numberProperties;
    }

    /**
     * Returns the value of the specified property.
     * @param key the key of the property to retrieve the value of. (ex. GRAVITY_KEY)
     * @return the current numeric value of specified property
     */
    public Number getNumberProperty(String key){
        return numberProperties.get(key);
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
                numberProperties.put(key, value.intValue());
                break;
            default:
                numberProperties.put(key, value.floatValue());
                break;
        }
    }

    /**
     * Returns the ObservableMap that contains the values of this object.
     */
    public ObservableMap<String, String> getStringProperties() {
        return stringProperties;
    }

    /**
     * Returns the value of the specified property.
     * @param key the key of the property to retrieve the value of. (ex. DRONE_ID_KEY)
     * @return the current string value of this property
     */
    public String getStringProperty(String key){
        return stringProperties.get(key);
    }

    /**
     * Assigns the specified value to the specified property.
     * @param key the key of the property to set the value of. (ex. GRAVITY_KEY)
     * @param value the value to assign to this property.
     */
    public void setProperty(String key, String value){
        stringProperties.put(key, value);
    }


    @Override
    public String getDroneID() {
        return stringProperties.get(DRONE_ID_KEY);
    }

    @Override
    public float getGravity() {
        return (float) numberProperties.get(GRAVITY_KEY);
    }

    @Override
    public float getWingX() {
        return (float) numberProperties.get(WING_X_KEY);
    }

    @Override
    public float getTailSize() {
        return (float) numberProperties.get(TAIL_SIZE_KEY);
    }

    @Override
    public float getWheelY() {
        return (float) numberProperties.get(WHEEL_Y_KEY);
    }

    @Override
    public float getFrontWheelZ() {
        return (float) numberProperties.get(FRONT_WHEEL_Z_KEY);
    }

    @Override
    public float getRearWheelZ() {
        return (float) numberProperties.get(REAR_WHEEL_Z_KEY);
    }

    @Override
    public float getRearWheelX() {
        return (float) numberProperties.get(REAR_WHEEL_X_KEY);
    }

    @Override
    public float getTyreSlope() {
        return (float) numberProperties.get(TYRE_SLOPE_KEY);
    }

    @Override
    public float getDampSlope() {
        return (float) numberProperties.get(DAMP_SLOPE_KEY);
    }

    @Override
    public float getTyreRadius() {
        return (float) numberProperties.get(TYRE_RADIUS_KEY);
    }

    @Override
    public float getRMax() {
        return (float) numberProperties.get(R_MAX_KEY);
    }

    @Override
    public float getFcMax() {
        return (float) numberProperties.get(FC_MAX_KEY);
    }

    @Override
    public float getEngineMass() {
        return (float) numberProperties.get(ENGINE_MASS_KEY);
    }

    @Override
    public float getWingMass() {
        return (float) numberProperties.get(WING_MASS_KEY);
    }

    @Override
    public float getTailMass() {
        return (float) numberProperties.get(TAIL_MASS_KEY);
    }

    @Override
    public float getMaxThrust() {
        return (float) numberProperties.get(MAX_THRUST_KEY);
    }

    @Override
    public float getMaxAOA() {
        return (float) numberProperties.get(MAX_AOA_KEY);
    }

    @Override
    public float getWingLiftSlope() {
        return (float) numberProperties.get(WING_LIFT_SLOPE_KEY);
    }

    @Override
    public float getHorStabLiftSlope() {
        return (float) numberProperties.get(HOR_STAB_LIFT_SLOPE_KEY);
    }

    @Override
    public float getVerStabLiftSlope() {
        return (float) numberProperties.get(VER_STAB_LIFT_SLOPE_KEY);
    }

    @Override
    public float getHorizontalAngleOfView() {
        return (float) numberProperties.get(HORIZONTAL_ANGLE_OF_VIEW_KEY);
    }

    @Override
    public float getVerticalAngleOfView() {
        return (float) numberProperties.get(VERTICAL_ANGLE_OF_VIEW_KEY);
    }

    @Override
    public int getNbColumns() {
        return (int) numberProperties.get(NB_COLUMNS_KEY);
    }

    @Override
    public int getNbRows() {
        return (int) numberProperties.get(NB_ROWS_KEY);
    }
}
