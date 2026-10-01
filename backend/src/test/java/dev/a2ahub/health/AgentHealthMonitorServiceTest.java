package dev.a2ahub.health;

import dev.a2ahub.agent.Agent;
import dev.a2ahub.agent.AgentCard;
import dev.a2ahub.agent.AgentRepository;
import dev.a2ahub.events.AgentEventPublisher;
import dev.a2ahub.security.SsrfValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentHealthMonitorService Unit Tests")
class AgentHealthMonitorServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private HealthCheckRepository healthCheckRepository;

    @Mock
    private SsrfValidator ssrfValidator;

    @Mock
    private AgentEventPublisher eventPublisher;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private AgentHealthMonitorService healthMonitorService;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(restClient);
        healthMonitorService = new AgentHealthMonitorService(
                agentRepository,
                healthCheckRepository,
                ssrfValidator,
                eventPublisher,
                restClientBuilder
        );
    }

    @Test
    @DisplayName("Should probe agent successfully and maintain HEALTHY status")
    void shouldProbeHealthyAgent() {
        Agent agent = new Agent();
        agent.setId(UUID.randomUUID());
        agent.setName("WeatherAgent");
        agent.setUrl("https://weather.agent.io");
        agent.setStatus("HEALTHY");

        AgentCard card = new AgentCard("WeatherAgent", "Weather", agent.getUrl(), "1.0.0", List.of(), Map.of(), List.of());

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(AgentCard.class)).thenReturn(card);

        AgentHealthMonitorService.HealthCheckResult result = healthMonitorService.probeAgent(agent);

        assertThat(result.status()).isEqualTo("HEALTHY");
        verify(healthCheckRepository).save(any(HealthCheckEntity.class));
        verify(agentRepository).save(agent);
    }

    @Test
    @DisplayName("Should mark agent OFFLINE and broadcast event when HTTP probe fails")
    void shouldMarkAgentOfflineOnFailure() {
        Agent agent = new Agent();
        agent.setId(UUID.randomUUID());
        agent.setName("WeatherAgent");
        agent.setUrl("https://weather.agent.io");
        agent.setStatus("HEALTHY");

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        AgentHealthMonitorService.HealthCheckResult result = healthMonitorService.probeAgent(agent);

        assertThat(result.status()).isEqualTo("OFFLINE");
        assertThat(agent.getStatus()).isEqualTo("OFFLINE");
        verify(healthCheckRepository).save(any(HealthCheckEntity.class));
        verify(eventPublisher).publishAgentStatusChanged(agent.getId(), "OFFLINE");
    }
}
