package patmal.course.enigma.machine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.course.enigma.component.keyboard.Keyboard;
import patmal.course.enigma.component.plugboard.Plugboard;
import patmal.course.enigma.component.reflector.Reflector;
import patmal.course.enigma.component.rotor.RotorManager;

import java.io.Serializable;

public class MachineImpl implements Machine, Serializable {
    private final Reflector reflector;
    private final RotorManager rotorManager;
    private final Keyboard keyboard;
    private final Plugboard plugboard;
    public static final Logger logger = LogManager.getLogger(MachineImpl.class);

    public MachineImpl(Reflector reflector, RotorManager rotorManager, Keyboard keyboard, Plugboard plugboard) {
        this.reflector = reflector;
        this.rotorManager = rotorManager;
        this.keyboard = keyboard;
        this.plugboard = plugboard;
    }

    @Override
    public char encryptChar(char inputChar) {
        logger.debug("encrypting char {}", inputChar);
        // Pass through plug board
        inputChar = plugboard.substitute(inputChar);
        // Convert character to index
        int charIndex = keyboard.charToIndex(inputChar);

        // Move rotors before encoding
        rotorManager.moveRotorsBeforeEncodingLetter();

        // Pass through rotors and reflector
        int inputToReflector = rotorManager.encryptLetterThroughRotorsRTL(charIndex, keyboard.getMapFromIntToChar());

        logger.debug("Input to reflector: {} ({}).", inputToReflector, keyboard.indexToChar(inputToReflector));
        int outputFromReflector = reflector.reflect(inputToReflector);
        logger.debug("Output from reflector: {} ({})", outputFromReflector, keyboard.indexToChar(outputFromReflector));

        int finalOutputIndex = rotorManager.encryptLetterThroughRotorsLTR(outputFromReflector, keyboard.getMapFromIntToChar());

        // pass through plug board again
        char outputChar = keyboard.indexToChar(finalOutputIndex);
        outputChar = plugboard.substitute(outputChar);
        finalOutputIndex = keyboard.charToIndex(outputChar);

        // Convert index back to character
        return keyboard.indexToChar(finalOutputIndex);
    }

}
