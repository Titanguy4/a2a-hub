package dev.a2ahub.task;

import dev.a2ahub.agent.Agent;
import dev.a2ahub.agent.AgentNotFoundException;
import dev.a2ahub.agent.AgentRepository;
import dev.a2ahub.events.AgentEventPublisher;
import dev.a2ahub.security.SsrfValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private TaskPersistenceService persistenceService;

    @Mock
    private SsrfValidator ssrfValidator;

    @Mock
    private AgentEventPublisher eventPublisher;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(restClient);
        taskService = new TaskService(
                taskRepository,
                agentRepository,
                persistenceService,
                ssrfValidator,
                eventPublisher,
                restClientBuilder,
                new SimpleAsyncTaskExecutor()
        );
    }

    @Test
    @DisplayName("Should reject task submission when target agent is OFFLINE")
    void shouldRejectTaskWhenAgentOffline() {
        UUID agentId = UUID.randomUUID();
        Agent agent = new Agent();
        agent.setId(agentId);
        agent.setName("OfflineAgent");
        agent.setStatus("OFFLINE");

        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));

        TaskService.SubmitTaskRequest request = new TaskService.SubmitTaskRequest(agentId, "ctx-1", Map.of("query", "test"), false);

        assertThatThrownBy(() -> taskService.submitTask(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OFFLINE");
    }

    @Test
    @DisplayName("Should throw when submitting a task to a non-existent agent")
    void shouldThrowWhenSubmittingTaskToMissingAgent() {
        UUID agentId = UUID.randomUUID();
        when(agentRepository.findById(agentId)).thenReturn(Optional.empty());

        TaskService.SubmitTaskRequest request = new TaskService.SubmitTaskRequest(
                agentId,
                "ctx-missing-agent",
                Map.of("query", "test"),
                false
        );

        assertThatThrownBy(() -> taskService.submitTask(request))
                .isInstanceOf(AgentNotFoundException.class)
                .hasMessage("Agent not found with ID: " + agentId);

        verify(persistenceService, never()).createSubmittedTask(any(), any(), any());
    }

    @Test
    @DisplayName("Should successfully execute synchronous task proxying")
    @SuppressWarnings("unchecked")
    void shouldExecuteAndCompleteSynchronousTask() {
        UUID agentId = UUID.randomUUID();
        Agent agent = new Agent();
        agent.setId(agentId);
        agent.setName("WorkerAgent");
        agent.setUrl("https://worker.agent.io");
        agent.setStatus("HEALTHY");

        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));

        UUID initialTaskId = UUID.randomUUID();
        TaskService.TaskDto initialDto = new TaskService.TaskDto(
                initialTaskId,
                agentId,
                agent.getName(),
                "ctx-100",
                TaskState.SUBMITTED.name(),
                Map.of("input", "hello"),
                null,
                null,
                java.time.ZonedDateTime.now(),
                java.time.ZonedDateTime.now()
        );

        when(persistenceService.createSubmittedTask(eq(agent), eq("ctx-100"), any())).thenReturn(initialDto);

        TaskService.TaskDto completedDto = new TaskService.TaskDto(
                initialTaskId,
                agentId,
                agent.getName(),
                "ctx-100",
                TaskState.COMPLETED.name(),
                Map.of("input", "hello"),
                Map.of("output", "world"),
                null,
                java.time.ZonedDateTime.now(),
                java.time.ZonedDateTime.now()
        );

        when(persistenceService.updateTaskResult(eq(initialTaskId), eq(TaskState.COMPLETED), any(), isNull()))
                .thenReturn(completedDto);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(Map.of("output", "world"));

        TaskService.SubmitTaskRequest request = new TaskService.SubmitTaskRequest(agentId, "ctx-100", Map.of("input", "hello"), false);
        TaskService.TaskExecutionResponse result = taskService.submitTask(request, false);

        assertThat(result.isAsync()).isFalse();
        assertThat(result.task()).isNotNull();
        assertThat(result.task().state()).isEqualTo("COMPLETED");
        assertThat(result.task().response()).containsEntry("output", "world");
        verify(eventPublisher).publishTaskUpdated(any(TaskService.TaskDto.class));
    }

    @Test
    @DisplayName("Should immediately return SUBMITTED task when async mode requested")
    void shouldReturnSubmittedImmediatelyInAsyncMode() {
        UUID agentId = UUID.randomUUID();
        Agent agent = new Agent();
        agent.setId(agentId);
        agent.setName("AsyncAgent");
        agent.setUrl("https://async.agent.io");
        agent.setStatus("HEALTHY");

        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));

        UUID initialTaskId = UUID.randomUUID();
        TaskService.TaskDto initialDto = new TaskService.TaskDto(
                initialTaskId,
                agentId,
                agent.getName(),
                "ctx-async",
                TaskState.SUBMITTED.name(),
                Map.of("prompt", "deep thinking"),
                null,
                null,
                java.time.ZonedDateTime.now(),
                java.time.ZonedDateTime.now()
        );

        when(persistenceService.createSubmittedTask(eq(agent), any(), any())).thenReturn(initialDto);

        TaskService.SubmitTaskRequest request = new TaskService.SubmitTaskRequest(agentId, "ctx-async", Map.of("prompt", "deep thinking"), true);
        TaskService.TaskExecutionResponse result = taskService.submitTask(request, true);

        assertThat(result.isAsync()).isTrue();
        assertThat(result.task().state()).isEqualTo("SUBMITTED");
        verify(persistenceService).createSubmittedTask(eq(agent), eq("ctx-async"), any());
    }
}
