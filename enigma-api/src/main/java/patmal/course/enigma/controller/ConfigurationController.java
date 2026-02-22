package patmal.course.enigma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import patmal.course.enigma.core.service.ConfigService;
import patmal.course.enigma.core.dto.EnigmaManualConfigRequest;
import patmal.course.enigma.core.dto.MachineConfigResponseDTO;

import java.util.Map;

@RestController
@RequestMapping("/enigma/config")
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigService configService;

    @GetMapping
    public ResponseEntity<?> getCurrentStatus(
            @RequestParam("sessionID") String sessionID,
            @RequestParam(value = "verbose", defaultValue = "false") boolean verbose) {
        try {
            // Pass the verbose flag to the service
            return ResponseEntity.ok(configService.getCurrentStatus(sessionID, verbose));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/manual")
    public ResponseEntity<?> setManual(@RequestBody EnigmaManualConfigRequest request) {
        try {
            configService.setManualConfiguration(request);
            return ResponseEntity.ok("Manual code set successfully");
        } catch (IllegalArgumentException e) {
            // Returns the validation error message to the user
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/automatic")
    public ResponseEntity<String> setAutomaticCodeSetup(@RequestParam("sessionID") String sessionID) {
        configService.setAutomaticConfiguration(sessionID);
        return ResponseEntity.ok("Automatic code setup completed successfully");
    }

    @PutMapping("/reset")
    public ResponseEntity<String> resetToOriginalCode(@RequestParam("sessionID") String sessionID) {
        configService.resetMachine(sessionID);
        return ResponseEntity.ok("Reset to original code completed successfully");
    }
}