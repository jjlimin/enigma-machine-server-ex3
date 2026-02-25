package patmal.course.enigma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import patmal.course.enigma.core.dto.LoadResponseDTO;
import patmal.course.enigma.service.LoadService;

@RestController
@RequestMapping("/enigma")
public class LoaderController {
    private final LoadService loaderService;

    public LoaderController(LoadService loadService) {
        this.loaderService = loadService;
    }

    @PostMapping(value = "/load", consumes = "multipart/form-data")
    public ResponseEntity<LoadResponseDTO> loadMachine(@RequestParam("file") MultipartFile file) {
        try {
            String machineName = loaderService.handleXmlImport(file.getInputStream());

            return ResponseEntity.ok(LoadResponseDTO.builder()
                    .success(true)
                    .name(machineName)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoadResponseDTO.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build());
        }
    }
}
