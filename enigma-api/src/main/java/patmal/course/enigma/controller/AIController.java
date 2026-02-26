package patmal.course.enigma.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import patmal.course.enigma.core.dto.ai.AIRequestDTO;
import patmal.course.enigma.core.dto.ai.AIResponseDTO;
import patmal.course.enigma.core.service.AIService;

@RestController
@RequestMapping("/enigma/ai")
public class AIController {
    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping //
    public AIResponseDTO processAIQuery(@RequestBody AIRequestDTO request) {
        return aiService.handleAIQuery(request.getQuery());
    }
}
