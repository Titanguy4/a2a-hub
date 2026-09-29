package dev.a2ahub.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Service
public class AgentRegistryService {

    private static final Logger log = LoggerFactory.getLogger(AgentRegistryService.class);
    private final AgentRepository agentRepository;
    private final RestClient restClient;

    public AgentRegistryService(AgentRepository agentRepository, RestClient.Builder restClientBuilder) {
        this.agentRepository = agentRepository;
        this.restClient = restClientBuilder.build();
    }

    @Transactional
    public Agent register(String agentUrl) {
        if (agentRepository.existsByUrl(agentUrl)) {
            throw new IllegalArgumentException("Agent with URL " + agentUrl + " is already registered.");
        }

        // TODO: Validate SSRF (prevent resolving to localhost/internal IPs in prod)
        
        String fetchUrl = agentUrl.endsWith("/") 
            ? agentUrl + ".well-known/agent-card.json" 
            : agentUrl + "/.well-known/agent-card.json";
            
        log.info("Fetching agent card from: {}", fetchUrl);
        
        AgentCard card;
        try {
            card = restClient.get()
                    .uri(URI.create(fetchUrl))
                    .retrieve()
                    .body(AgentCard.class);
        } catch (Exception e) {
            log.error("Failed to fetch agent card from {}", fetchUrl, e);
            throw new IllegalStateException("Failed to fetch agent card from " + fetchUrl + ": " + e.getMessage());
        }

        if (card == null || card.name() == null) {
            throw new IllegalStateException("Invalid Agent Card received from " + fetchUrl);
        }

        Agent agent = new Agent();
        agent.setName(card.name());
        agent.setDescription(card.description());
        agent.setUrl(agentUrl);
        agent.setVersion(card.version());
        agent.setAgentCard(card);
        agent.setStatus("HEALTHY");

        return agentRepository.save(agent);
    }

    public List<Agent> findAll() {
        return agentRepository.findAll();
    }

    public Agent findById(UUID id) {
        return agentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found with ID: " + id));
    }

    @Transactional
    public void unregister(UUID id) {
        agentRepository.deleteById(id);
    }
}
