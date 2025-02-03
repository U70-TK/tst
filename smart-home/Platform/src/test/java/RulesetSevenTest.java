import org.junit.jupiter.api.Test;
import tartan.smarthome.resources.StaticTartanStateEvaluator;
import tartan.smarthome.resources.iotcontroller.IoTValues;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class RulesetSevenTest {

    @Test
    public void testRulesetSeven() {
        // Setup
        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();
        Map<String, Object> iniState = new HashMap<>();

        // Initial state: door open and house vacant, house becoming occupied
        iniState.put(IoTValues.TEMP_READING, 20);
        iniState.put(IoTValues.HUMIDITY_READING, 50);
        iniState.put(IoTValues.TARGET_TEMP, 20);
        iniState.put(IoTValues.HUMIDIFIER_STATE, false);
        iniState.put(IoTValues.DOOR_STATE, true); // door open
        iniState.put(IoTValues.LIGHT_STATE, false);
        iniState.put(IoTValues.PROXIMITY_STATE, true); // simulate house becoming occupied
        iniState.put(IoTValues.ALARM_STATE, false);
        iniState.put(IoTValues.HEATER_STATE, false);
        iniState.put(IoTValues.CHILLER_STATE, false);
        iniState.put(IoTValues.ALARM_PASSCODE, "Testing1!");
        iniState.put(IoTValues.GIVEN_PASSCODE, "Testing1!");
        iniState.put(IoTValues.HVAC_MODE, "HEATER");
        iniState.put(IoTValues.AWAY_TIMER, false);
        iniState.put(IoTValues.ALARM_ACTIVE, false);

        Map<String, Object> newState = evaluator.evaluateState(iniState, log);

        // Assert: Light should be turned on when house becomes occupied and alarm is disabled
        assertTrue((Boolean) newState.get(IoTValues.LIGHT_STATE), "Light should be turned on when house is occupied and alarm is disabled");

        // Check the log for the light turning on message
        assertTrue(log.toString().contains("Turning on light"), "Log should contain message about turning on the light");
    }
}
