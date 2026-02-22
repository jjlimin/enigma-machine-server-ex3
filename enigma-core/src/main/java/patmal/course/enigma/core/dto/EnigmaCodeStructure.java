package patmal.course.enigma.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnigmaCodeStructure {
    private List<RotorStatusDTO> rotors;
    private String reflector; // Optional for current position
    private List<PlugConnection> plugs; // Optional for current position
}