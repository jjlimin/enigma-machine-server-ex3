package patmal.course.enigma.core.service;

import org.springframework.stereotype.Service;
import patmal.course.enigma.core.dto.*;
import patmal.course.enigma.core.formatter.EnigmaFormatter;
import patmal.course.enigma.dal.api.MachineRepository;
import patmal.course.enigma.engine.logic.repository.Repository;
import patmal.course.enigma.session.EnigmaSession;
import patmal.course.enigma.session.service.SessionService;
import java.util.*;

@Service
public class ConfigService {
    private final SessionService sessionService;
    private final MachineRepository machineRepository; // To fetch the catalog for validation
    private final EnigmaFormatter formatter;

    public ConfigService(SessionService sessionService, MachineRepository machineRepository, EnigmaFormatter formatter) {
        this.sessionService = sessionService;
        this.machineRepository = machineRepository;
        this.formatter = formatter;
    }

    public MachineConfigResponseDTO getCurrentStatus(String sessionID, boolean verbose) throws IllegalAccessException {
        EnigmaSession session = sessionService.getSession(sessionID);
        if (session.getCurrentPositions() == null) {
            throw new IllegalAccessException("this session has no machine configuration");
        }

        Repository catalog = machineRepository.getMachineByName(session.getMachineName());
        int processedCount = machineRepository.getProcessedMessageCount(session.getMachineName());

        MachineConfigResponseDTO.MachineConfigResponseDTOBuilder builder = MachineConfigResponseDTO.builder()
                .totalRotors(catalog.getAllRotors().size())
                .totalReflectors(catalog.getAllReflectors().size())
                .totalProcessedMessages(processedCount)
                .originalCodeCompact(formatter.formatFullOriginalCode(session, catalog))
                .currentRotorsPositionCompact(formatter.formatPositions(session.getCurrentRotorIds(), session.getCurrentPositions(), catalog));
                //.currentRotorsPositionCompact(formatter.formatPositions(session.getCurrentRotorIds(), session.getOriginalPositions(), catalog));

        // If verbose is true, build the complex nested objects
        if (verbose) {
            builder.originalCode(buildCodeStructure(
                    session.getCurrentRotorIds(),
                    session.getOriginalPositions(),
                    session.getCurrentReflectorId(),
                    session.getCurrentPlugs(),
                    catalog));

            builder.currentRotorsPosition(buildCodeStructure(
                    session.getCurrentRotorIds(),
                    session.getCurrentPositions(),
                    null, // reflector not needed for current pos in your example
                    null, // plugs not needed for current pos in your example
                    catalog));
        }

        return builder.build();
    }

    private EnigmaCodeStructure buildCodeStructure(List<Integer> ids, List<Character> positions, String reflector, Map<Character, Character> plugs, Repository catalog) {
        List<RotorStatusDTO> rotorStatuses = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            char pos = positions.get(i);

            // Fetch the actual Rotor component to get the notch position
            var rotorComponent = catalog.getAllRotors().get(id);
            int currentIdx = catalog.getKeyboard().charToIndex(pos);

            // Calculate distance to notch: (Notch - Current + AlphabetSize) % AlphabetSize
            int distance = (rotorComponent.getNotchPosition() - currentIdx + catalog.getKeyboard().getAlphabetLength())
                    % catalog.getKeyboard().getAlphabetLength();

            rotorStatuses.add(RotorStatusDTO.builder()
                    .rotorNumber(id)
                    .rotorPosition(pos)
                    .notchDistance(distance + 1)
                    .build());
        }

        return EnigmaCodeStructure.builder()
                .rotors(rotorStatuses)
                .reflector(reflector)
                .plugs(plugs == null ? null : convertToPlugList(plugs))
                .build();
    }

    private List<PlugConnection> convertToPlugList(Map<Character, Character> plugs) {
        return plugs.entrySet().stream()
                .filter(e -> e.getKey() < e.getValue())
                .map(e -> new PlugConnection(e.getKey().toString(), e.getValue().toString()))
                .toList();
    }

    public void setManualConfiguration(EnigmaManualConfigRequest request) {
        EnigmaSession session = sessionService.getSession(request.getSessionID());

        // Fetch the catalog from the DB to perform validation
        Repository catalog = machineRepository.getMachineByName(session.getMachineName());

        // Step 1: Validate the request against the catalog
        validateConfiguration(request, catalog);
        
        // 1. Logic: Extract IDs and positions from the request
        List<Integer> rotorIds = request.getRotors().stream()
                .map(RotorSelection::getRotorNumber).toList();
        List<Character> positions = request.getRotors().stream()
                .map(RotorSelection::getRotorPosition).toList();

        // 2. State Update: Save to session memory
        session.setCurrentRotorIds(rotorIds);
        session.setCurrentPositions(new ArrayList<>(positions));
        session.setOriginalPositions(new ArrayList<>(positions)); // For Reset flow
        session.setCurrentReflectorId(request.getReflector());
        session.setCurrentPlugs(mapPlugs(request.getPlugs()));
    }

    private void validateConfiguration(EnigmaManualConfigRequest request, Repository catalog) {
        // 1. Validate Rotor Count
        if (request.getRotors().size() != catalog.getNumOfUsedRotorsInMachine()) {
            throw new IllegalArgumentException("Invalid number of rotors. Expected: " +
                    catalog.getNumOfUsedRotorsInMachine() + ", but got: " + request.getRotors().size());
        }

        // 2. Validate Individual Rotors and Positions
        for (RotorSelection selection : request.getRotors()) {
            if (!catalog.getAllRotors().containsKey(selection.getRotorNumber())) {
                throw new IllegalArgumentException("Rotor ID " + selection.getRotorNumber() + " does not exist in this machine.");
            }
            if (!catalog.getKeyboard().isValidChar(selection.getRotorPosition())) {
                throw new IllegalArgumentException("Invalid start position '" + selection.getRotorPosition() +
                        "' for rotor " + selection.getRotorNumber() + ".");
            }
        }

        // 3. Validate Reflector
        if (!catalog.getAllReflectors().containsKey(request.getReflector())) {
            throw new IllegalArgumentException("Reflector ID '" + request.getReflector() + "' does not exist in this machine.");
        }

        // 4. Validate Plugs
        if (request.getPlugs() != null) {
            for (PlugConnection plug : request.getPlugs()) {
                char p1 = plug.getPlug1().toUpperCase().charAt(0);
                char p2 = plug.getPlug2().toUpperCase().charAt(0);
                if (!catalog.getKeyboard().isValidChar(p1) || !catalog.getKeyboard().isValidChar(p2)) {
                    throw new IllegalArgumentException("Plugboard contains characters not in the keyboard alphabet.");
                }
            }
        }
    }

    public void setAutomaticConfiguration(String sessionID) {
        EnigmaSession session = sessionService.getSession(sessionID);

        // Fetch the machine catalog from the DB to generate valid random parts
        Repository catalog = machineRepository.getMachineByName(session.getMachineName());

        // Use the existing logic in the Repository to pick random valid settings
        List<Integer> randomRotorIds = catalog.getRandomRotorIds();
        List<Character> randomPositions = catalog.getRandomPositionsForRotors(randomRotorIds.size());

        session.setCurrentRotorIds(randomRotorIds);
        session.setCurrentPositions(new ArrayList<>(randomPositions));
        session.setOriginalPositions(new ArrayList<>(randomPositions));
        session.setCurrentReflectorId(catalog.getRandomReflectorId());
        session.setCurrentPlugs(new HashMap<>()); // Typically empty for auto-setup
    }

    public void resetMachine(String sessionID) {
        EnigmaSession session = sessionService.getSession(sessionID);
        // Simply overwrite current positions with the saved original ones
        session.setCurrentPositions(new ArrayList<>(session.getOriginalPositions()));
    }

    private Map<Character, Character> mapPlugs(List<PlugConnection> plugs) {
        Map<Character, Character> plugMap = new HashMap<>();
        if (plugs != null) {
            for (PlugConnection p : plugs) {
                plugMap.put(p.getPlug1().toUpperCase().charAt(0), p.getPlug2().toUpperCase().charAt(0));
                plugMap.put(p.getPlug2().toUpperCase().charAt(0), p.getPlug1().toUpperCase().charAt(0));
            }
        }
        return plugMap;
    }

}