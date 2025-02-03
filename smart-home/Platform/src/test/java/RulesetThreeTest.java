import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tartan.smarthome.resources.StaticTartanStateEvaluator;
import tartan.smarthome.resources.iotcontroller.IoTValues;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;

public class RulesetThreeTest {

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

    // Boundary test: House occupied, light off, alarm disabled
    @Test
    public void testHouseOccupiedAndLightOff() {
        initialState.put(IoTValues.PROXIMITY_STATE, true);  // House occupied
        initialState.put(IoTValues.LIGHT_STATE, false);    // Light off
        initialState.put(IoTValues.ALARM_STATE, false);    // Alarm disabled

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Light should be turned on because the house is occupied and alarm is off
        assertTrue((Boolean) newState.get(IoTValues.LIGHT_STATE), "Light should be turned on when house is occupied and alarm is disabled");
    }

    // Boundary test: House occupied, light already on, alarm disabled
    @Test
    public void testHouseOccupiedAndLightOn() {
        initialState.put(IoTValues.PROXIMITY_STATE, true);  // House occupied
        initialState.put(IoTValues.LIGHT_STATE, true);     // Light already on
        initialState.put(IoTValues.ALARM_STATE, false);    // Alarm disabled

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Light should remain on, as the house is occupied and alarm is off
        assertTrue((Boolean) newState.get(IoTValues.LIGHT_STATE), "Light should remain on when house is occupied and alarm is disabled");
    }

    // Boundary test: House vacant, door open, alarm activated
    @Test
    public void testDoorOpenWithHouseVacant() {
        initialState.put(IoTValues.PROXIMITY_STATE, false);  // House vacant
        initialState.put(IoTValues.DOOR_STATE, true);        // Door open
        initialState.put(IoTValues.ALARM_STATE, true);       // Alarm activated

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // The alarm should sound because the house is vacant and door is open
        assertTrue((Boolean) newState.get(IoTValues.ALARM_ACTIVE), "Alarm should sound when door is open and house is vacant");
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

    // House vacant, away timer should trigger
    @Test
    public void testAwayTimerTriggeredWhenHouseVacant() {
        initialState.put(IoTValues.PROXIMITY_STATE, false);  // House vacant
        initialState.put(IoTValues.AWAY_TIMER, false);       // Away timer not set
        initialState.put(IoTValues.LIGHT_STATE, true);       // Light on
        initialState.put(IoTValues.DOOR_STATE, true);        // Door open

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Away timer should be triggered, turning off light and door, and enabling alarm
        assertTrue((Boolean) newState.get(IoTValues.AWAY_TIMER), "Away timer should be triggered when the house is vacant");
        assertFalse((Boolean) newState.get(IoTValues.LIGHT_STATE), "Light should be turned off when the away timer is triggered");
        assertTrue((Boolean) newState.get(IoTValues.ALARM_STATE), "Alarm should be enabled when the away timer is triggered");
    }

    // Edge case: Extremely high or low temperature
    @Test
    public void testEdgeCaseExtremeTemperature() {
        initialState.put(IoTValues.TEMP_READING, -100);       // Extremely low temperature
        initialState.put(IoTValues.TARGET_TEMP, 22);          // Target temperature
        initialState.put(IoTValues.HUMIDIFIER_STATE, false);  // Humidifier off
        initialState.put(IoTValues.HEATER_STATE, false);      // Heater off

        Map<String, Object> newState = evaluator.evaluateState(initialState, log);

        // Assert: The heater should be turned on in response to extreme cold
        assertTrue((Boolean) newState.get(IoTValues.HEATER_STATE), "Heater should be turned on when temperature is extremely low");
    }

    @Test
    public void testRandomizedRuleSetThree() {
        // Reinitialize the log for each test to avoid potential state carryover
        StringBuffer log = new StringBuffer();

        Map<String, Object> randstate = new HashMap<>();
        Random rand = new Random();

        randstate.put(IoTValues.TEMP_READING, rand.nextInt(50));  // Random temp between 0 and 50
        randstate.put(IoTValues.HUMIDITY_READING, rand.nextInt(101));  // Random humidity between 0 and 100
        randstate.put(IoTValues.TARGET_TEMP, rand.nextInt(50));  // Random target temp between 0 and 50
        randstate.put(IoTValues.HUMIDIFIER_STATE, rand.nextBoolean());  // Random humidifier state
        randstate.put(IoTValues.DOOR_STATE, rand.nextBoolean());  // Random door state
        randstate.put(IoTValues.LIGHT_STATE, rand.nextBoolean());  // Random light state
        randstate.put(IoTValues.PROXIMITY_STATE, rand.nextBoolean());  // Random proximity state
        randstate.put(IoTValues.ALARM_STATE, rand.nextBoolean());  // Random alarm state
        randstate.put(IoTValues.HEATER_STATE, rand.nextBoolean());  // Random heater state
        randstate.put(IoTValues.CHILLER_STATE, rand.nextBoolean());  // Random chiller state
        randstate.put(IoTValues.HVAC_MODE, rand.nextBoolean() ? "HEATER" : "CHILLER");  // Random HVAC mode

        evaluator = new StaticTartanStateEvaluator();  // Reinitialize evaluator

        Map<String, Object> newState = evaluator.evaluateState(randstate, log);

        // Assertions based on the randomized state values for Rule Set Three
        if ((Boolean) newState.get(IoTValues.PROXIMITY_STATE) && !(Boolean) newState.get(IoTValues.ALARM_STATE)) {
            assertTrue((Boolean) newState.get(IoTValues.LIGHT_STATE), "Light should be on if house is occupied and alarm is off");
        } else if ((Boolean) newState.get(IoTValues.PROXIMITY_STATE) && (Boolean) newState.get(IoTValues.ALARM_STATE)) {
            assertFalse((Boolean) newState.get(IoTValues.LIGHT_STATE), "Light should be off if house is occupied and alarm is on");
        }

        // Validate HVAC mode based on temperature readings and target temperature
        if ((Integer) newState.get(IoTValues.TEMP_READING) < (Integer) newState.get(IoTValues.TARGET_TEMP)) {
            assertEquals("HEATER", newState.get(IoTValues.HVAC_MODE), "HVAC mode should be HEATER if the temperature is lower than the target");
        } else if ((Integer) newState.get(IoTValues.TEMP_READING) > (Integer) newState.get(IoTValues.TARGET_TEMP)) {
            assertEquals("CHILLER", newState.get(IoTValues.HVAC_MODE), "HVAC mode should be CHILLER if the temperature is higher than the target");
        }

        // Check if the door state is updated correctly based on proximity
        if ((Boolean) newState.get(IoTValues.PROXIMITY_STATE)) {
            assertTrue((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should remain open when proximity indicates presence");
        } else {
            assertFalse((Boolean) newState.get(IoTValues.DOOR_STATE), "Door should be closed when no presence is detected");
        }
    }
}
