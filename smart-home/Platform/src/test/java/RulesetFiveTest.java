import org.junit.jupiter.api.Test;
import tartan.smarthome.resources.StaticTartanStateEvaluator;
import tartan.smarthome.resources.iotcontroller.IoTValues;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class RulesetFiveTest {
    @Test
    public void testRulesetFive() {
        StaticTartanStateEvaluator evaluator = new StaticTartanStateEvaluator();
        StringBuffer log = new StringBuffer();
        Map<String, Object> iniState = new HashMap<>();

        iniState.put(IoTValues.TEMP_READING, 20);
        iniState.put(IoTValues.HUMIDITY_READING, 50);
        iniState.put(IoTValues.TARGET_TEMP, 20);
        iniState.put(IoTValues.HUMIDIFIER_STATE, false);
        iniState.put(IoTValues.DOOR_STATE, true); // door open
        iniState.put(IoTValues.LIGHT_STATE, false);
        iniState.put(IoTValues.PROXIMITY_STATE, true); // house occupied
        iniState.put(IoTValues.ALARM_STATE, false); // alarm off
        iniState.put(IoTValues.HEATER_STATE, false);
        iniState.put(IoTValues.CHILLER_STATE, false);
        iniState.put(IoTValues.AWAY_TIMER, false); // away timer off
        iniState.put(IoTValues.ALARM_ACTIVE, false);
        iniState.put(IoTValues.HVAC_MODE, "Heater"); // Initialize hvacSetting
        iniState.put(IoTValues.ALARM_PASSCODE, "1234"); // Initialize alarmPassCode
        iniState.put(IoTValues.GIVEN_PASSCODE, "1234"); // Initialize givenPassCode

        // Simulate house becoming vacant
        iniState.put(IoTValues.PROXIMITY_STATE, false); // house now vacant

        Map<String, Object> newState = evaluator.evaluateState(iniState, log);

        assertTrue(
            (Boolean) newState.get(IoTValues.AWAY_TIMER),
            "Away timer should start when the house becomes vacant"
        );

        assertTrue(
            log.toString().contains("Starting away timer"),
            "Log should contain a message about starting the away timer"
        );
    }
}