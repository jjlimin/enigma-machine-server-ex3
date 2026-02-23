package patmal.course.enigma.dal.api;

import patmal.course.enigma.dal.dto.MachinePersistenceEntity;
import patmal.course.enigma.engine.logic.repository.Repository;

public interface MachineRepository {
    MachinePersistenceEntity save(Repository repository);
    boolean existsByName(String name);
    Repository getMachineByName(String name);
    int getProcessedMessageCount(String machineName);
    void saveProcessingEntry(String machineName, String sessionID, String input, String output, String code, Long duration);
}
