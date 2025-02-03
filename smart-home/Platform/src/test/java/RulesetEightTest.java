package tartan.tests.rulesetEight;

import java.util.Map;
import java.lang.StringBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import tartan.tests.util.Utility;
import tartan.smarthome.resources.StaticTartanStateEvaluator;
import tartan.smarthome.resources.iotcontroller.IoTValues;

class RulesetEightTest {
    @Test
    void rulsetEightDefaultState() {
        var log = new StringBuffer();
        var evaluator = new StaticTartanStateEvaluator();
        Map<String, Object> customState = new Utility().createDefaultState();

        // Set the house to vacant and trigger disabling the alarm
        customState.put(IoTValues.PROXIMITY_STATE, false);
        customState.put(IoTValues.ALARM_STATE, false);

        var evaluatedState = evaluator.evaluateState(customState, log);

        // By ruleset 8, it cannot be disabled when no one is home
        assertEquals((boolean)evaluatedState.get(IoTValues.ALARM_STATE), true);

        // Should be able to turn it off once someone is home
        customState.put(IoTValues.PROXIMITY_STATE, true);
        customState.put(IoTValues.ALARM_STATE, false);

        evaluatedState = evaluator.evaluateState(customState, log);

        assertEquals((boolean)evaluatedState.get(IoTValues.ALARM_STATE), false);
    }
}
