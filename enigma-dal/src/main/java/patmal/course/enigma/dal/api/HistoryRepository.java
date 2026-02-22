package patmal.course.enigma.dal.api;

import patmal.course.enigma.dal.dto.ProcessingPersistenceEntity;
import java.util.List;

public interface HistoryRepository {
    void save(ProcessingPersistenceEntity entity);
    List<ProcessingPersistenceEntity> getHistoryByMachine(String machineName);
}