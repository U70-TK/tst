import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tartan.smarthome.resources.StaticTartanStateEvaluator;
import tartan.smarthome.resources.iotcontroller.IoTValues;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;

public class StaticTartanStateEvaluatorTest {

    private static StaticTartanStateEvaluator evaluator;
    private static Map<String, Object> initialState;
    private static StringBuffer log;

    @BeforeAll
    public static void initialize() {
        evaluator = new StaticTartanStateEvaluator();
        initialState = new HashMap<>();
        log = new StringBuffer();

        // Default state setup: door open and house vacant
        initialState.put(IoTValues.TEMP_READING, 20);
        initialState.put(IoTValues.HUMIDITY_READING, 50);
        initialState.put(IoTValues.TARGET_TEMP, 20);
        initialState.put(IoTValues.HUMIDIFIER_STATE, false);
        initialState.put(IoTValues.DOOR_STATE, true); // Door open
        initialState.put(IoTValues.LIGHT_STATE, false);
        initialState.put(IoTValues.PROXIMITY_STATE, false); // House vacant
        initialState.put(IoTValues.ALARM_STATE, false);
        initialState.put(IoTValues.HEATER_STATE, false);
        initialState.put(IoTValues.CHILLER_STATE, false);
        initialState.put(IoTValues.ALARM_PASSCODE, "Testing1!");
        initialState.put(IoTValues.GIVEN_PASSCODE, "Testing1!");
        initialState.put(IoTValues.HVAC_MODE, "HEATER");
        initialState.put(IoTValues.AWAY_TIMER, false);
        initialState.put(IoTValues.ALARM_ACTIVE, false);
    }

    @Test
    public void testHouseOccupiedAndLightOff() {
        // House occupied, light off, alarm disabled
        initialState.put(IoTValues.PROXIMITY_STATE, true);  // House occupied
        initialState.put(IoTValues.LIGHT_STATE, false);    // Light off
        initialState.put(IoTValues.ALARM_STATE, false);    // Alarm disabled

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Light should be turned on because the house is occupied and alarm is off
        assertTrue((Boolean) newState.get(IoTValues.LIGHT_STATE), "Light should be turned on when house is occupied and alarm is disabled");
    }

    @Test
    public void testHouseOccupiedAndLightOn() {
        // House occupied, light already on, alarm disabled
        initialState.put(IoTValues.PROXIMITY_STATE, true);  // House occupied
        initialState.put(IoTValues.LIGHT_STATE, true);     // Light already on
        initialState.put(IoTValues.ALARM_STATE, false);    // Alarm disabled

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Light should remain on, as the house is occupied and alarm is off
        assertTrue((Boolean) newState.get(IoTValues.LIGHT_STATE), "Light should remain on when house is occupied and alarm is disabled");
    }

    @Test
    public void testDoorClosedWithHouseOccupied() {
        // Test: Door closed when house is occupied, alarm active
        initialState.put(IoTValues.PROXIMITY_STATE, true);  // House occupied
        initialState.put(IoTValues.DOOR_STATE, false);     // Door closed
        initialState.put(IoTValues.ALARM_STATE, true);     // Alarm active

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // The door should remain closed; no break-in detected as house is occupied
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "The door should remain closed when house is occupied and alarm is active");
    }

    @Test
    public void testDoorOpenWithHouseVacant() {
        // House vacant, door open, alarm activated
        initialState.put(IoTValues.PROXIMITY_STATE, false);  // House vacant
        initialState.put(IoTValues.DOOR_STATE, true);        // Door open
        initialState.put(IoTValues.ALARM_STATE, true);       // Alarm activated

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // The alarm should sound because the house is vacant and door is open
        assertTrue((Boolean) newState.get(IoTValues.ALARM_ACTIVE), "Alarm should sound when door is open and house is vacant");
    }

    @Test
    public void testDoorClosedWithHouseVacant() {
        // House vacant, door closed, no alarm activated
        initialState.put(IoTValues.PROXIMITY_STATE, false);  // House vacant
        initialState.put(IoTValues.DOOR_STATE, false);       // Door closed
        initialState.put(IoTValues.ALARM_STATE, false);      // Alarm deactivated

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // The door should remain closed, no alarm should be triggered as the house is vacant
        assertFalse((Boolean) newState.get(IoTValues.ALARM_ACTIVE), "Alarm should not sound when house is vacant and door is closed");
    }

    @Test
    public void testInvalidAlarmPasscode() {
        // Invalid passcode to disable alarm
        initialState.put(IoTValues.PROXIMITY_STATE, true);  // House occupied
        initialState.put(IoTValues.ALARM_STATE, true);      // Alarm active
        initialState.put(IoTValues.GIVEN_PASSCODE, "wrong"); // Incorrect passcode
        initialState.put(IoTValues.ALARM_PASSCODE, "Testing1!"); // Correct passcode

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // The alarm should stay active, since the passcode is incorrect
        assertTrue((Boolean) newState.get(IoTValues.ALARM_ACTIVE), "Alarm should stay active when the wrong passcode is entered");
    }

    @Test
    public void testValidAlarmPasscode() {
        // Valid passcode to disable alarm
        initialState.put(IoTValues.PROXIMITY_STATE, true);  // House occupied
        initialState.put(IoTValues.ALARM_STATE, true);      // Alarm active
        initialState.put(IoTValues.GIVEN_PASSCODE, "Testing1!"); // Correct passcode
        initialState.put(IoTValues.ALARM_PASSCODE, "Testing1!"); // Correct passcode

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // The alarm should be disabled since the correct passcode is entered
        assertFalse((Boolean) newState.get(IoTValues.ALARM_ACTIVE), "Alarm should be disabled when the correct passcode is entered");
    }

    @Test
    public void testHouseVacantAutoTimerTriggered() {
        // House vacant, away timer should trigger
        initialState.put(IoTValues.PROXIMITY_STATE, false);  // House vacant
        initialState.put(IoTValues.AWAY_TIMER, false);       // Away timer not set
        initialState.put(IoTValues.LIGHT_STATE, true);       // Light on
        initialState.put(IoTValues.DOOR_STATE, true);        // Door open

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Away timer should be triggered, turning off light and door, and enabling alarm
        assertTrue((Boolean) newState.get(IoTValues.AWAY_TIMER), "Away timer should be triggered when the house is vacant");
        assertFalse((Boolean) newState.get(IoTValues.LIGHT_STATE), "Light should be turned off when the away timer is triggered");
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should be closed when the away timer is triggered");
        assertTrue((Boolean) newState.get(IoTValues.ALARM_STATE), "Alarm should be enabled when the away timer is triggered");
    }

    @Test
    public void testEdgeCaseExtremeTemperature() {
        // Edge case: Extremely high or low temperature
        initialState.put(IoTValues.TEMP_READING, -100);       // Extremely low temperature
        initialState.put(IoTValues.TARGET_TEMP, 22);          // Target temperature
        initialState.put(IoTValues.HUMIDIFIER_STATE, false);  // Humidifier off
        initialState.put(IoTValues.HEATER_STATE, false);      // Heater off

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Assert: The heater should be turned on in response to extreme cold
        assertTrue((Boolean) newState.get(IoTValues.HEATER_STATE), "Heater should be turned on when temperature is extremely low");
    }
}
