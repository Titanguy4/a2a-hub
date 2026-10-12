package dev.a2ahub.api;

import dev.a2ahub.orchestration.A2AOrchestrator;
import dev.a2ahub.orchestration.AiSuggestionDto;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orchestration")
public class OrchestrationController {

    private final A2AOrchestrator a2aOrchestrator;

    public OrchestrationController(A2AOrchestrator a2aOrchestrator) {
        this.a2aOrchestrator = a2aOrchestrator;
    }

    @PostMapping(
        value = "/suggest",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AiSuggestionDto> suggestAgent(@Valid @RequestBody AiSuggestionRequest request) {
        AiSuggestionDto suggestion = a2aOrchestrator.suggestAgent(request.getTaskDescription());
        return ResponseEntity.ok(suggestion);
    }
}
