package tartan.tests.rulesetEight;

import java.util.Map;
import java.lang.StringBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import tartan.tests.util.Utility;
import tartan.smarthome.resources.StaticTartanStateEvaluator;
import tartan.smarthome.resources.iotcontroller.IoTValues;

class RulesetOneTest {
    @Test
    void rulsetOneDefaultState() {
        var log = new StringBuffer();
        var evaluator = new StaticTartanStateEvaluator();
        Map<String, Object> customState = new Utility().createDefaultState();

        // Set the house to vacant and trigger the light to be turned on
        customState.put(IoTValues.PROXIMITY_STATE, false);
        customState.put(IoTValues.LIGHT_STATE, true);

        var evaluatedState = evaluator.evaluateState(customState, log);

        // By ruleset 1, the light should not be able to be turned on.
        assertEquals((boolean)evaluatedState.get(IoTValues.LIGHT_STATE), false);

        // Set the house to non-vacant and trigger the light to be turned on
        customState.put(IoTValues.PROXIMITY_STATE, true);
        customState.put(IoTValues.LIGHT_STATE, true);

        evaluatedState = evaluator.evaluateState(customState, log);

        // This shouldn't be impacted by rulest 1, meaning the light is now on
        assertEquals((boolean)evaluatedState.get(IoTValues.LIGHT_STATE), true);
    }
}
