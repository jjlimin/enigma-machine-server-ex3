package patmal.course.enigma.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnigmaSession {
    private String sessionId;
    private String machineName;

    // Persisted choices
    private List<Integer> currentRotorIds;
    private String currentReflectorId;
    private Map<Character, Character> currentPlugs;

    // Current dynamic state
    private List<Character> currentPositions;
    private List<Character> originalPositions;
}