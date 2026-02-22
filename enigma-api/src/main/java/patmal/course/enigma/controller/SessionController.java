package patmal.course.enigma.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import patmal.course.enigma.session.dto.CreateSessionRequest;
import patmal.course.enigma.session.dto.CreateSessionResponse;
import patmal.course.enigma.session.service.SessionService;

import java.util.Map;

@RestController
@RequestMapping("/enigma")
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/session")
    public ResponseEntity<?> createSession(@RequestBody CreateSessionRequest request) {
        try {
            String sessionId = sessionService.createSession(request.getMachineName());
            return ResponseEntity.ok(new CreateSessionResponse(sessionId));
        } catch (IllegalArgumentException e) {
            // Return specific error message for missing machines
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/session")
    public ResponseEntity<?> deleteSession(@RequestParam("sessionID") String sessionId) {
        if (!sessionService.exists(sessionId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionId));
        }
        sessionService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
