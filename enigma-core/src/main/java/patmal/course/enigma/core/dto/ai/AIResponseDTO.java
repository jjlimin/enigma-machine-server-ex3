package patmal.course.enigma.core.dto.ai;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIResponseDTO {
    private String answer;
    private String sql;
}
