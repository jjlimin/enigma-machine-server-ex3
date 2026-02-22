package patmal.course.enigma.dal.db.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import patmal.course.enigma.dal.dto.ProcessingPersistenceEntity;
import java.util.List;
import java.util.UUID;

public interface JpaProcessingRepository extends JpaRepository<ProcessingPersistenceEntity, UUID> {
    // Helpful for the History flow later
    List<ProcessingPersistenceEntity> findBySessionId(String sessionId);
    List<ProcessingPersistenceEntity> findByMachineName(String machineName);
}