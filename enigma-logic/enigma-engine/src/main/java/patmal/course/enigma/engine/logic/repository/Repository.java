package patmal.course.enigma.engine.logic.repository;

import lombok.Getter;
import patmal.course.enigma.component.keyboard.Keyboard;
import patmal.course.enigma.component.reflector.Reflector;
import patmal.course.enigma.component.rotor.Rotor;

import java.io.Serializable;
import java.util.*;

public class Repository implements Serializable {
    @Getter
    private final String machineName;
    @Getter
    private final Map<Integer, Rotor> allRotors;
    @Getter
    private final Map<String, Reflector> allReflectors;
    @Getter
    private final Keyboard keyboard;
    @Getter
    private final int numOfUsedRotorsInMachine;
    private final Random randomGenerator;

    public Repository(String machineName, Map<Integer, Rotor> allRotors, Map<String, Reflector> allReflectors, Keyboard keyboard, int numOfUsedRotorsInMachine) {
        this.machineName = machineName;
        this.allRotors = allRotors;
        this.allReflectors = allReflectors;
        this.keyboard = keyboard;
        this.numOfUsedRotorsInMachine = numOfUsedRotorsInMachine;
        this.randomGenerator = new Random();
    }

    public String getRandomReflectorId() {
        Object[] reflectorIds = allReflectors.keySet().toArray();
        return (String) reflectorIds[randomGenerator.nextInt(reflectorIds.length)];
    }

    public List<Integer> getRandomRotorIds() {
        List<Integer> available = new ArrayList<>(allRotors.keySet());
        if (available.isEmpty()) {
            return new ArrayList<>();
        }

        // we will use it later
        // int maxRotors = available.size();
        // int randomNumOfRotors = randomGenerator.nextInt(maxRotors) + 1; // 1..maxRotors

        Collections.shuffle(available, randomGenerator);
        return new ArrayList<>(available.subList(0, this.numOfUsedRotorsInMachine));
    }

    public List<Character> getRandomPositionsForRotors(int size) {
        List<Character> randomRotorStartingPositions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int randomIndex = randomGenerator.nextInt(keyboard.getAlphabetLength());
            char randomChar = keyboard.indexToChar(randomIndex);
            randomRotorStartingPositions.add(randomChar);
        }
        return randomRotorStartingPositions;
    }
}
