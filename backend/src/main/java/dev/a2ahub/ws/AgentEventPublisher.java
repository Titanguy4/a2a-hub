package dev.a2ahub.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class AgentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AgentEventPublisher.class);
    private static final String STATUS_TOPIC = "/topic/agents/status";

    private final SimpMessagingTemplate messagingTemplate;

    public AgentEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishStatusEvent(AgentStatusEvent event) {
        try {
            log.debug("Publishing status event for agent {} ({}) -> {}", event.agentName(), event.agentId(), event.status());
            messagingTemplate.convertAndSend(STATUS_TOPIC, event);
        } catch (Exception e) {
            log.warn("Failed to broadcast WebSocket status event for agent {}: {}", event.agentId(), e.getMessage());
        }
    }
}
