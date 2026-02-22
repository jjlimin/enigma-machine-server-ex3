package patmal.course.enigma.engine.logic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class EnigmaResult {
    private final String input;
    private final String output;
    private final List<Character> newPositions; // The state to be saved after rotation
    private final long durationInNs;
}