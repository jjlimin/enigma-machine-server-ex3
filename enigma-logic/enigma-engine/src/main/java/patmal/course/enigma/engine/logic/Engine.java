package patmal.course.enigma.engine.logic;

import patmal.course.enigma.engine.logic.dto.EnigmaResult;
import patmal.course.enigma.engine.logic.dto.MachineConfiguration;
import patmal.course.enigma.engine.logic.repository.Repository;

public interface Engine {
    EnigmaResult process(String input, Repository repository, MachineConfiguration config);
}