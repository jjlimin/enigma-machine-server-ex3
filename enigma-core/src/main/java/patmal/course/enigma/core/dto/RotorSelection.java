package patmal.course.enigma.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RotorSelection {
    private int rotorNumber; // The ID of the rotor in the machine catalog
    private char rotorPosition; // The starting character
}