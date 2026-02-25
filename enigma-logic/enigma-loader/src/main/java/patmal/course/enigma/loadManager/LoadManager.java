package patmal.course.enigma.loadManager;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import patmal.course.enigma.component.keyboard.Keyboard;
import patmal.course.enigma.component.keyboard.KeyboardImpl;
import patmal.course.enigma.component.reflector.ReflectedPositionsPair;
import patmal.course.enigma.component.reflector.Reflector;
import patmal.course.enigma.component.reflector.ReflectorImpl;
import patmal.course.enigma.component.reflector.RomanValues;
import patmal.course.enigma.component.rotor.Rotor;
import patmal.course.enigma.component.rotor.RotorImpl;
import patmal.course.enigma.engine.logic.repository.Repository;
import patmal.course.enigma.loader.schema.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LoadManager implements Serializable {
    public static final Logger logger = LogManager.getLogger(LoadManager.class);
    private final static String JAXB_XML_GAME_PACKAGE_NAME = "patmal.course.enigma.loader.schema";

    private static BTEEnigma deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_GAME_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return (BTEEnigma) u.unmarshal(in);
    }

    // argument was string now it is inputstream
    public Repository loadMachineSettingsFromXML(InputStream inputStream) throws JAXBException, FileNotFoundException {
        try {
            // extracting bteEnigma (root of the xml tree)
            logger.debug("Serializing XML file to BTEEnigma object...");
            BTEEnigma bteEnigma = deserializeFrom(inputStream);
            logger.debug("BTEEnigma object created successfully.");

            logger.debug("Number of rotors to be used in the machine set to: {}", bteEnigma.getRotorsCount().intValue());

            String name = bteEnigma.getName();
            logger.debug("Machine name extracted from XML: {}", name);
            // extracting keyboard
            String abcForKeyboard = bteEnigma.getABC().trim().toUpperCase();
            Set<Character> characterSet = abcForKeyboard.chars()
                    .mapToObj(c -> (char)c)
                    .collect(Collectors.toSet());
            // validation for abcForKeyboard
            if (characterSet.size() < abcForKeyboard.length()) {
                logger.error("Repeated character found in XML file");
                throw new IllegalArgumentException("Repeated character found in XML file");
            }
            if (abcForKeyboard.length() % 2 == 1) {
                logger.error("The ABC length must be even, but got: {} chars.", abcForKeyboard.length());
                throw new IllegalArgumentException("The ABC length must be even, but got: " + abcForKeyboard.length() + " chars.");
            }
            Keyboard keyboard = createKeyboard(abcForKeyboard);
            logger.debug("Keyboard created successfully. the ABC is: {}", abcForKeyboard);

            // extracting reflectors
            logger.debug("Creating reflectors from XML...");
            BTEReflectors bteReflectors = bteEnigma.getBTEReflectors();
            Map<String, Reflector> allReflectors = createReflectorsMap(bteReflectors, keyboard);
            logger.debug("Reflectors successfully created from XML.");

            // extracting rotors
            logger.debug("Creating rotors from XML...");
            BTERotors bteRotors = bteEnigma.getBTERotors();
            Map<Integer, Rotor> allRotors = createRotorsMap(bteRotors, keyboard, bteEnigma.getRotorsCount().intValue());
            logger.debug("Rotors successfully created from XML.");

            // creating the repository
            logger.debug("initializing Repository with created components...");
            return new Repository(name, allRotors, allReflectors, keyboard, bteEnigma.getRotorsCount().intValue());

        } catch (JAXBException e) {
            throw new JAXBException("Something went wrong. Please check if your XML is valid because it looks like its not, check for invalid characters (like &) or wrong structure.");
        }
    }

    private int getId(Set<Integer> idSet, int id){

        // finds duplicated id
        if(idSet.contains(id)){ throw new IllegalArgumentException("Rotor ID must be unique, but got a duplicate ID: " + id); }

        // adds the unique id
        idSet.add(id);

        return id;
    }

    private int getNotch(int rotorId, Set<Integer> notchSet, int notch, Keyboard keyboard) {
//        if(notch < 0 || notch >= keyboard.getAlphabetLength()){
//            int size = keyboard.getAlphabetLength() - 1;
//            throw new IllegalArgumentException("Rotor notch must be between 0 and the length of the abc size minus one, which is currently " + size + ", but got " + notch + " for Rotor ID " + rotorId + ".");
//        }
        if(notch <= 0 || notch > keyboard.getAlphabetLength()){
            int size = keyboard.getAlphabetLength();
            throw new IllegalArgumentException("Rotor notch must be between 0 and the length of the abc size minus one, which is currently " + size + ", but got " + notch + " for Rotor ID " + rotorId + ".");
        }
        return notch;
    }

    private void checkIfDuplicateCharacterInColumn(List<Character> abcInColumn, BTEPositioning positioning, int id){
        if(!abcInColumn.contains(positioning.getLeft().toUpperCase().charAt(0))){
            throw new IllegalArgumentException("Rotor ID " + id + " character in the left column '" + positioning.getLeft().charAt(0) + "' is mapped more than once.");
        } else {
            abcInColumn.remove((Character) positioning.getLeft().charAt(0));
        }
    }

    private Boolean isCharacterInKeyboard(Keyboard keyboard, BTEPositioning positioning, int rotorId){
        if(!keyboard.isValidChar(positioning.getLeft().toUpperCase().charAt(0))){
            throw new IllegalArgumentException("Rotor ID " + rotorId + " character in the left column '" + positioning.getLeft().charAt(0) + "' is not in the keyboard allowed characters, which is currently: " + keyboard.toString() + ".");
        }
        if(!keyboard.isValidChar(positioning.getRight().toUpperCase().charAt(0))){
            throw new IllegalArgumentException("Rotor ID " + rotorId + " character in the right column '" + positioning.getRight().charAt(0) + "' is not in the keyboard allowed characters, which is currently: " + keyboard.toString() + ".");
        }
        return true;
    }

    private void validateAndSetupPositioning(BTEPositioning btePosition, Keyboard keyboard, List<Integer> leftColumn, List<Integer> rightColumn, List<Character> abcInLeftColumn, List<Character> abcInRightColumn, int rotorId){
            // check if character is in keyboard
            if(isCharacterInKeyboard(keyboard, btePosition, rotorId)){
                // add to columns if valid
                leftColumn.add(keyboard.charToIndex(btePosition.getLeft().toUpperCase().charAt(0)));
                rightColumn.add(keyboard.charToIndex(btePosition.getRight().toUpperCase().charAt(0)));
            }

            // check for duplication
            checkIfDuplicateCharacterInColumn(abcInLeftColumn, btePosition, rotorId);
            checkIfDuplicateCharacterInColumn(abcInRightColumn, btePosition, rotorId);
    }

    private Map<Integer, Rotor> createRotorsMap(BTERotors bteRotors, Keyboard keyboard, int numOfUsedRotorsInMachine) {
        List<BTERotor> listOfBTERotors = bteRotors.getBTERotor();
        if(listOfBTERotors.size() < numOfUsedRotorsInMachine){
            throw new IllegalArgumentException("The machine must have at least " + numOfUsedRotorsInMachine + " rotors (as written in rotors count attribute), but got only " + listOfBTERotors.size() + " rotors in your XML.");
        }
        Map<Integer, Rotor> rotorMap = new HashMap<>();

        Set<Integer> idSet = new HashSet<>();
        for (BTERotor bteRotor : listOfBTERotors) {
            int id = getId(idSet, bteRotor.getId());
            int notch  = getNotch(id, idSet, bteRotor.getNotch(), keyboard);

            List<Integer> rightColumn = new ArrayList<>();
            List<Integer> leftColumn = new ArrayList<>();

            List<Character> abcInLeftColumn = new ArrayList<>(keyboard.toString().chars().mapToObj(c -> (char) c).toList());
            List<Character> abcInRightColumn = new ArrayList<>(keyboard.toString().chars().mapToObj(c -> (char) c).toList());

            List<BTEPositioning> btePositioning = bteRotor.getBTEPositioning();

            // Validate the rotor mapping before processing
            validateRotorMapping(id, btePositioning, keyboard);

            for (BTEPositioning btePosition : btePositioning) {
                if (btePosition.getLeft().length() == 1 && btePosition.getRight().length() == 1){
                    validateAndSetupPositioning(btePosition, keyboard, leftColumn, rightColumn, abcInLeftColumn, abcInRightColumn, id);
                } else {
                    throw new IllegalArgumentException("position is more than 1 character long. your XML is not valid.");
                }
            }

            int alphabet_length = keyboard.getAlphabetLength();
            Rotor rotor = new RotorImpl(rightColumn, leftColumn, notch, alphabet_length);
            rotor.setKeyboard(keyboard);
            rotor.setId(id);
            rotorMap.put(id, rotor);
        }

        // make sure we have no holes in id sequence
        for(int i = 1; i <= rotorMap.size(); i++){
            if(!rotorMap.containsKey(i)){
                throw new IllegalArgumentException("Rotor IDs must be consecutive integers starting from 1 to " + rotorMap.size() + ", but missing ID: " + i);
            }
        }

        return rotorMap;
    }

    private void validateRotorMapping(int rotorId, List<BTEPositioning> btePositioning, Keyboard keyboard) {
        String abc = keyboard.toString();
        int alphabetSize = keyboard.getAlphabetLength();

        // Track characters found in left and right columns
        Set<Character> leftCharsFound = new HashSet<>();
        Set<Character> rightCharsFound = new HashSet<>();

        // Track the mapping to ensure one-to-one correspondence
        Map<Character, Character> leftToRightMapping = new HashMap<>();
        Map<Character, Character> rightToLeftMapping = new HashMap<>();

        logger.debug("Validating rotor mapping for Rotor ID: {}", rotorId);

        for (BTEPositioning positioning : btePositioning) {
            String leftStr = positioning.getLeft().trim().toUpperCase();
            String rightStr = positioning.getRight().trim().toUpperCase();

            if (leftStr.length() != 1 || rightStr.length() != 1) {
                throw new IllegalArgumentException(
                        "Rotor ID " + rotorId + " has position elements with invalid length. " +
                                "Each position must have exactly 1 character, but got left='" + leftStr + "' and right='" + rightStr + "'.");
            }

            char leftChar = leftStr.charAt(0);
            char rightChar = rightStr.charAt(0);

            // Check if characters are in the defined alphabet
            if (!abc.contains(String.valueOf(leftChar))) {
                throw new IllegalArgumentException(
                        "Rotor ID " + rotorId + " contains character '" + leftChar + "' in left positioning " +
                                "which is not in the defined alphabet: " + abc);
            }
            if (!abc.contains(String.valueOf(rightChar))) {
                throw new IllegalArgumentException(
                        "Rotor ID " + rotorId + " contains character '" + rightChar + "' in right positioning " +
                                "which is not in the defined alphabet: " + abc);
            }

            // Check for duplicate characters in left column
            if (leftCharsFound.contains(leftChar)) {
                throw new IllegalArgumentException(
                        "Rotor ID " + rotorId + " has duplicate character '" + leftChar + "' in left positioning.");
            }
            leftCharsFound.add(leftChar);

            // Check for duplicate characters in right column
            if (rightCharsFound.contains(rightChar)) {
                throw new IllegalArgumentException(
                        "Rotor ID " + rotorId + " has duplicate character '" + rightChar + "' in right positioning.");
            }
            rightCharsFound.add(rightChar);

            // Check for one-to-one mapping violations
            if (leftToRightMapping.containsKey(leftChar)) {
                char previousMapping = leftToRightMapping.get(leftChar);
                if (previousMapping != rightChar) {
                    throw new IllegalArgumentException(
                            "Rotor ID " + rotorId + " has conflicting mapping for character '" + leftChar + "'. " +
                                    "Mapped to both '" + previousMapping + "' and '" + rightChar + "'.");
                }
            }
            leftToRightMapping.put(leftChar, rightChar);

            if (rightToLeftMapping.containsKey(rightChar)) {
                char previousMapping = rightToLeftMapping.get(rightChar);
                if (previousMapping != leftChar) {
                    throw new IllegalArgumentException(
                            "Rotor ID " + rotorId + " has conflicting reverse mapping for character '" + rightChar + "'. " +
                                    "Mapped from both '" + previousMapping + "' and '" + leftChar + "'.");
                }
            }
            rightToLeftMapping.put(rightChar, leftChar);
        }

        // Check for completeness: all characters must be present
        if (leftCharsFound.size() != alphabetSize) {
            Set<Character> missingLeft = new HashSet<>();
            for (char c : abc.toCharArray()) {
                if (!leftCharsFound.contains(c)) {
                    missingLeft.add(c);
                }
            }
            throw new IllegalArgumentException(
                    "Rotor ID " + rotorId + " is missing characters in left positioning. " +
                            "Missing characters: " + missingLeft + ". All alphabet characters must be mapped exactly once.");
        }

        if (rightCharsFound.size() != alphabetSize) {
            Set<Character> missingRight = new HashSet<>();
            for (char c : abc.toCharArray()) {
                if (!rightCharsFound.contains(c)) {
                    missingRight.add(c);
                }
            }
            throw new IllegalArgumentException(
                    "Rotor ID " + rotorId + " is missing characters in right positioning. " +
                            "Missing characters: " + missingRight + ". All alphabet characters must be mapped exactly once.");
        }

        logger.debug("Rotor ID {} mapping validation passed. All characters are properly mapped.", rotorId);
    }

    private Map<String, Reflector> createReflectorsMap(BTEReflectors bteReflectors, Keyboard keyboard) {

        Map<String, Reflector> reflectorMap = new HashMap<>();
        List<BTEReflector> listOfBTEReflectors = bteReflectors.getBTEReflector();

        RomanValues.clear();
        for (BTEReflector bteReflector : listOfBTEReflectors) {
            List<ReflectedPositionsPair> listOfReflectedPositionsPairs = new ArrayList<>();
            String id = bteReflector.getId().trim();
            if(!RomanValues.romanValues.containsKey(id)){
                throw new IllegalArgumentException("Reflector ID must be a roman value, but got: " + id);
            } else if (RomanValues.checkIfUsed(id)){
                throw new IllegalArgumentException("Reflector ID must be unique, but got a duplicate ID: " + id);
            }
            RomanValues.markAsUsed(id);
             // to ensure no duplicate IDs

            List<BTEReflect> bteReflects = bteReflector.getBTEReflect();

            for (BTEReflect bteReflect : bteReflects) {
                int input = bteReflect.getInput();
                int output = bteReflect.getOutput();
                if(input == output){
                    throw new IllegalArgumentException("Reflector cannot map a position to itself, but got map between " + input + " and " + output + " in Reflector " + id + ".");
                }
                listOfReflectedPositionsPairs.add(new ReflectedPositionsPair(--input, --output)); // convert to zero-based index
            }
            Reflector reflector = new ReflectorImpl(id, listOfReflectedPositionsPairs);
            reflector.setKeyboard(keyboard);
            reflectorMap.put(id, reflector);
        }

        return reflectorMap;
    }

    private Keyboard createKeyboard(String abcForKeyboard) {
        Map<Character, Integer> mapFromCharToInt = createMapFromCharToInt(abcForKeyboard);
        return new KeyboardImpl(abcForKeyboard, mapFromCharToInt);
    }

    private Map<Character, Integer> createMapFromCharToInt(String abcForKeyboard) {
        abcForKeyboard = abcForKeyboard.trim();
        return java.util.stream.IntStream.range(0, abcForKeyboard.length())
            .boxed()
            .collect(Collectors.toMap(
                    abcForKeyboard::charAt,            // key: character at index i
                i -> i,                                   // value: index i
                (existing, replacement) -> existing,      // keep first index on duplicates
                LinkedHashMap::new              // preserve insertion order
            ));
    }
}