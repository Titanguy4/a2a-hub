package dev.a2ahub.health;

import dev.a2ahub.agent.Agent;
import dev.a2ahub.agent.AgentCard;
import dev.a2ahub.agent.AgentRepository;
import dev.a2ahub.events.AgentEventPublisher;
import dev.a2ahub.security.SsrfValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgentHealthMonitorService {

    private static final Logger log = LoggerFactory.getLogger(AgentHealthMonitorService.class);

    private final AgentRepository agentRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final SsrfValidator ssrfValidator;
    private final AgentEventPublisher eventPublisher;
    private final RestClient restClient;

    public AgentHealthMonitorService(AgentRepository agentRepository,
                                     HealthCheckRepository healthCheckRepository,
                                     SsrfValidator ssrfValidator,
                                     AgentEventPublisher eventPublisher,
                                     RestClient.Builder restClientBuilder) {
        this.agentRepository = agentRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.ssrfValidator = ssrfValidator;
        this.eventPublisher = eventPublisher;
        this.restClient = restClientBuilder.build();
    }

    @Scheduled(fixedRateString = "${a2ahub.health-monitor.interval-ms:60000}", initialDelay = 10000)
    public void runScheduledHealthChecks() {
        List<Agent> agents = agentRepository.findAll();
        if (agents.isEmpty()) {
            return;
        }

        log.debug("Running health probe cycle for {} registered agents", agents.size());
        for (Agent agent : agents) {
            try {
                probeAgent(agent);
            } catch (Exception e) {
                log.warn("Health probe error for agent {}: {}", agent.getName(), e.getMessage());
            }
        }
    }

    @Transactional
    public HealthCheckResult probeAgent(Agent agent) {
        String previousStatus = agent.getStatus();
        String url = agent.getUrl();
        String probeUrl = url.endsWith("/")
                ? url + ".well-known/agent-card.json"
                : url + "/.well-known/agent-card.json";

        long start = System.currentTimeMillis();
        String newStatus;
        String errorMsg = null;
        int latency;

        try {
            ssrfValidator.validateSafeRemoteUrl(url);

            AgentCard card = restClient.get()
                    .uri(URI.create(probeUrl))
                    .retrieve()
                    .body(AgentCard.class);

            latency = (int) (System.currentTimeMillis() - start);

            if (card != null && card.name() != null) {
                newStatus = latency > 1500 ? "DEGRADED" : "HEALTHY";
                agent.setLastSeenAt(ZonedDateTime.now());
            } else {
                newStatus = "DEGRADED";
                errorMsg = "Agent responded with empty or malformed card";
            }
        } catch (Exception e) {
            latency = (int) (System.currentTimeMillis() - start);
            newStatus = "OFFLINE";
            errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        }

        agent.setStatus(newStatus);
        agentRepository.save(agent);

        HealthCheckEntity check = new HealthCheckEntity();
        check.setAgent(agent);
        check.setCheckedAt(ZonedDateTime.now());
        check.setStatus(newStatus);
        check.setLatencyMs(latency);
        check.setErrorMsg(errorMsg);
        healthCheckRepository.save(check);

        if (!newStatus.equalsIgnoreCase(previousStatus)) {
            eventPublisher.publishAgentStatusChanged(agent.getId(), newStatus);
        }

        return new HealthCheckResult(agent.getId(), newStatus, latency, errorMsg, check.getCheckedAt());
    }

    public List<HealthCheckHistoryDto> getRecentHealthChecks(UUID agentId) {
        return healthCheckRepository.findTop10ByAgentIdOrderByCheckedAtDesc(agentId).stream()
                .map(h -> new HealthCheckHistoryDto(
                        h.getId(),
                        h.getStatus(),
                        h.getLatencyMs(),
                        h.getErrorMsg(),
                        h.getCheckedAt()
                ))
                .toList();
    }

    public record HealthCheckResult(UUID agentId, String status, int latencyMs, String errorMsg, ZonedDateTime checkedAt) {}
    public record HealthCheckHistoryDto(Long id, String status, Integer latencyMs, String errorMsg, ZonedDateTime checkedAt) {}
}
