package patmal.course.enigma.dal.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "machines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachinePersistenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private int rotorsCount;
    private String abc;

    // Relationship to all available rotors defined in the XML
    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RotorPersistenceEntity> rotors;

    // Relationship to all available reflectors defined in the XML
    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReflectorPersistenceEntity> reflectors;

    // Relationship to Processed massages
    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProcessingPersistenceEntity> processingHistory;
}