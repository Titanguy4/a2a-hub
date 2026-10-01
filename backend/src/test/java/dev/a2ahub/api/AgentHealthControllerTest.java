package dev.a2ahub.api;

import dev.a2ahub.agent.Agent;
import dev.a2ahub.agent.AgentRepository;
import dev.a2ahub.health.AgentHealthMonitor;
import dev.a2ahub.health.HealthCheckLog;
import dev.a2ahub.health.HealthCheckRepository;
import dev.a2ahub.security.ApiKeyAuthFilter;
import dev.a2ahub.security.SecurityProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgentHealthController.class)
@Import({SecurityProperties.class, ApiKeyAuthFilter.class})
@DisplayName("AgentHealthController Web Slice Tests")
class AgentHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentRepository agentRepository;

    @MockitoBean
    private HealthCheckRepository healthCheckRepository;

    @MockitoBean
    private AgentHealthMonitor healthMonitor;

    @Test
    @DisplayName("GET /api/v1/agents/{id}/health - Should return agent health history")
    void shouldReturnAgentHealthHistory() throws Exception {
        UUID agentId = UUID.randomUUID();
        Agent agent = new Agent();
        agent.setId(agentId);
        agent.setName("MetricsAgent");
        agent.setStatus("HEALTHY");
        agent.setLastSeenAt(ZonedDateTime.now());

        HealthCheckLog logItem = new HealthCheckLog(agent, "HEALTHY", 120, null);
        logItem.setId(1L);

        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));
        when(healthCheckRepository.findRecentByAgentId(eq(agentId), any(Pageable.class))).thenReturn(List.of(logItem));

        mockMvc.perform(get("/api/v1/agents/{id}/health", agentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentId").value(agentId.toString()))
                .andExpect(jsonPath("$.agentName").value("MetricsAgent"))
                .andExpect(jsonPath("$.currentStatus").value("HEALTHY"))
                .andExpect(jsonPath("$.history[0].status").value("HEALTHY"))
                .andExpect(jsonPath("$.history[0].latencyMs").value(120));

        verify(agentRepository).findById(agentId);
        verify(healthCheckRepository).findRecentByAgentId(eq(agentId), any(Pageable.class));
    }

    @Test
    @DisplayName("POST /api/v1/agents/{id}/health/check - Should trigger manual probe")
    void shouldTriggerManualProbe() throws Exception {
        UUID agentId = UUID.randomUUID();
        Agent agent = new Agent();
        agent.setId(agentId);
        agent.setName("TestProbeAgent");

        when(agentRepository.findById(agentId)).thenReturn(Optional.of(agent));
        when(healthMonitor.checkAgent(agent)).thenReturn(
                new AgentHealthMonitor.HealthCheckResult(agentId, "HEALTHY", 85, null)
        );

        mockMvc.perform(post("/api/v1/agents/{id}/health/check", agentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HEALTHY"))
                .andExpect(jsonPath("$.latencyMs").value(85));

        verify(healthMonitor).checkAgent(agent);
    }

    @Test
    @DisplayName("GET /api/v1/health/stats - Should return aggregate hub metrics")
    void shouldReturnHubHealthStats() throws Exception {
        Agent a1 = new Agent();
        a1.setStatus("HEALTHY");
        Agent a2 = new Agent();
        a2.setStatus("OFFLINE");

        when(agentRepository.findAll()).thenReturn(List.of(a1, a2));
        when(healthCheckRepository.calculateAverageHealthyLatency(any(ZonedDateTime.class))).thenReturn(95.4);

        mockMvc.perform(get("/api/v1/health/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAgents").value(2))
                .andExpect(jsonPath("$.healthyAgents").value(1))
                .andExpect(jsonPath("$.offlineAgents").value(1))
                .andExpect(jsonPath("$.averageLatencyMs").value(95.4));
    }
}
