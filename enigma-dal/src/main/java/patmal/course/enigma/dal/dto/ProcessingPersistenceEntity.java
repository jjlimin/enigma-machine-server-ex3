package patmal.course.enigma.dal.dto;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "processing")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessingPersistenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private MachinePersistenceEntity machine; // Foreign Key to machines table

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "code", nullable = false)
    private String code; // The configuration string at the moment of encryption

    @Column(name = "input", nullable = false)
    private String input;

    @Column(name = "output", nullable = false)
    private String output;

    @Column(name = "time", nullable = false)
    private Long time; // bigint in ns
}