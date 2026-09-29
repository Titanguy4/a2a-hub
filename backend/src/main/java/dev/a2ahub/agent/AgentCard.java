package dev.a2ahub.agent;

import java.util.List;
import java.util.Map;

public record AgentCard(
    String name,
    String description,
    String url,
    String version,
    List<Skill> skills,
    Map<String, Object> capabilities,
    List<String> supportedInterfaces
) {
    public record Skill(String id, String name, String description, List<String> tags) {}
}
