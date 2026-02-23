package patmal.course.enigma.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryEntry {
    private String input;
    private String output;
    private long duration; // In nanoseconds, from your 'time' column
}