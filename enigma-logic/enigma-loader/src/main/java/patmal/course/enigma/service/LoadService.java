package patmal.course.enigma.service;

import jakarta.transaction.Transactional;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import patmal.course.enigma.dal.api.MachineRepository;
import patmal.course.enigma.engine.logic.repository.Repository;
import patmal.course.enigma.loadManager.LoadManager;

import java.io.FileNotFoundException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class LoadService {
    private final LoadManager loadManager;
    private final MachineRepository machineRepository;

    @Transactional // Ensures the DB save is atomic for parent and all children
    public String handleXmlImport(InputStream inputStream) throws JAXBException, FileNotFoundException {

        // Parse XML to domain Repository
        Repository repository = loadManager.loadMachineSettingsFromXML(inputStream);

        // get machine name
        String machineName = repository.getMachineName();

        // Save the entire tree to the DB using CascadeType.ALL
        machineRepository.save(repository);

        return machineName;
    }
}