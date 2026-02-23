package patmal.course.enigma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import patmal.course.enigma.core.dto.ProcessResponseDTO;
import patmal.course.enigma.core.service.ProcessService;

@RestController
@RequestMapping("/enigma/process")
@RequiredArgsConstructor
public class ProcessController {
    private final ProcessService processingService;

    @PostMapping
    public ResponseEntity<ProcessResponseDTO> process(
            @RequestParam("sessionID") String sessionID,
            @RequestParam("input") String input) { // Change from @RequestBody to @RequestParam
        return ResponseEntity.ok(processingService.processMessage(sessionID, input));
    }
}