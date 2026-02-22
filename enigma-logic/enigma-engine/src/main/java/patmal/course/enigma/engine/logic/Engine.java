package patmal.course.enigma.engine.logic;

import patmal.course.enigma.engine.logic.dto.EnigmaMessage;

import java.util.List;
import java.util.Map;

public interface Engine {
    void setMachineCode(List<Integer> rotorIds, List<Character> positions, String reflectorId, Map<Character, Character> plugboardConfig);
    void setAutomaticCode();
    EnigmaMessage processInput(String inputString);
}