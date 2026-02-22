package patmal.course.enigma.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import patmal.course.enigma.service.LoadService;

import java.util.Map;

@RestController
@RequestMapping("/enigma")
public class LoaderController {
    private final LoadService loaderService;

    public LoaderController(LoadService loadService) {
        this.loaderService = loadService;
    }

    @PostMapping(value = "/load", consumes = "multipart/form-data")
    public ResponseEntity<?> loadMachine(@RequestParam("file") MultipartFile file) {
        try {
            // Get the InputStream from the uploaded file and pass it to the service
            String machineName = loaderService.handleXmlImport(file.getInputStream());

            // Return a success response with the assigned name
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "name", machineName
            ));
        } catch (Exception e) {
            // Return 400 Bad Request if validation or parsing fails
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
