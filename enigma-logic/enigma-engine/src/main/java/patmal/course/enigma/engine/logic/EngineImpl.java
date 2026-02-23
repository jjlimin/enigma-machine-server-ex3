package patmal.course.enigma.engine.logic;

import org.springframework.stereotype.Service;
import patmal.course.enigma.component.plugboard.PlugboardImpl;
import patmal.course.enigma.component.rotor.Rotor;
import patmal.course.enigma.component.rotor.RotorManager;
import patmal.course.enigma.engine.logic.dto.EnigmaResult;
import patmal.course.enigma.engine.logic.dto.MachineConfiguration;
import patmal.course.enigma.engine.logic.repository.Repository;
import patmal.course.enigma.machine.Machine;
import patmal.course.enigma.machine.MachineImpl;

import java.util.List;

@Service
public class EngineImpl implements Engine {

    public EnigmaResult process(String input, Repository repository, MachineConfiguration config) {
        // Build machine from provided config
        Machine machine = buildMachine(repository, config);

        StringBuilder output = new StringBuilder();
        long startTime = System.nanoTime();

        // Encrypt
        for (char c : input.toCharArray()) {
            output.append(machine.encryptChar(c));
        }

        long duration = System.nanoTime() - startTime;

        // Capture the new positions after rotors moved
        List<Character> newPositions = machine.getRotorManager().getCurrentPositions();

        return new EnigmaResult(input, output.toString(), newPositions, duration);
    }

    private Machine buildMachine(Repository repository, MachineConfiguration config) {
//        List<Rotor> rotors = config.getRotorIds().stream()
//                .map(id -> repository.getAllRotors().get(id))
//                .toList();
        List<Rotor> rotors = config.getRotorIds().stream()
                .map(id -> {
                    Rotor blueprint = repository.getAllRotors().get(id);
                    // Assuming your RotorImpl has a copy constructor or clone method
                    return blueprint.cloneRotor();
                })
                .toList();

        List<Integer> positionIndices = config.getStartingPositions().stream()
                .map(c -> repository.getKeyboard().charToIndex(c))
                .toList();

        RotorManager rotorManager = new RotorManager(rotors, positionIndices, config.getRotorIds());

        return new MachineImpl(
                repository.getAllReflectors().get(config.getReflectorId()),
                rotorManager,
                repository.getKeyboard(),
                new PlugboardImpl(config.getPlugs())
        );
    }
}