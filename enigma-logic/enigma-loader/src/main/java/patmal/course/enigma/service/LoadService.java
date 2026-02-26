package patmal.course.enigma.service;

import jakarta.transaction.Transactional;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import patmal.course.enigma.dal.api.MachineRepository;
import patmal.course.enigma.engine.logic.repository.Repository;
import patmal.course.enigma.loadManager.LoadManager;

import java.io.FileNotFoundException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class LoadService {
    private static final String DUPLICATE_MACHINE_ERROR_TEMPLATE =
            "Machine '%s' already exists. Please use a different machine name or delete the existing one before importing.";

    private final LoadManager loadManager;
    private final MachineRepository machineRepository;

    @Transactional // Ensures the DB save is atomic for parent and all children
    public String handleXmlImport(InputStream inputStream) throws JAXBException, FileNotFoundException {

        // Parse XML to domain Repository
        Repository repository = loadManager.loadMachineSettingsFromXML(inputStream);

        // get machine name
        String machineName = repository.getMachineName();

        // Fail early with a readable domain message instead of leaking raw DB/SQL details
        if (machineRepository.existsByName(machineName)) {
            throw new IllegalArgumentException(String.format(DUPLICATE_MACHINE_ERROR_TEMPLATE, machineName));
        }

        // Save the entire tree to the DB using CascadeType.ALL
        try {
            machineRepository.save(repository);
        } catch (DataIntegrityViolationException e) {
            // Keep this fallback for concurrent requests racing on the same machine name
            throw new IllegalArgumentException(String.format(DUPLICATE_MACHINE_ERROR_TEMPLATE, machineName), e);
        }

        return machineName;
    }
}
