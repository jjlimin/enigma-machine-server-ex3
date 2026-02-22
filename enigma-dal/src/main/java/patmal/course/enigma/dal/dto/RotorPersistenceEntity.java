package patmal.course.enigma.dal.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "machines_rotors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotorPersistenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id")
    private MachinePersistenceEntity machine;

    private int rotorId;
    private int notch;
    private String wiringRight;
    private String wiringLeft;
}