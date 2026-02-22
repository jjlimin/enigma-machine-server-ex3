package patmal.course.enigma.dal.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import patmal.course.enigma.component.keyboard.Keyboard;
import patmal.course.enigma.component.keyboard.KeyboardImpl;
import patmal.course.enigma.component.reflector.Reflector;
import patmal.course.enigma.component.reflector.ReflectorImpl;
import patmal.course.enigma.component.reflector.ReflectedPositionsPair;
import patmal.course.enigma.component.rotor.Rotor;
import patmal.course.enigma.component.rotor.RotorImpl;
import patmal.course.enigma.dal.dto.MachinePersistenceEntity;
import patmal.course.enigma.dal.dto.RotorPersistenceEntity;
import patmal.course.enigma.dal.dto.ReflectorPersistenceEntity;
import patmal.course.enigma.engine.logic.repository.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MachinePersistenceEntityToRepositoryConverter implements Converter<MachinePersistenceEntity, Repository> {

    @Override
    public Repository convert(MachinePersistenceEntity source) {
        // 1. Reconstruct Keyboard
        String abc = source.getAbc();
        Map<Character, Integer> charToInt = new HashMap<>();
        for (int i = 0; i < abc.length(); i++) {
            charToInt.put(abc.charAt(i), i);
        }
        Keyboard keyboard = new KeyboardImpl(abc, charToInt);

        // 2. Build the Rotors Map
        Map<Integer, Rotor> rotorsMap = source.getRotors().stream()
                .collect(Collectors.toMap(
                        RotorPersistenceEntity::getRotorId,
                        entity -> {
                            Rotor rotor = new RotorImpl(
                                    parseColumn(entity.getWiringRight()),
                                    parseColumn(entity.getWiringLeft()),
                                    entity.getNotch(),
                                    abc.length()
                            );
                            rotor.setId(entity.getRotorId());
                            rotor.setKeyboard(keyboard);
                            return rotor;
                        }
                ));

        // 3. Build the Reflectors Map using the helper method below
        Map<String, Reflector> reflectorsMap = source.getReflectors().stream()
                .collect(Collectors.toMap(
                        ReflectorPersistenceEntity::getReflectorId,
                        entity -> mapToDomainReflector(entity, keyboard)
                ));

        // 4. Final step: Call Repository Constructor
        return new Repository(
                source.getName(),
                rotorsMap,
                reflectorsMap,
                keyboard,
                source.getRotorsCount()
        );
    }

    // Helper to extract index pairs from the input/output alphabet strings
    private Reflector mapToDomainReflector(ReflectorPersistenceEntity entity, Keyboard keyboard) {
        List<ReflectedPositionsPair> pairs = new ArrayList<>();
        String input = entity.getInputText();
        String output = entity.getOutputText();
        Set<Integer> processedIndices = new HashSet<>();

        // Iterate through the strings and find unique pairs (e.g., if A maps to B, find indices of A and B)
        for (int i = 0; i < input.length(); i++) {
            if (!processedIndices.contains(i)) {
                char outputChar = output.charAt(i);

                int inputIndex = i;
                int outputIndex = input.indexOf(outputChar);

                // Add to the list required by ReflectorImpl constructor
                pairs.add(new ReflectedPositionsPair(inputIndex, outputIndex));

                // Mark both sides of the reflection to avoid duplicate pairs
                processedIndices.add(inputIndex);
                processedIndices.add(outputIndex);
            }
        }

        Reflector reflector = new ReflectorImpl(entity.getReflectorId(), pairs);
        reflector.setKeyboard(keyboard);
        return reflector;
    }

    private List<Integer> parseColumn(String wiring) {
        return Arrays.stream(wiring.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}