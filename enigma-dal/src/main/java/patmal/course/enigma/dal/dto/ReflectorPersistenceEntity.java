package patmal.course.enigma.dal.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "machines_reflectors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReflectorPersistenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id")
    private MachinePersistenceEntity machine;

    private String reflectorId; // e.g., "I", "II"

    @Column(name = "input") // Maps to 'input' in your ERD
    private String inputText;

    @Column(name = "output") // Maps to 'output' in your ERD
    private String outputText;
}