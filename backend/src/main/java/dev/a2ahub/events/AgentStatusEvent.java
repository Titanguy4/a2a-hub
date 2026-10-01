package dev.a2ahub.events;

import java.time.ZonedDateTime;
import java.util.UUID;

public record AgentStatusEvent(
    UUID agentId,
    String agentName,
    String status,
    String previousStatus,
    Integer latencyMs,
    ZonedDateTime timestamp,
    String message
) {}
