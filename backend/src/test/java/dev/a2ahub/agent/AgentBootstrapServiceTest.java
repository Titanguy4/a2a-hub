package dev.a2ahub.agent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentBootstrapService Unit Tests")
class AgentBootstrapServiceTest {

    @Mock
    private AgentRegistryService registryService;

    @Mock
    private AgentRepository agentRepository;

    @Test
    @DisplayName("Should parse seed URLs and register unindexed agents on startup")
    void shouldRegisterSeedAgentsOnStartup() {
        String seeds = "http://weather-agent:8081, http://calculator-agent:8082";
        AgentBootstrapService bootstrapService = new AgentBootstrapService(registryService, agentRepository, seeds);

        when(agentRepository.existsByUrl("http://weather-agent:8081")).thenReturn(false);
        when(agentRepository.existsByUrl("http://calculator-agent:8082")).thenReturn(true);

        bootstrapService.onStartup();

        verify(registryService, times(1)).register("http://weather-agent:8081");
        verify(registryService, never()).register("http://calculator-agent:8082");
        assertThat(bootstrapService.getPendingUrls()).isEmpty();
    }

    @Test
    @DisplayName("Should defer and reconcile failed registrations during boot")
    void shouldDeferFailedRegistrationsAndReconcile() {
        String seeds = "http://weather-agent:8081";
        AgentBootstrapService bootstrapService = new AgentBootstrapService(registryService, agentRepository, seeds);

        when(agentRepository.existsByUrl("http://weather-agent:8081")).thenReturn(false);
        doThrow(new RuntimeException("Connection refused")).when(registryService).register("http://weather-agent:8081");

        bootstrapService.onStartup();

        assertThat(bootstrapService.getPendingUrls()).contains("http://weather-agent:8081");

        // Next cycle: service recovers
        doAnswer(inv -> null).when(registryService).register("http://weather-agent:8081");
        bootstrapService.reconcilePendingSeeds();

        assertThat(bootstrapService.getPendingUrls()).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty seed configuration gracefully")
    void shouldHandleEmptyConfiguration() {
        AgentBootstrapService bootstrapService = new AgentBootstrapService(registryService, agentRepository, "");

        bootstrapService.onStartup();
        bootstrapService.reconcilePendingSeeds();

        verifyNoInteractions(registryService);
        verifyNoInteractions(agentRepository);
        assertThat(bootstrapService.getSeedUrls()).isEmpty();
    }
}
