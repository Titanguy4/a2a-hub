package dev.a2ahub.task;

import dev.a2ahub.agent.Agent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Dedicated Transactional Worker for task persistence.
 * Separated from TaskService to ensure:
 * 1. Invocation through Spring AOP CGLIB dynamic proxy (eliminating self-invocation bypass).
 * 2. Zero database connections held during downstream network HTTP proxying.
 */
@Service
public class TaskPersistenceService {

    private final TaskRepository taskRepository;

    public TaskPersistenceService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Short-lived transaction to persist initial submitted task state.
     */
    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TaskService.TaskDto createSubmittedTask(Agent agent, String contextId, Map<String, ?> payload) {
        TaskEntity task = new TaskEntity();
        task.setAgent(agent);
        task.setContextId(contextId != null ? contextId : UUID.randomUUID().toString());
        task.setRequest((Map<String, Object>) (Map<?, ?>) payload);
        task.setState(TaskState.SUBMITTED);
        TaskEntity saved = taskRepository.save(task);
        return new TaskService.TaskDto(
                saved.getId(),
                agent.getId(),
                agent.getName(),
                saved.getContextId(),
                saved.getState().name(),
                saved.getRequest(),
                saved.getResponse(),
                saved.getErrorDetail(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    /**
     * Short-lived transaction to update final task state and response.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TaskService.TaskDto updateTaskResult(UUID taskId, TaskState state, Map<String, Object> response, String errorDetail) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("Task not found with ID: " + taskId));
        task.setState(state);
        task.setResponse(response);
        task.setErrorDetail(errorDetail);
        task.setUpdatedAt(ZonedDateTime.now());
        TaskEntity saved = taskRepository.save(task);
        return new TaskService.TaskDto(
                saved.getId(),
                saved.getAgent().getId(),
                saved.getAgent().getName(),
                saved.getContextId(),
                saved.getState().name(),
                saved.getRequest(),
                saved.getResponse(),
                saved.getErrorDetail(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
