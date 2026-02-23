package patmal.course.enigma.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RotorStatusDTO {
    private int rotorNumber;
    private char rotorPosition;
    private int notchDistance; // The distance to the notch from current position
}