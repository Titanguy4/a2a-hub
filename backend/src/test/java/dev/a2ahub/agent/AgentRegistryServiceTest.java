package dev.a2ahub.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentRegistryService Unit Tests")
class AgentRegistryServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private AgentRegistryService agentRegistryService;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(restClient);
        agentRegistryService = new AgentRegistryService(agentRepository, restClientBuilder);
    }

    @Test
    @DisplayName("Should reject registration if agent URL is already registered")
    void shouldRejectDuplicateAgentUrl() {
        String url = "https://agent.example.com";
        when(agentRepository.existsByUrl(url)).thenReturn(true);

        assertThatThrownBy(() -> agentRegistryService.register(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");

        verify(agentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully find agent by ID")
    void shouldFindAgentById() {
        UUID id = UUID.randomUUID();
        Agent agent = new Agent();
        agent.setId(id);
        agent.setName("WeatherAgent");

        when(agentRepository.findById(id)).thenReturn(Optional.of(agent));

        Agent result = agentRegistryService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("WeatherAgent");
        verify(agentRepository).findById(id);
    }

    @Test
    @DisplayName("Should throw when finding non-existent agent by ID")
    void shouldThrowWhenAgentNotFound() {
        UUID id = UUID.randomUUID();
        when(agentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agentRegistryService.findById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Agent not found with ID");
    }

    @Test
    @DisplayName("Should successfully list all registered agents")
    void shouldListAllAgents() {
        Agent a1 = new Agent();
        a1.setName("Agent 1");
        Agent a2 = new Agent();
        a2.setName("Agent 2");

        when(agentRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Agent> results = agentRegistryService.findAll();

        assertThat(results).hasSize(2);
        verify(agentRepository).findAll();
    }

    @Test
    @DisplayName("Should unregister agent by ID")
    void shouldUnregisterAgent() {
        UUID id = UUID.randomUUID();
        doNothing().when(agentRepository).deleteById(id);

        agentRegistryService.unregister(id);

        verify(agentRepository).deleteById(id);
    }
}
