package dev.a2ahub.health;

import dev.a2ahub.agent.Agent;
import dev.a2ahub.agent.AgentCard;
import dev.a2ahub.agent.AgentRepository;
import dev.a2ahub.security.SsrfValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentHealthMonitor Unit Tests")
class AgentHealthMonitorTest {

    private static final int RETENTION_HOURS = 48;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private HealthCheckRepository healthCheckRepository;

    @Mock
    private AgentHealthPersistenceService persistenceService;

    @Mock
    private SsrfValidator ssrfValidator;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient probeClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private AgentHealthMonitor healthMonitor;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.requestFactory(any())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(probeClient);
        HealthProperties healthProperties = new HealthProperties();
        healthProperties.setRetentionHours(RETENTION_HOURS);

        healthMonitor = new AgentHealthMonitor(
                agentRepository,
                healthCheckRepository,
                persistenceService,
                ssrfValidator,
                restClientBuilder,
                healthProperties,
                new SimpleAsyncTaskExecutor()
        );
    }

    @Test
    @DisplayName("Should mark agent HEALTHY on valid Agent Card response and delegate persistence")
    @SuppressWarnings("unchecked")
    void shouldMarkAgentHealthyOnValidResponse() {
        UUID agentId = UUID.randomUUID();
        Agent agent = new Agent();
        agent.setId(agentId);
        agent.setName("Translator");
        agent.setUrl("https://translator.agent.io");
        agent.setStatus("UNKNOWN");

        AgentCard card = new AgentCard("Translator", "Translation agent", "https://translator.agent.io", "1.0", List.of(), Map.of(), List.of());

        when(probeClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(AgentCard.class)).thenReturn(card);

        AgentHealthMonitor.HealthCheckResult result = healthMonitor.checkAgent(agent);

        assertThat(result.status()).isEqualTo("HEALTHY");
        assertThat(result.error()).isNull();
        verify(persistenceService).recordProbeResult(eq(agentId), eq("HEALTHY"), anyInt(), isNull());
    }

    @Test
    @DisplayName("Should mark agent OFFLINE when connection fails and record error")
    @SuppressWarnings("unchecked")
    void shouldMarkAgentOfflineOnConnectionError() {
        UUID agentId = UUID.randomUUID();
        Agent agent = new Agent();
        agent.setId(agentId);
        agent.setName("BrokenAgent");
        agent.setUrl("https://broken.agent.io");
        agent.setStatus("HEALTHY");

        when(probeClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        AgentHealthMonitor.HealthCheckResult result = healthMonitor.checkAgent(agent);

        assertThat(result.status()).isEqualTo("OFFLINE");
        assertThat(result.error()).contains("Connection refused");
        verify(persistenceService).recordProbeResult(eq(agentId), eq("OFFLINE"), anyInt(), contains("Connection refused"));
    }

    @Test
    @DisplayName("Should execute chunked batch pruning of health check logs")
    void shouldPurgeOldHealthLogsInBatches() {
        when(healthCheckRepository.deleteBatchOlderThan(any(ZonedDateTime.class), anyInt()))
                .thenReturn(5000)
                .thenReturn(120);

        ZonedDateTime before = ZonedDateTime.now();

        healthMonitor.purgeOldHealthLogs();

        ArgumentCaptor<ZonedDateTime> cutoff = ArgumentCaptor.forClass(ZonedDateTime.class);
        verify(healthCheckRepository, times(2)).deleteBatchOlderThan(cutoff.capture(), eq(5000));
        assertThat(cutoff.getValue())
                .isBetween(before.minusHours(RETENTION_HOURS), ZonedDateTime.now().minusHours(RETENTION_HOURS));
    }
}
