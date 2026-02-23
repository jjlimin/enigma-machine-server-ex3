package patmal.course.enigma.engine.logic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class MachineConfiguration {
    private final List<Integer> rotorIds;
    private final List<Character> startingPositions;
    private final String reflectorId;
    private final Map<Character, Character> plugs;
}