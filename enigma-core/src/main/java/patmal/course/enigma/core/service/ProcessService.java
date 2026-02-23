package patmal.course.enigma.core.service;

import org.springframework.stereotype.Service;
import patmal.course.enigma.core.dto.ProcessResponseDTO;
import patmal.course.enigma.core.formatter.EnigmaFormatter;
import patmal.course.enigma.dal.api.MachineRepository;
import patmal.course.enigma.engine.logic.Engine;
import patmal.course.enigma.engine.logic.dto.EnigmaResult;
import patmal.course.enigma.engine.logic.dto.MachineConfiguration;
import patmal.course.enigma.engine.logic.repository.Repository;
import patmal.course.enigma.session.EnigmaSession;
import patmal.course.enigma.session.service.SessionService;

@Service
public class ProcessService {
    private final SessionService sessionService;
    private final MachineRepository machineRepository;
    private final Engine engine;
    private final EnigmaFormatter formatter;

    public ProcessService(SessionService sessionService, MachineRepository machineRepository, Engine engine, EnigmaFormatter formatter) {
        this.sessionService = sessionService;
        this.machineRepository = machineRepository;
        this.engine = engine;
        this.formatter = formatter;
    }

    public ProcessResponseDTO processMessage(String sessionID, String input) {
        // 1. Get current state from Session
        EnigmaSession session = sessionService.getSession(sessionID);

        // 2. Get machine catalog from DB (Blueprint)
        Repository catalog = machineRepository.getMachineByName(session.getMachineName());

        // 3. Prepare config for the Stateless Engine
        MachineConfiguration config = MachineConfiguration.builder()
                .rotorIds(session.getCurrentRotorIds())
                .startingPositions(session.getCurrentPositions())
                .reflectorId(session.getCurrentReflectorId())
                .plugs(session.getCurrentPlugs())
                .build();

        // 4. Run the logic
        EnigmaResult result = engine.process(input, catalog, config);

        // 5. Capture the "snapshot" of the code before updating the session
        // This is the string like <1,2><A(3),A(1)><II><A|F> required by your schema
        String codeAtMomentOfEncryption = formatter.formatFullOriginalCode(session, catalog);

        // 6. UPDATE STATE: Save the new positions back to the session memory
        session.setCurrentPositions(result.getNewPositions());

        // 7. PERSISTENCE: Save to DB using the UUID-linked repository method
        machineRepository.saveProcessingEntry(
                session.getMachineName(),
                sessionID,
                input,
                result.getOutput(),
                codeAtMomentOfEncryption,
                result.getDurationInNs()
        );

        // 8. Return response with the updated notch-distance compact string
        return ProcessResponseDTO.builder()
                .output(result.getOutput())
                .currentRotorsPositionCompact(formatter.formatPositions(session.getCurrentRotorIds(), result.getNewPositions(), catalog))
                .build();
    }

}