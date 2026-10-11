package dev.a2ahub.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Declarative Bootstrap Service for automatically discovering seed agent microservices.
 * Reads configured seed URLs on startup and periodically reconciles any deferred agents
 * without blocking system boot or failing when downstream containers are initializing.
 */
@Service
public class AgentBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(AgentBootstrapService.class);

    private final AgentRegistryService registryService;
    private final AgentRepository agentRepository;
    private final List<String> seedUrls;
    private final Set<String> pendingUrls = ConcurrentHashMap.newKeySet();

    public AgentBootstrapService(
            AgentRegistryService registryService,
            AgentRepository agentRepository,
            @Value("${a2ahub.bootstrap.agents:}") String bootstrapAgentsConfig) {
        this.registryService = registryService;
        this.agentRepository = agentRepository;
        this.seedUrls = Arrays.stream(bootstrapAgentsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        if (seedUrls.isEmpty()) {
            log.info("No bootstrap seed agents configured.");
            return;
        }
        log.info("Attempting bootstrap discovery for {} configured agent seed(s)...", seedUrls.size());
        for (String url : seedUrls) {
            attemptRegister(url);
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void reconcilePendingSeeds() {
        if (pendingUrls.isEmpty()) {
            return;
        }
        log.debug("Reconciling {} pending bootstrap seed agent(s)...", pendingUrls.size());
        for (String url : List.copyOf(pendingUrls)) {
            attemptRegister(url);
        }
    }

    private void attemptRegister(String url) {
        try {
            if (agentRepository.existsByUrl(url)) {
                pendingUrls.remove(url);
                return;
            }
            registryService.register(url);
            pendingUrls.remove(url);
            log.info("Successfully bootstrapped agent from seed URL: {}", url);
        } catch (Exception e) {
            log.warn("Bootstrap discovery for {} deferred (will retry): {}", url, e.getMessage());
            pendingUrls.add(url);
        }
    }

    public List<String> getSeedUrls() {
        return seedUrls;
    }

    public Set<String> getPendingUrls() {
        return Set.copyOf(pendingUrls);
    }
}
