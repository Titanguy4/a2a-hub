package dev.a2ahub.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {

    List<TaskEntity> findByAgentIdOrderByCreatedAtDesc(UUID agentId);

    List<TaskEntity> findByState(String state);
}
