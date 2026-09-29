package dev.a2ahub.api;

import dev.a2ahub.agent.Agent;
import dev.a2ahub.agent.AgentRegistryService;
import dev.a2ahub.agent.RegisterAgentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents")
@CrossOrigin(origins = "*") // Allows local Vue development
public class AgentController {

    private final AgentRegistryService agentRegistryService;

    public AgentController(AgentRegistryService agentRegistryService) {
        this.agentRegistryService = agentRegistryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Agent register(@RequestBody @Valid RegisterAgentRequest request) {
        return agentRegistryService.register(request.url());
    }

    @GetMapping
    public List<Agent> getAllAgents() {
        return agentRegistryService.findAll();
    }

    @GetMapping("/{id}")
    public Agent getAgent(@PathVariable UUID id) {
        return agentRegistryService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregister(@PathVariable UUID id) {
        agentRegistryService.unregister(id);
    }
}
