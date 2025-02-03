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
     * Normal case: House is vacant and door should close.
     */
    @Test
    public void testVacantHouseClosesDoor() {
        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Assert that the door is closed when the house is vacant
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should be closed when the house is vacant");

        // Check appropriate log message exists
        assertTrue(log.toString().contains("Closed door because house vacant"), "Log should contain message about closing door due to vacancy");
    }

    /**
     * Boundary case: House is vacant and away timer is set to false.
     * This test verifies that the house will remain in a stable state even without the away timer passing.
     */
    @Test
    public void testVacantHouseClosesDoorWhenAwayTimerNotPassed() {
        initialState.put(IoTValues.AWAY_TIMER, false);
        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Assert that the door is closed
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should remain closed when the house is vacant");

        // Check appropriate log message exists
        assertTrue(log.toString().contains("Closed door because house vacant"), "Log should contain message about closing door due to vacancy");
    }

    /**
     * Boundary case: Away timer has passed, house vacant.
     * This test verifies that the door will be closed once the away timer has passed.
     */
    @Test
    public void testVacantHouseClosesDoorWithTimerPassed() {
        initialState.put(IoTValues.AWAY_TIMER, true);
        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Assert that the door is closed
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should be closed when the house is vacant and timer has passed");

        // Check appropriate log message exists
        assertTrue(log.toString().contains("Closed door because house vacant"), "Log should contain message about closing door due to vacancy after timer");
    }

    /**
     * Corner case: Invalid data for house vacancy state.
     * This tests how the system reacts to invalid input for the house vacancy state.
     */
    @Test
    public void testInvalidHouseVacancyState() {
        // Set the proximity state to a non-boolean value (invalid)
        initialState.put(IoTValues.PROXIMITY_STATE, "invalid_value");

        // Try to evaluate the state
        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Assert: The proximity state should not affect door status due to invalid value
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should remain closed despite invalid proximity state");

        // Check the log for any error messages regarding the invalid state
        assertTrue(log.toString().contains("Error: Invalid proximity state"), "Log should contain error for invalid proximity state");
    }

    /**
     * Randomized test: Simulate a series of random house vacancy states with different away timer settings.
     * This tests the system's handling of varied input over multiple iterations.
     */
    @Test
    public void testRandomizedVacancyAndTimer() {
        for (int i = 0; i < 100; i++) {
            boolean isVacant = Math.random() < 0.5; // Randomly determine if the house is vacant
            boolean isAwayTimer = Math.random() < 0.5; // Randomly determine if the away timer has passed

            // Update the state for each iteration
            initialState.put(IoTValues.PROXIMITY_STATE, isVacant);
            initialState.put(IoTValues.AWAY_TIMER, isAwayTimer);

            Map<String, Object> newState = evaluator.evaluateState(initialState, log);

            // Assert: Door state should always be closed when house is vacant
            if (isVacant) {
                assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should be closed when the house is vacant");
            } else {
                assertTrue((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should remain open when the house is not vacant");
            }

            // Check if the log is updated correctly
            assertTrue(log.toString().contains(isVacant ? "Closed door because house vacant" : "Door left open because house occupied"), "Log should reflect appropriate door action based on house vacancy state");
        }
    }

    /**
     * Invalid case: Test invalid away timer setting (non-boolean).
     * This test ensures that non-boolean values for away timer do not break the logic.
     */
    @Test
    public void testInvalidAwayTimer() {
        // Set away timer to an invalid value (non-boolean)
        initialState.put(IoTValues.AWAY_TIMER, "invalid_value");

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Assert that the door will be closed because the system should ignore invalid away timer values
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should be closed when the away timer is invalid");

        // Check for log message indicating invalid away timer input
        assertTrue(log.toString().contains("Error: Invalid away timer value"), "Log should contain message about invalid away timer value");
    }

    /**
     * Test case for boundary temperature reading: extremely low temperature.
     * This is to check that even if there are extreme environmental factors, the door closing behavior should be unaffected.
     */
    @Test
    public void testExtremeTemperatureLow() {
        initialState.put(IoTValues.TEMP_READING, -100);  // Extreme low temperature
        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Assert: Door should remain closed, regardless of temperature
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should remain closed even in extreme low temperatures");

        // Check appropriate log message
        assertTrue(log.toString().contains("Closed door because house vacant"), "Log should indicate door closing due to vacancy even with extreme temperature");
    }

    /**
     * Test case for boundary temperature reading: extremely high temperature.
     * Again, the door behavior should be independent of temperature.
     */
    @Test
    public void testExtremeTemperatureHigh() {
        initialState.put(IoTValues.TEMP_READING, 100);  // Extreme high temperature
        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Assert: Door should remain closed, regardless of temperature
        assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should remain closed even in extreme high temperatures");

        // Check appropriate log message
        assertTrue(log.toString().contains("Closed door because house vacant"), "Log should indicate door closing due to vacancy even with extreme temperature");
    }
}
