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
public class ProcessResponseDTO {
    private String output;
    private String currentRotorsPositionCompact; // Format: "Char(Distance),Char(Distance)"
}