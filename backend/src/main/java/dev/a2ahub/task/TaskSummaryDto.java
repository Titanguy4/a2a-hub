package dev.a2ahub.task;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Lightweight projection DTO for task lists and audit views.
 * Bypasses PostgreSQL TOAST tables by strictly omitting bulky JSONB request and response columns.
 */
public record TaskSummaryDto(
    UUID id,
    UUID agentId,
    String agentName,
    String contextId,
    TaskState state,
    String errorDetail,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {}
