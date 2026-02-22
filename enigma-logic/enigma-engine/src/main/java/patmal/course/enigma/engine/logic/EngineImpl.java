package patmal.course.enigma.engine.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.course.enigma.component.keyboard.Keyboard;
import patmal.course.enigma.component.plugboard.Plugboard;
import patmal.course.enigma.component.plugboard.PlugboardImpl;
import patmal.course.enigma.component.reflector.Reflector;
import patmal.course.enigma.component.rotor.Rotor;
import patmal.course.enigma.component.rotor.RotorManager;
import patmal.course.enigma.engine.logic.dto.EnigmaConfiguration;
import patmal.course.enigma.engine.logic.dto.EnigmaMessage;
import patmal.course.enigma.engine.logic.dto.RotorLetterAndNotch;
import patmal.course.enigma.engine.logic.repository.Repository;
import patmal.course.enigma.machine.Machine;
import patmal.course.enigma.machine.MachineImpl;

import java.util.*;

public class EngineImpl implements Engine {
    private Machine machine;
    private final Repository repository;

    public static final Logger logger = LogManager.getLogger(EngineImpl.class);

    public EngineImpl(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void setMachineCode(List<Integer> rotorIds, List<Character> positions, String reflectorId, Map<Character, Character> plugBoardConfig) {
        
        logger.info("Setting machine code with reflector ID: {}, rotor IDs: {}, positions of rotors: {}, plugboard config: {}",
                reflectorId, rotorIds, positions, formatPlugBoardConfig(plugBoardConfig));
        
        if (!repository.getAllReflectors().containsKey(reflectorId)) {
            throw new IllegalArgumentException("Invalid reflector ID: " + reflectorId);
        }
        Reflector reflector = repository.getAllReflectors().get(reflectorId);
        List<Rotor> currentRotors = new ArrayList<>();
        for (Integer rotorId : rotorIds) {
            if (!repository.getAllRotors().containsKey(rotorId)) {
                throw new IllegalArgumentException("Invalid rotor ID: " + rotorId);
            }
            Rotor rotor = repository.getAllRotors().get(rotorId);
            currentRotors.add(rotor);
        }

        List<Integer> positionIndices = new ArrayList<>();
        for (Character posChar : positions) {
            if (!repository.getKeyboard().isValidChar(posChar)) {
                throw new IllegalArgumentException("Invalid rotor position: " + posChar);
            }
            int index = repository.getKeyboard().charToIndex(posChar);
            positionIndices.add(index);
        }

        if(plugBoardConfigNotValid(plugBoardConfig, repository.getKeyboard())) {
            throw new IllegalArgumentException("Invalid plugBoard configuration.");
        }

        RotorManager rotorManager = new RotorManager(currentRotors, positionIndices, rotorIds);
        Plugboard plugboard = new PlugboardImpl(plugBoardConfig);
        this.machine = new MachineImpl(reflector, rotorManager, repository.getKeyboard(), plugboard);

        logger.info("Machine code set successfully.");
    }

    @Override
    public void setAutomaticCode() {
        logger.info("Setting automatic machine code...");
        if(repository == null)
            throw new IllegalStateException("Machine is not loaded yet. Please load an XML file first.");
        // random parameters
        List<Integer> randomRotorIds = repository.getRandomRotorIds();
        logger.debug("Randomly selected rotor IDs: {}", randomRotorIds);

        List<Character> randomRotorStartPositions = repository.getRandomPositionsForRotors(randomRotorIds.size());
        logger.debug("Randomly selected rotor starting positions: {}", randomRotorStartPositions);
        String randomReflectorId = repository.getRandomReflectorId();
        logger.debug("Randomly selected reflector ID: {}", randomReflectorId);
        Map<Character, Character> plugBoardConfig = createRandomPlugBoardConfig();
        logger.debug("Randomly generated plugboard configuration: {}", formatPlugBoardConfig(plugBoardConfig));
        setMachineCode(randomRotorIds, randomRotorStartPositions, randomReflectorId, plugBoardConfig);
    }

    @Override
    public EnigmaMessage processInput(String inputString) {

        logger.info("Processing input string: {}", inputString);
        if(!isStringAbleToBeCrypt(inputString)) {
            throw new IllegalArgumentException("Input string contains invalid characters not present in the machine's keyboard.");
        }

        StringBuilder outputString = new StringBuilder();
        long startTime = System.nanoTime();

        for (char inputChar : inputString.toCharArray()) {
            char outputChar = machine.encryptChar(inputChar);
            outputString.append(outputChar);
            logger.debug("Encrypted character: {} --> {}", inputChar, outputChar);
        }
        long endTime = System.nanoTime();
        long time = endTime - startTime;

        logger.info("Input processing complete. Output string: {}, Time taken (ns): {}", outputString, time);
        return new EnigmaMessage(inputString, outputString.toString(), time);
    }


    private boolean plugBoardConfigNotValid(Map<Character, Character> plugBoardConfig, Keyboard keyboard) {
        Set<Character> validatedChars = new HashSet<>();

        for (Map.Entry<Character, Character> entry : plugBoardConfig.entrySet()) {
            char charA = entry.getKey();
            char charB = entry.getValue();
            // Since the map contains A->B and B->A, we skip the entry where the key is alphabetically larger.
            if (charA > charB) {
                continue;
            }

            if (!keyboard.isValidChar(charA)) {
                logger.error("Invalid plugboard configuration. Character outside of alphabet: {}", charA);
                return true;
            }
            if (!keyboard.isValidChar(charB)) {
                logger.error("Invalid plugboard configuration. Character outside of alphabet: {}", charB);
                return true;
            }
            // 2. Check for self-mapping (A -> A)
            if (charA == charB) {
                logger.error("Invalid plugboard configuration. Self-mapping detected for character: {}", charA);
                // This case should ideally be caught in the UI layer, but checked here for safety.
                return true;
            }

            if(validatedChars.contains(charA)) {
                logger.error("Invalid plugboard configuration. Character already mapped: {}", charA);
                return true;
            }

            if(validatedChars.contains(charB)) {
                logger.error("Invalid plugboard configuration. Character already mapped: {}", charB);
                return true;
            }

            // Mark the connection as validated and used
            validatedChars.add(charA);
            validatedChars.add(charB);
            logger.trace("Validated plugboard connection: {} <-> {}", charA, charB);
        }

        // If the loop completes without returning true, the configuration is valid.
        return false;
    }

    private Map<Character, Character> createRandomPlugBoardConfig() {

        String alphabet = repository.getKeyboard().toString();
        Random random = new Random();
        Map<Character, Character> plugboardMap = new HashMap<>();

        List<Character> availableChars = new ArrayList<>();
        for (char c : alphabet.toCharArray()) {
            availableChars.add(c);
        }

        int maxNumOfPairs = availableChars.size() / 2;
        int randomNumOfPairs = random.nextInt(maxNumOfPairs + 1); // +1 to include the maximum

        logger.debug("Generating Plugboard with {} pairs...", randomNumOfPairs);
        // 4. Loop N times to create the pairs
        for (int i = 0; i < randomNumOfPairs; i++) {

            int indexA = random.nextInt(availableChars.size());
            Character charA = availableChars.remove(indexA);

            int indexB = random.nextInt(availableChars.size());
            Character charB = availableChars.remove(indexB);

            plugboardMap.put(charA, charB);
            plugboardMap.put(charB, charA);
            logger.trace("Created plugboard pair: {} <-> {}", charA, charB);

        }
        return plugboardMap;
    }

    private boolean isStringAbleToBeCrypt(String inputString) {
        for (char c : inputString.toCharArray()) {
            if (!repository.getKeyboard().isValidChar(c)) {
                return false;
            }
        }
        return true;
    }

    private String formatPlugBoardConfig(Map<Character, Character> plugBoardConfig) {
        if (plugBoardConfig == null || plugBoardConfig.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        Set<Character> processed = new HashSet<>();
        List<Character> keys = new ArrayList<>(plugBoardConfig.keySet());
        Collections.sort(keys);

        boolean first = true;
        for (Character key : keys) {
            if (processed.contains(key)) {
                continue;
            }
            Character value = plugBoardConfig.get(key);
            processed.add(key);
            if (value != null) {
                processed.add(value);
            }

            if (!first) {
                sb.append(", ");
            }
            sb.append(key).append("=").append(value);
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}