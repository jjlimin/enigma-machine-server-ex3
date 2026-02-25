package patmal.course.enigma.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Hides 'error' on success and 'name' on failure
@JsonPropertyOrder({ "success", "name", "error" }) // Guarantees the order you requested
public class LoadResponseDTO {
    private boolean success;
    private String name;
    private String error;
}