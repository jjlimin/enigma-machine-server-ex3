package patmal.course.enigma.component.plugboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Map;

public class PlugboardImpl implements  Plugboard, Serializable {
    private final Map<Character, Character> wiringMap;
    public static final Logger logger = LogManager.getLogger(PlugboardImpl.class);

    public PlugboardImpl(Map<Character,Character> wiringMap) {
        this.wiringMap = wiringMap;
    }

    @Override
    public char substitute(char inputChar) {
        if(wiringMap.containsKey(inputChar)){
            logger.debug("Plugboard substitution: {} -> {}", inputChar, wiringMap.get(inputChar));
        }
        return wiringMap.getOrDefault(inputChar, inputChar);
    }

    @Override
    public Map<Character, Character> getWiringMap() {
        return wiringMap;
    }
}


