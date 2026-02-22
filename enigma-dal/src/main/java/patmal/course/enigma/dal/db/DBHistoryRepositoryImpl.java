package patmal.course.enigma.dal.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import patmal.course.enigma.dal.api.HistoryRepository;
import patmal.course.enigma.dal.db.jpa.JpaProcessingRepository;
import patmal.course.enigma.dal.dto.ProcessingPersistenceEntity;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DBHistoryRepositoryImpl implements HistoryRepository {
    private final JpaProcessingRepository jpaProcessingRepository;

    @Override
    public void save(ProcessingPersistenceEntity entity) {
        jpaProcessingRepository.save(entity);
    }

    @Override
    public List<ProcessingPersistenceEntity> getHistoryByMachine(String machineName) {
        return jpaProcessingRepository.findByMachineName(machineName);
    }
}