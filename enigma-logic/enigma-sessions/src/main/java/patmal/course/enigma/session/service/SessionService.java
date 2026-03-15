package patmal.course.enigma.session.service;

import org.springframework.stereotype.Service;
import patmal.course.enigma.dal.api.MachineRepository;
import patmal.course.enigma.engine.logic.Engine;
import patmal.course.enigma.engine.logic.EngineImpl;
import patmal.course.enigma.engine.logic.repository.Repository;
import patmal.course.enigma.session.EnigmaSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    private final Map<String, EnigmaSession> sessions = new ConcurrentHashMap<>();
    private final MachineRepository machineRepository;

    public SessionService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    public String createSession(String machineName) {
        if (!machineRepository.existsByName(machineName)) {
            throw new IllegalArgumentException("Machine not found: " + machineName);
        }

        String sessionId = UUID.randomUUID().toString();

        EnigmaSession newSession = new EnigmaSession();
        newSession.setSessionId(sessionId);
        newSession.setMachineName(machineName);

        sessions.put(sessionId, newSession);
        return sessionId;
    }

    public EnigmaSession getSession(String sessionId) {
        EnigmaSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session ID: " + sessionId + " does not exist");
        }
        return session;
    }

    public boolean exists(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public void deleteSession(String sessionId) {
        if (!exists(sessionId)) {
            throw new IllegalArgumentException("Session ID: " + sessionId + " does not exist");
        }
        sessions.remove(sessionId);
    }
}
