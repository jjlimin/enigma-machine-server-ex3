package patmal.course.enigma.core.service;

import org.springframework.stereotype.Service;
import patmal.course.enigma.core.dto.HistoryEntry;
import patmal.course.enigma.dal.db.jpa.JpaProcessingRepository;
import patmal.course.enigma.dal.dto.ProcessingPersistenceEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    private final JpaProcessingRepository processingRepository;

    public HistoryService(JpaProcessingRepository processingRepository) {
        this.processingRepository = processingRepository;
    }

    public Map<String, List<HistoryEntry>> getHistory(String sessionID, String machineName) {

        if ((sessionID == null && machineName == null) || (sessionID != null && machineName != null)) {
            throw new IllegalArgumentException("Exactly one of sessionID or machineName must be provided");
        }

        List<ProcessingPersistenceEntity> entities;

        // Fetch from DB based on provided parameter
        if (sessionID != null) {
            entities = processingRepository.findBySessionId(sessionID);
            if (entities.isEmpty()) {
                throw new IllegalArgumentException("No history found for the provided sessionID");
            }
        } else {
            entities = processingRepository.findByMachineName(machineName);
            if (entities.isEmpty()) {
                throw new IllegalArgumentException("No history found for the provided  machineName");
            }
        }

        // Group by the 'code' snapshot and map to DTOs
        return entities.stream().collect(Collectors.groupingBy(
                ProcessingPersistenceEntity::getCode,
                Collectors.mapping(entity -> HistoryEntry.builder()
                                .input(entity.getInput())
                                .output(entity.getOutput())
                                .duration(entity.getTime())
                                .build(),
                        Collectors.toList())
        ));
    }
}