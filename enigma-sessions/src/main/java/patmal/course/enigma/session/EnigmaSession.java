package patmal.course.enigma.session;

import lombok.AllArgsConstructor;
import lombok.Data;
import patmal.course.enigma.engine.logic.Engine;

@Data
@AllArgsConstructor
public class EnigmaSession {
    private final String sessionId;
    private final String machineName;
    private final Engine engine;
}
