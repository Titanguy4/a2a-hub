package dev.a2ahub.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AgentCard Domain Unit Tests")
class AgentCardTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should apply resilient defaults in compact constructor when passed null collections")
    void shouldApplyResilientDefaultsWhenPassedNullCollections() {
        AgentCard card = new AgentCard("TestAgent", "Description", "https://test.io", "1.0.0", null, null, null);

        assertThat(card.name()).isEqualTo("TestAgent");
        assertThat(card.skills()).isNotNull().isEmpty();
        assertThat(card.capabilities()).isNotNull().isEmpty();
        assertThat(card.supportedInterfaces()).isNotNull().isEmpty();
        assertThat(card.interfaces()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Should normalize mixed supportedInterfaces (AgentInterface, Map, String) to AgentInterface records")
    void shouldNormalizeMixedSupportedInterfaces() {
        AgentCard.AgentInterface structured = new AgentCard.AgentInterface("https://api.structured.io", "JSON-RPC", "2.0");
        Map<String, Object> mapInterface = Map.of(
                "url", "https://api.map.io",
                "protocolBinding", "gRPC",
                "protocolVersion", "1.2"
        );
        Map<String, Object> emptyMapInterface = Map.of();
        String stringInterface = "REST";

        List<Object> rawInterfaces = List.of(structured, mapInterface, emptyMapInterface, stringInterface);

        AgentCard card = new AgentCard(
                "MultiInterfaceAgent",
                "Supports diverse interface schemas",
                "https://agent.io",
                "1.0.0",
                List.of(),
                Map.of(),
                rawInterfaces
        );

        List<AgentCard.AgentInterface> normalized = card.interfaces();
        assertThat(normalized).hasSize(4);

        // 1. Structured
        assertThat(normalized.get(0).url()).isEqualTo("https://api.structured.io");
        assertThat(normalized.get(0).protocolBinding()).isEqualTo("JSON-RPC");
        assertThat(normalized.get(0).protocolVersion()).isEqualTo("2.0");

        // 2. Map
        assertThat(normalized.get(1).url()).isEqualTo("https://api.map.io");
        assertThat(normalized.get(1).protocolBinding()).isEqualTo("gRPC");
        assertThat(normalized.get(1).protocolVersion()).isEqualTo("1.2");

        // 3. Map with defaults
        assertThat(normalized.get(2).url()).isNull();
        assertThat(normalized.get(2).protocolBinding()).isEqualTo("HTTP+JSON");
        assertThat(normalized.get(2).protocolVersion()).isEqualTo("1.0");

        // 4. String
        assertThat(normalized.get(3).url()).isNull();
        assertThat(normalized.get(3).protocolBinding()).isEqualTo("REST");
        assertThat(normalized.get(3).protocolVersion()).isEqualTo("1.0");
    }

    @Test
    @DisplayName("Should deserialize legacy JSON string arrays for supportedInterfaces via Jackson")
    void shouldDeserializeLegacyJsonStringArrays() throws Exception {
        String legacyJson = """
                {
                  "name": "LegacyAgent",
                  "description": "Legacy agent definition",
                  "url": "https://legacy.agent.io",
                  "version": "1.0.0",
                  "supportedInterfaces": ["REST", "JSON-RPC"]
                }
                """;

        AgentCard card = objectMapper.readValue(legacyJson, AgentCard.class);

        assertThat(card.name()).isEqualTo("LegacyAgent");
        assertThat(card.supportedInterfaces()).containsExactly("REST", "JSON-RPC");
        assertThat(card.skills()).isEmpty();
        assertThat(card.capabilities()).isEmpty();

        List<AgentCard.AgentInterface> normalized = card.interfaces();
        assertThat(normalized).hasSize(2);
        assertThat(normalized.get(0).protocolBinding()).isEqualTo("REST");
        assertThat(normalized.get(0).protocolVersion()).isEqualTo("1.0");
        assertThat(normalized.get(1).protocolBinding()).isEqualTo("JSON-RPC");
        assertThat(normalized.get(1).protocolVersion()).isEqualTo("1.0");
    }
}
