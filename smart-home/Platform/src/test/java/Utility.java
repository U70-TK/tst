package tartan.tests.util;

import java.util.Map;
import java.util.Hashtable;

import tartan.smarthome.resources.iotcontroller.IoTValues;

/**
 * Test utility class. Used to isolate a bunch of 'global' test behaviour. Should be 
 * usefull to numerous test classes.
 */
public class Utility {
    /**
     * Create a default state for the TartanStateEvalutator
     * @return The default state
     */
    public Map<String, Object> createDefaultState() {
        var customState = new Hashtable<String, Object>();

        customState.put(IoTValues.PROXIMITY_STATE, false);
        customState.put(IoTValues.LIGHT_STATE, false);
        customState.put(IoTValues.TEMP_READING, 20);
        customState.put(IoTValues.HUMIDITY_READING, 50);
        customState.put(IoTValues.HUMIDIFIER_STATE, false);
        customState.put(IoTValues.DOOR_STATE, true);
        customState.put(IoTValues.ALARM_STATE, false);
        customState.put(IoTValues.HEATER_STATE, false);
        customState.put(IoTValues.CHILLER_STATE, false);
        customState.put(IoTValues.TARGET_TEMP, 70);
        customState.put(IoTValues.ALARM_PASSCODE, "1234");
        customState.put(IoTValues.GIVEN_PASSCODE, "1234");
        customState.put(IoTValues.HVAC_MODE, "HEATER");
        customState.put(IoTValues.AWAY_TIMER, false);
        customState.put(IoTValues.ALARM_ACTIVE, false);

        return customState;
    }
}
