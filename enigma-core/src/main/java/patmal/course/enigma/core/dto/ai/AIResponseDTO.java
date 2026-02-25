package patmal.course.enigma.core.dto.ai;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponseDTO {
    private String answer;
    private String sql;
}
