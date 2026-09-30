package dev.a2ahub.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgentDiscoveryService Unit Tests")
class AgentDiscoveryServiceTest {

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private AgentSkillRepository agentSkillRepository;

    private AgentDiscoveryService discoveryService;

    private Agent weatherAgent;
    private Agent calculatorAgent;

    @BeforeEach
    void setUp() {
        discoveryService = new AgentDiscoveryService(agentRepository, agentSkillRepository);

        weatherAgent = new Agent();
        weatherAgent.setId(UUID.randomUUID());
        weatherAgent.setName("WeatherAgent");
        weatherAgent.setDescription("Provides global weather and climate alerts");
        weatherAgent.setUrl("https://weather.agent.io");
        weatherAgent.setAgentCard(new AgentCard(
                "WeatherAgent",
                "Provides global weather and climate alerts",
                "https://weather.agent.io",
                "1.0.0",
                List.of(
                        new AgentCard.Skill("get_current_weather", "Current Weather", "Fetch real-time weather", List.of("weather", "forecast", "temp")),
                        new AgentCard.Skill("air_quality", "Air Quality Index", "Fetch AQI data", List.of("weather", "environment"))
                ),
                Map.of("streaming", true, "geo_location", true),
                List.of("JSON-RPC", "REST")
        ));

        calculatorAgent = new Agent();
        calculatorAgent.setId(UUID.randomUUID());
        calculatorAgent.setName("MathAgent");
        calculatorAgent.setDescription("Performs high precision calculations");
        calculatorAgent.setUrl("https://math.agent.io");
        calculatorAgent.setAgentCard(new AgentCard(
                "MathAgent",
                "Performs high precision calculations",
                "https://math.agent.io",
                "2.1.0",
                List.of(
                        new AgentCard.Skill("evaluate_expr", "Evaluate Expression", "Solves math equations", List.of("math", "algebra", "finance"))
                ),
                Map.of("big_decimal", true),
                List.of("REST")
        ));
    }

    @Test
    @DisplayName("Should return all agents when no filter is specified")
    void shouldReturnAllAgentsWhenNoFilter() {
        when(agentRepository.findAll()).thenReturn(List.of(weatherAgent, calculatorAgent));

        List<Agent> results = discoveryService.discover(null, null, null, null);

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("Should filter agents by skill ID or name")
    void shouldFilterBySkill() {
        when(agentRepository.findAll()).thenReturn(List.of(weatherAgent, calculatorAgent));

        List<Agent> results = discoveryService.discover("air_quality", null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("WeatherAgent");
    }

    @Test
    @DisplayName("Should filter agents by tag")
    void shouldFilterByTag() {
        when(agentRepository.findAll()).thenReturn(List.of(weatherAgent, calculatorAgent));

        List<Agent> results = discoveryService.discover(null, "finance", null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("MathAgent");
    }

    @Test
    @DisplayName("Should filter agents by capability key")
    void shouldFilterByCapability() {
        when(agentRepository.findAll()).thenReturn(List.of(weatherAgent, calculatorAgent));

        List<Agent> results = discoveryService.discover(null, null, "streaming", null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("WeatherAgent");
    }

    @Test
    @DisplayName("Should search agents by free-form text query matching description or skills")
    void shouldSearchByTextQuery() {
        when(agentRepository.findAll()).thenReturn(List.of(weatherAgent, calculatorAgent));

        List<Agent> results = discoveryService.discover(null, null, null, "equations");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("MathAgent");
    }

    @Test
    @DisplayName("Should aggregate distinct skills across all registered agents")
    void shouldGetDistinctSkills() {
        when(agentRepository.findAll()).thenReturn(List.of(weatherAgent, calculatorAgent));

        List<AgentDiscoveryService.SkillSummary> skills = discoveryService.getDistinctSkills();

        assertThat(skills).hasSize(3);
        assertThat(skills.stream().map(AgentDiscoveryService.SkillSummary::name))
                .containsExactlyInAnyOrder("Air Quality Index", "Current Weather", "Evaluate Expression");
    }

    @Test
    @DisplayName("Should aggregate distinct tags with frequency counts")
    void shouldGetDistinctTags() {
        when(agentRepository.findAll()).thenReturn(List.of(weatherAgent, calculatorAgent));

        List<AgentDiscoveryService.TagSummary> tags = discoveryService.getDistinctTags();

        assertThat(tags).isNotEmpty();
        // "weather" appears in 2 skills of weatherAgent
        assertThat(tags.stream().filter(t -> t.tag().equals("weather")).findFirst().get().count()).isEqualTo(2);
    }
}
