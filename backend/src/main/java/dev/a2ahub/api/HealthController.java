package dev.a2ahub.api;

import dev.a2ahub.agent.Agent;
import dev.a2ahub.agent.AgentRegistryService;
import dev.a2ahub.health.AgentHealthMonitorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents/{id}/health")
public class HealthController {

    private final AgentRegistryService agentRegistryService;
    private final AgentHealthMonitorService healthMonitorService;

    public HealthController(AgentRegistryService agentRegistryService, AgentHealthMonitorService healthMonitorService) {
        this.agentRegistryService = agentRegistryService;
        this.healthMonitorService = healthMonitorService;
    }

    @GetMapping
    public List<AgentHealthMonitorService.HealthCheckHistoryDto> getHealthHistory(@PathVariable UUID id) {
        // Validate that agent exists
        agentRegistryService.findById(id);
        return healthMonitorService.getRecentHealthChecks(id);
    }

    @PostMapping("/probe")
    public AgentHealthMonitorService.HealthCheckResult triggerProbe(@PathVariable UUID id) {
        Agent agent = agentRegistryService.findById(id);
        return healthMonitorService.probeAgent(agent);
    }
}
