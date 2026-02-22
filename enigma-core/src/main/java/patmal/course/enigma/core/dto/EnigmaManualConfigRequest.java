package patmal.course.enigma.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnigmaManualConfigRequest {
    private String sessionID;
    private List<RotorSelection> rotors;
    private String reflector;
    private List<PlugConnection> plugs;
}