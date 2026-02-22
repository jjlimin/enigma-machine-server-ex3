package patmal.course.enigma.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MachineConfigResponseDTO {
    private int totalRotors;
    private int totalReflectors;
    private int totalProcessedMessages;
    private EnigmaCodeStructure originalCode;
    private EnigmaCodeStructure currentRotorsPosition;
    private String originalCodeCompact;
    private String currentRotorsPositionCompact;
}



