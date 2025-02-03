package tartan.unit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tartan.smarthome.resources.iotcontroller.IoTValues;
import tartan.smarthome.resources.StaticTartanStateEvaluator;

import java.util.*;

class RulesetThreeTest {

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

    /**
     * Test to ensure the door closes when the house is vacant.
     * This test checks the functionality where the door should close
     * if the house is vacant and no timer has been set.
     */
    @Test
    public void testVacantHouseClosesDoor() {
        Map<String, Object> newState = evaluateStateSafely(initialState, log);

        // Assert that the door is closed when the house is vacant
        assertNotNull(newState, "State should not be null");
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should be closed when the house is vacant");

        // Check appropriate log message exists
        assertTrue(log.toString().contains("Closed door because house vacant"), "Log should contain message about closing door due to vacancy");
    }

    /**
     * Test to ensure the door is closed when the house is vacant, even when the away timer has not passed.
     * This test verifies that the door will still close due to the house vacancy state alone.
     */
    @Test
    public void testVacantHouseClosesDoorWhenAwayTimerNotPassed() {
        initialState.put(IoTValues.AWAY_TIMER, false);
        Map<String, Object> newState = evaluateStateSafely(initialState, log);

        // Assert that the door is closed
        assertNotNull(newState, "State should not be null");
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should remain closed when the house is vacant");

        // Check appropriate log message exists
        assertTrue(log.toString().contains("Closed door because house vacant"), "Log should contain message about closing door due to vacancy");
    }

    /**
     * Test to check the door closing when the house is vacant after a certain period of time.
     * This test assumes that the away timer has passed and the door should be closed as a result.
     */
    @Test
    public void testVacantHouseClosesDoorWithTimerPassed() {
        initialState.put(IoTValues.AWAY_TIMER, true);
        Map<String, Object> newState = evaluateStateSafely(initialState, log);

        // Assert that the door is closed
        assertNotNull(newState, "State should not be null");
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should be closed when the house is vacant and timer has passed");

        // Check appropriate log message exists
        assertTrue(log.toString().contains("Closed door because house vacant"), "Log should contain message about closing door due to vacancy after timer");
    }

    /**
     * Test for invalid house vacancy state: Testing how the system reacts when
     * invalid data is provided for the proximity state (e.g., a string instead of a boolean).
     */
    @Test
    public void testInvalidHouseVacancyState() {
        // Set the proximity state to an invalid value
        initialState.put(IoTValues.PROXIMITY_STATE, "invalid_value");

        // Evaluate the state and check for graceful handling
        Map<String, Object> newState = evaluateStateSafely(initialState, log);

        assertNotNull(newState, "State should not be null");

        // Assert that the door remains closed despite invalid input for proximity state
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should remain closed despite invalid proximity state");

        // Check the log for error or warning about invalid data
        assertTrue(log.toString().contains("Error: Invalid proximity state"), "Log should contain an error for invalid proximity state");
    }

    /**
     * A helper method to evaluate the state safely and avoid ClassCastException.
     * It catches exceptions and returns a default state if any errors occur.
     */
    private Map<String, Object> evaluateStateSafely(Map<String, Object> state, StringBuffer log) {
        try {
            return evaluator.evaluateState(state, log);
        } catch (ClassCastException e) {
            log.append("Error: Invalid data type in state evaluation").toString();
            return new HashMap<>(); // return a default empty state or previous state if you have one
        } catch (Exception e) {
            log.append("Unexpected error: " + e.getMessage()).toString();
            return new HashMap<>(); // return a default empty state or previous state if you have one
        }
    }
}
