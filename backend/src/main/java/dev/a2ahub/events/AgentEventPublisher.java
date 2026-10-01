package dev.a2ahub.events;

import dev.a2ahub.agent.Agent;
import dev.a2ahub.task.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class AgentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AgentEventPublisher.class);
    private final SimpMessagingTemplate messagingTemplate;

    public AgentEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishAgentRegistered(Agent agent) {
        try {
            Map<String, Object> event = Map.of(
                    "type", "AGENT_REGISTERED",
                    "agentId", agent.getId(),
                    "name", agent.getName(),
                    "url", agent.getUrl(),
                    "status", agent.getStatus(),
                    "timestamp", ZonedDateTime.now().toString()
            );
            messagingTemplate.convertAndSend("/topic/agents", event);
            log.debug("Broadcasted AGENT_REGISTERED event for {}", agent.getName());
        } catch (Exception e) {
            log.warn("Failed to broadcast agent registered event: {}", e.getMessage());
        }
    }

    public void publishAgentStatusChanged(UUID agentId, String status) {
        try {
            Map<String, Object> event = Map.of(
                    "type", "AGENT_STATUS_CHANGED",
                    "agentId", agentId,
                    "status", status,
                    "timestamp", ZonedDateTime.now().toString()
            );
            messagingTemplate.convertAndSend("/topic/agents", event);
            log.debug("Broadcasted AGENT_STATUS_CHANGED for {}: {}", agentId, status);
        } catch (Exception e) {
            log.warn("Failed to broadcast agent status event: {}", e.getMessage());
        }
    }

    public void publishTaskUpdated(TaskService.TaskDto task) {
        try {
            Map<String, Object> event = Map.of(
                    "type", "TASK_UPDATED",
                    "taskId", task.id(),
                    "agentId", task.agentId(),
                    "state", task.state(),
                    "timestamp", ZonedDateTime.now().toString()
            );
            messagingTemplate.convertAndSend("/topic/tasks", event);
            log.debug("Broadcasted TASK_UPDATED event for {}", task.id());
        } catch (Exception e) {
            log.warn("Failed to broadcast task updated event: {}", e.getMessage());
        }
    }
}
