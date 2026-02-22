package patmal.course.enigma.component.reflector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.course.enigma.component.keyboard.Keyboard;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectorImpl implements Reflector, Serializable {

    private final String id;
    private final Map<Integer, Integer> wiring;
    private Keyboard keyboard;
    private static final Logger logger = LogManager.getLogger(ReflectorImpl.class);

    public ReflectorImpl(String id, List<ReflectedPositionsPair> rawPairs) {
        this.id = id;
        Map<Integer, Integer> finalWiring = new HashMap<>();

        logger.debug("setting the wiring for reflector id: {}...", id);
        for (ReflectedPositionsPair pair : rawPairs) {
            int input = pair.getInput();
            int output = pair.getOutput();

            // Adds the mapping: Input -> Output
            finalWiring.put(input, output);

            // Adds the reciprocal mapping: Output -> Input
            finalWiring.put(output, input);
            logger.trace("mapped positions: {} <-> {}", input, output);
        }

        this.wiring = Collections.unmodifiableMap(finalWiring);
    }

    public String getId() {
        return id;
    }

    @Override
    public int reflect(int inputIndex) {
        if (!wiring.containsKey(inputIndex)) {
            // This should ideally not happen if the input is a valid alphabet index,
            // but handles cases where the reflector is incomplete.
            throw new IllegalArgumentException("Reflector mapping not found for index: " + inputIndex);
        }
        int outputIndex = wiring.get(inputIndex);
        if (keyboard != null) {
            logger.trace("Reflector [ID: {}] | REFLECT | In-Index: {} ({}) -> Out-Index: {} ({})",
                    id, inputIndex, keyboard.indexToChar(inputIndex), outputIndex, keyboard.indexToChar(outputIndex));
        } else {
            logger.trace("Reflector [ID: {}] | REFLECT | In-Index: {} -> Out-Index: {}",
                    id, inputIndex, outputIndex);
        }
        return outputIndex;
    }

    @Override
    public void setKeyboard(Keyboard keyboard) {
        this.keyboard = keyboard;
    }
}
