package dev.a2ahub.agent;

import dev.a2ahub.security.SecurityProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(SecurityProperties.class)
@DisplayName("Database & Flyway Hermetic Integration Tests (Testcontainers + PostgreSQL)")
class AgentDatabaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("a2ahub_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgentSkillRepository agentSkillRepository;

    @Test
    @DisplayName("Should persist and retrieve Agent with JSONB AgentCard and skills")
    void shouldPersistAndRetrieveAgentWithJsonb() {
        AgentCard.Skill weatherSkill = new AgentCard.Skill("get_weather", "Get Weather", "Fetches live weather", List.of("weather", "meteo"));
        AgentCard card = new AgentCard("IntegrationAgent", "Test integration agent", "https://integration.agent.io", "1.0.0", List.of(weatherSkill), Map.of("streaming", true), List.of("REST"));

        Agent agent = new Agent();
        agent.setName("IntegrationAgent");
        agent.setDescription("Test integration agent");
        agent.setUrl("https://integration.agent.io");
        agent.setVersion("1.0.0");
        agent.setStatus("HEALTHY");
        agent.setAuthType("NONE");
        agent.setAgentCard(card);

        Agent savedAgent = agentRepository.save(agent);
        assertThat(savedAgent.getId()).isNotNull();

        AgentSkillEntity skillEntity = new AgentSkillEntity();
        skillEntity.setAgentId(savedAgent.getId());
        skillEntity.setSkillId("get_weather");
        skillEntity.setName("Get Weather");
        skillEntity.setDescription("Fetches live weather");
        skillEntity.setTags(List.of("weather", "meteo"));

        agentSkillRepository.save(skillEntity);

        // Verify retrieval
        Optional<Agent> retrieved = agentRepository.findById(savedAgent.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("IntegrationAgent");
        assertThat(retrieved.get().getAgentCard().skills()).hasSize(1);
        assertThat(retrieved.get().getAgentCard().skills().getFirst().id()).isEqualTo("get_weather");

        List<AgentSkillEntity> skills = agentSkillRepository.findByAgentId(savedAgent.getId());
        assertThat(skills).hasSize(1);
        assertThat(skills.getFirst().getSkillId()).isEqualTo("get_weather");

        // Cleanup
        agentSkillRepository.deleteByAgentId(savedAgent.getId());
        agentRepository.deleteById(savedAgent.getId());

        assertThat(agentRepository.findById(savedAgent.getId())).isEmpty();
        assertThat(agentSkillRepository.findByAgentId(savedAgent.getId())).isEmpty();
    }
}
