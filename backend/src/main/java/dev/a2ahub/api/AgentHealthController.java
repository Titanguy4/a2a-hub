package dev.a2ahub.api;

import dev.a2ahub.agent.Agent;
import dev.a2ahub.agent.AgentRepository;
import dev.a2ahub.health.AgentHealthMonitor;
import dev.a2ahub.health.HealthCheckLog;
import dev.a2ahub.health.HealthCheckRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class AgentHealthController {

    private final AgentRepository agentRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final AgentHealthMonitor healthMonitor;

    public AgentHealthController(AgentRepository agentRepository,
                                 HealthCheckRepository healthCheckRepository,
                                 AgentHealthMonitor healthMonitor) {
        this.agentRepository = agentRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.healthMonitor = healthMonitor;
    }

    @GetMapping("/agents/{id}/health")
    public AgentHealthResponse getAgentHealthHistory(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "30") int limit
    ) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent not found: " + id));

        int fetchLimit = Math.min(Math.max(limit, 1), 100);
        List<HealthCheckLog> logs = healthCheckRepository.findRecentByAgentId(id, PageRequest.of(0, fetchLimit));

        List<HealthCheckItem> history = logs.stream()
                .map(l -> new HealthCheckItem(l.getId(), l.getCheckedAt(), l.getStatus(), l.getLatencyMs(), l.getErrorMsg()))
                .toList();

        return new AgentHealthResponse(
                agent.getId(),
                agent.getName(),
                agent.getStatus(),
                agent.getLastSeenAt(),
                history
        );
    }

    @PostMapping("/agents/{id}/health/check")
    public AgentHealthMonitor.HealthCheckResult triggerHealthCheck(@PathVariable UUID id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent not found: " + id));

        return healthMonitor.checkAgent(agent);
    }

    @GetMapping("/health/stats")
    public Map<String, Object> getHubHealthStats() {
        List<Agent> agents = agentRepository.findAll();
        long healthyCount = agents.stream().filter(a -> "HEALTHY".equalsIgnoreCase(a.getStatus())).count();
        long degradedCount = agents.stream().filter(a -> "DEGRADED".equalsIgnoreCase(a.getStatus())).count();
        long offlineCount = agents.stream().filter(a -> "OFFLINE".equalsIgnoreCase(a.getStatus())).count();
        long unknownCount = agents.stream().filter(a -> "UNKNOWN".equalsIgnoreCase(a.getStatus()) || a.getStatus() == null).count();

        Double avgLatency = healthCheckRepository.calculateAverageHealthyLatency(ZonedDateTime.now().minusHours(1));

        return Map.of(
                "totalAgents", agents.size(),
                "healthyAgents", healthyCount,
                "degradedAgents", degradedCount,
                "offlineAgents", offlineCount,
                "unknownAgents", unknownCount,
                "averageLatencyMs", avgLatency != null ? Math.round(avgLatency * 10.0) / 10.0 : 0
        );
    }

    public record AgentHealthResponse(
            UUID agentId,
            String agentName,
            String currentStatus,
            ZonedDateTime lastSeenAt,
            List<HealthCheckItem> history
    ) {}

    public record HealthCheckItem(
            Long id,
            ZonedDateTime checkedAt,
            String status,
            Integer latencyMs,
            String errorMessage
    ) {}
}
