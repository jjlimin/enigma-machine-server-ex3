package patmal.course.enigma.dal.db;

import jakarta.transaction.Transactional;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import patmal.course.enigma.component.reflector.Reflector;
import patmal.course.enigma.component.rotor.Rotor;
import patmal.course.enigma.dal.api.MachineRepository;
import patmal.course.enigma.dal.db.jpa.JpaProcessingRepository;
import patmal.course.enigma.dal.dto.MachinePersistenceEntity;
import patmal.course.enigma.dal.db.jpa.JpaMachineRepository;
import patmal.course.enigma.dal.dto.ProcessingPersistenceEntity;
import patmal.course.enigma.dal.dto.ReflectorPersistenceEntity;
import patmal.course.enigma.dal.dto.RotorPersistenceEntity;
import patmal.course.enigma.engine.logic.repository.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Component("DBMachineRepository")
public class DBMachineRepositoryImpl implements MachineRepository {

    private final JpaMachineRepository jpaMachineRepository;
    private final JpaProcessingRepository jpaProcessingRepository;
    private final ConversionService conversionService;

    public DBMachineRepositoryImpl(JpaMachineRepository jpaMachineRepository, JpaProcessingRepository jpaProcessingRepository, ConversionService conversionService) {
        this.jpaMachineRepository = jpaMachineRepository;
        this.jpaProcessingRepository = jpaProcessingRepository;
        this.conversionService = conversionService;
    }

    @Override
    public MachinePersistenceEntity save(Repository repository) {
        // 2. Create the parent Machine Entity
        MachinePersistenceEntity machineEntity = MachinePersistenceEntity.builder()
                .name(repository.getMachineName())
                .rotorsCount(repository.getNumOfUsedRotorsInMachine())
                .abc(repository.getKeyboard().toString())
                .build();

        // 3. Convert and link Rotors
        List<RotorPersistenceEntity> rotorEntities = repository.getAllRotors().values().stream()
                .map(rotor -> mapToRotorEntity(rotor, machineEntity))
                .collect(Collectors.toList());

        // 4. Convert and link Reflectors
        List<ReflectorPersistenceEntity> reflectorEntities = repository.getAllReflectors().values().stream()
                .map(reflector -> mapToReflectorEntity(reflector, machineEntity))
                .collect(Collectors.toList());

        // 5. Set the children lists in the parent
        machineEntity.setRotors(rotorEntities);
        machineEntity.setReflectors(reflectorEntities);

        return jpaMachineRepository.save(machineEntity);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaMachineRepository.findByName(name).isPresent();
    }

    @Override
    public Repository getMachineByName(String name) {
        // Here you would fetch the Entity and rebuild the domain Repository
        // For now, this requires a Mapper or logic to reconstruct the machine components
        MachinePersistenceEntity entity = jpaMachineRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Machine not found: " + name));

        return conversionService.convert(entity, Repository.class);
    }

    @Override
    public int getProcessedMessageCount(String machineName) {
        // This executes a "SELECT COUNT(*) FROM processing WHERE machine_name = ..."
        return (int) jpaProcessingRepository.countByMachineName(machineName);
    }

    @Override
    @Transactional
    public void saveProcessingEntry(String machineName, String sessionID, String input, String output, String code, Long duration) {
        // 1. Resolve the name to a full Entity (to get the UUID)
        MachinePersistenceEntity machineEntity = jpaMachineRepository.findByName(machineName)
                .orElseThrow(() -> new RuntimeException("Machine not found for name: " + machineName));

        // 2. Build the entity using the @Builder you defined
        ProcessingPersistenceEntity entity = ProcessingPersistenceEntity.builder()
                .machine(machineEntity) // This satisfies the 'machine_id' UUID FK requirement
                .sessionId(sessionID)
                .code(code) // The compact code string like <1,2><A(0),A(0)><II><A|F>
                .input(input)
                .output(output)
                .time(duration) // nanoseconds
                .build();

        // 3. Save to the database
        jpaProcessingRepository.save(entity);
    }

    private RotorPersistenceEntity mapToRotorEntity(Rotor rotor, MachinePersistenceEntity machine) {
        return RotorPersistenceEntity.builder()
                .machine(machine) // Maintain the back-link for the foreign key
                .rotorId(rotor.getId())
                .notch(rotor.getNotchPosition() + 1) // Adjusting for 1-based XML notation
                .wiringRight(formatWiring(rotor.getRightColumn())) // Convert List<Integer> to CSV String
                .wiringLeft(formatWiring(rotor.getLeftColumn()))
                .build();
    }

    private ReflectorPersistenceEntity mapToReflectorEntity(Reflector reflector, MachinePersistenceEntity machineEntity) {
        String abc = machineEntity.getAbc();
        StringBuilder outputBuilder = new StringBuilder();

        // For each index in the alphabet, find its reflected output index
        for (int i = 0; i < abc.length(); i++) {
            int reflectedIndex = reflector.reflect(i);
            outputBuilder.append(abc.charAt(reflectedIndex));
        }

        return ReflectorPersistenceEntity.builder()
                .machine(machineEntity)
                .reflectorId(reflector.getId())
                .inputText(abc) // The original alphabet
                .outputText(outputBuilder.toString()) // The mapped alphabet
                .build();
    }
    private String formatWiring(List<Integer> wiringList) {
        // English comments: Joins indices [0, 1, 2] into a string "0,1,2" for DB storage
        return wiringList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
