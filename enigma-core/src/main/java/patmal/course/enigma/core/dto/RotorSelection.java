package patmal.course.enigma.core.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class RotorSelection {
    @Setter
    private int rotorNumber; // The ID of the rotor in the machine catalog
    private char rotorPosition; // The starting character

    public int getRotorNumber() {
        return rotorNumber;
    }

    public char getRotorPosition() {
        return rotorPosition;
    }

    public void setRotorPosition(char rotorPosition) {
        this.rotorPosition = Character.toUpperCase(rotorPosition);
    }
}