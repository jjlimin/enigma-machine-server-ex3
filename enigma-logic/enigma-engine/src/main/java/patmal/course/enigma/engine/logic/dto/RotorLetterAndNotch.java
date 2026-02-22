package patmal.course.enigma.engine.logic.dto;

import java.io.Serializable;
import java.util.Objects;

public class RotorLetterAndNotch implements Serializable {
    private final char letter;
    private final int notch;

    public RotorLetterAndNotch(char letter, int notch) {
        this.letter = letter;
        this.notch = notch;
    }

    public char getLetter() {
        return letter;
    }

    public int getNotchPos() {
        return notch;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RotorLetterAndNotch that = (RotorLetterAndNotch) o;
        return letter == that.letter && notch == that.notch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(letter, notch);
    }
}
