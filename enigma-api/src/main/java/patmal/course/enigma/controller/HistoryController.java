package patmal.course.enigma.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import patmal.course.enigma.core.service.HistoryService;

import java.util.Map;

@RestController
@RequestMapping("/enigma/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<?> getHistory(
            @RequestParam(value = "sessionID", required = false) String sessionID,
            @RequestParam(value = "machineName", required = false) String machineName) {
        try {
            return ResponseEntity.ok(historyService.getHistory(sessionID, machineName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
