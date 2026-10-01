package dev.a2ahub.api;

import dev.a2ahub.agent.Agent;
import dev.a2ahub.agent.AgentRegistryService;
import dev.a2ahub.health.AgentHealthMonitorService;
import dev.a2ahub.security.ApiKeyAuthFilter;
import dev.a2ahub.security.SecurityProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
@Import({SecurityProperties.class, ApiKeyAuthFilter.class})
@DisplayName("HealthController Web Slice Tests")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentRegistryService agentRegistryService;

    @MockitoBean
    private AgentHealthMonitorService healthMonitorService;

    @Test
    @DisplayName("GET /api/v1/agents/{id}/health - Should return recent health check logs")
    void shouldReturnHealthHistory() throws Exception {
        UUID agentId = UUID.randomUUID();
        Agent agent = new Agent();
        agent.setId(agentId);
        agent.setName("WeatherAgent");

        AgentHealthMonitorService.HealthCheckHistoryDto dto = new AgentHealthMonitorService.HealthCheckHistoryDto(
                1L,
                "HEALTHY",
                120,
                null,
                ZonedDateTime.now()
        );

        when(agentRegistryService.findById(agentId)).thenReturn(agent);
        when(healthMonitorService.getRecentHealthChecks(agentId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/agents/{id}/health", agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("HEALTHY"))
                .andExpect(jsonPath("$[0].latencyMs").value(120));
    }

    @Test
    @DisplayName("POST /api/v1/agents/{id}/health/probe - Should trigger on-demand health probe")
    void shouldTriggerProbe() throws Exception {
        UUID agentId = UUID.randomUUID();
        Agent agent = new Agent();
        agent.setId(agentId);
        agent.setName("WeatherAgent");

        AgentHealthMonitorService.HealthCheckResult result = new AgentHealthMonitorService.HealthCheckResult(
                agentId,
                "HEALTHY",
                95,
                null,
                ZonedDateTime.now()
        );

        when(agentRegistryService.findById(agentId)).thenReturn(agent);
        when(healthMonitorService.probeAgent(agent)).thenReturn(result);

        mockMvc.perform(post("/api/v1/agents/{id}/health/probe", agentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("HEALTHY"))
                .andExpect(jsonPath("$.latencyMs").value(95));
    }
}
