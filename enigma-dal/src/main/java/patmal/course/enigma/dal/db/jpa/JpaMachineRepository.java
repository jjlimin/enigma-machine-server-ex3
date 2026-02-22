package patmal.course.enigma.dal.db.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import patmal.course.enigma.dal.dto.MachinePersistenceEntity;
import java.util.Optional;
import java.util.UUID;

public interface JpaMachineRepository extends JpaRepository<MachinePersistenceEntity, UUID> {
    Optional<MachinePersistenceEntity> findByName(String name);
}
