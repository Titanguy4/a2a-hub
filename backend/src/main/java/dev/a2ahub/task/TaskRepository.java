package dev.a2ahub.task;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {

    @EntityGraph(attributePaths = {"agent"})
    List<TaskEntity> findByAgentIdOrderByCreatedAtDesc(UUID agentId);

    @Override
    @EntityGraph(attributePaths = {"agent"})
    Optional<TaskEntity> findById(UUID id);

    List<TaskEntity> findByState(TaskState state);

    /**
     * High-speed Keyset/Cursor Pagination with DTO projection.
     * O(1) Index seek on monotonic UUIDv7. Completely eliminates TOAST table reads.
     */
    @Query("""
        SELECT new dev.a2ahub.task.TaskSummaryDto(
            t.id,
            a.id,
            a.name,
            t.contextId,
            t.state,
            t.errorDetail,
            t.createdAt,
            t.updatedAt
        )
        FROM TaskEntity t
        JOIN t.agent a
        WHERE a.id = :agentId
          AND (:cursorId IS NULL OR t.id < :cursorId)
        ORDER BY t.id DESC
    """)
    Slice<TaskSummaryDto> findSummariesByAgentId(
            @Param("agentId") UUID agentId,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );

    void deleteByAgentId(UUID agentId);
}
