package dev.a2ahub.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AgentDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(AgentDiscoveryService.class);
    private final AgentRepository agentRepository;
    private final AgentSkillRepository agentSkillRepository;

    public AgentDiscoveryService(AgentRepository agentRepository, AgentSkillRepository agentSkillRepository) {
        this.agentRepository = agentRepository;
        this.agentSkillRepository = agentSkillRepository;
    }

    public List<Agent> discover(String skill, String tag, String capability, String query) {
        List<Agent> allAgents = agentRepository.findAll();

        if (allAgents.isEmpty()) {
            return List.of();
        }

        return allAgents.stream()
                .filter(agent -> matchesFilter(agent, skill, tag, capability, query))
                .sorted(Comparator.comparing(Agent::getName))
                .toList();
    }

    private boolean matchesFilter(Agent agent, String skill, String tag, String capability, String query) {
        AgentCard card = agent.getAgentCard();
        if (card == null) {
            return false;
        }

        // 1. Skill filter
        if (skill != null && !skill.isBlank()) {
            String lowerSkill = skill.toLowerCase();
            boolean hasSkill = card.skills() != null && card.skills().stream().anyMatch(s ->
                    (s.id() != null && s.id().toLowerCase().contains(lowerSkill)) ||
                    (s.name() != null && s.name().toLowerCase().contains(lowerSkill)) ||
                    (s.description() != null && s.description().toLowerCase().contains(lowerSkill))
            );
            if (!hasSkill) {
                return false;
            }
        }

        // 2. Tag filter
        if (tag != null && !tag.isBlank()) {
            String lowerTag = tag.toLowerCase();
            boolean hasTag = card.skills() != null && card.skills().stream().anyMatch(s ->
                    s.tags() != null && s.tags().stream().anyMatch(t -> t.equalsIgnoreCase(lowerTag))
            );
            if (!hasTag) {
                return false;
            }
        }

        // 3. Capability filter
        if (capability != null && !capability.isBlank()) {
            String lowerCap = capability.toLowerCase();
            boolean hasCap = card.capabilities() != null && card.capabilities().keySet().stream()
                    .anyMatch(k -> k.toLowerCase().contains(lowerCap));
            if (!hasCap) {
                return false;
            }
        }

        // 4. Free text / intent search
        if (query != null && !query.isBlank()) {
            String lowerQ = query.toLowerCase();
            boolean matchName = agent.getName() != null && agent.getName().toLowerCase().contains(lowerQ);
            boolean matchDesc = agent.getDescription() != null && agent.getDescription().toLowerCase().contains(lowerQ);
            boolean matchSkills = card.skills() != null && card.skills().stream().anyMatch(s ->
                    (s.name() != null && s.name().toLowerCase().contains(lowerQ)) ||
                    (s.description() != null && s.description().toLowerCase().contains(lowerQ)) ||
                    (s.tags() != null && s.tags().stream().anyMatch(t -> t.toLowerCase().contains(lowerQ)))
            );

            if (!matchName && !matchDesc && !matchSkills) {
                return false;
            }
        }

        return true;
    }

    public List<SkillSummary> getDistinctSkills() {
        List<Agent> agents = agentRepository.findAll();
        Map<String, SkillSummaryBuilder> skillMap = new LinkedHashMap<>();

        for (Agent agent : agents) {
            AgentCard card = agent.getAgentCard();
            if (card != null && card.skills() != null) {
                for (AgentCard.Skill s : card.skills()) {
                    String key = s.id() != null && !s.id().isBlank() ? s.id() : s.name();
                    if (key == null) continue;

                    skillMap.computeIfAbsent(key, k -> new SkillSummaryBuilder(
                            k,
                            s.name(),
                            s.description(),
                            new HashSet<>(s.tags() != null ? s.tags() : List.of())
                    )).addAgent(agent.getId());
                }
            }
        }

        return skillMap.values().stream()
                .map(SkillSummaryBuilder::build)
                .sorted(Comparator.comparing(SkillSummary::name))
                .toList();
    }

    public List<TagSummary> getDistinctTags() {
        List<Agent> agents = agentRepository.findAll();
        Map<String, Long> tagCounts = new HashMap<>();

        for (Agent agent : agents) {
            AgentCard card = agent.getAgentCard();
            if (card != null && card.skills() != null) {
                for (AgentCard.Skill s : card.skills()) {
                    if (s.tags() != null) {
                        for (String tag : s.tags()) {
                            if (tag != null && !tag.isBlank()) {
                                tagCounts.put(tag.toLowerCase(), tagCounts.getOrDefault(tag.toLowerCase(), 0L) + 1);
                            }
                        }
                    }
                }
            }
        }

        return tagCounts.entrySet().stream()
                .map(e -> new TagSummary(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.count(), a.count()))
                .toList();
    }

    public record SkillSummary(String skillId, String name, String description, List<String> tags, long agentCount, List<UUID> agentIds) {}
    public record TagSummary(String tag, long count) {}

    private static class SkillSummaryBuilder {
        private final String skillId;
        private final String name;
        private final String description;
        private final Set<String> tags;
        private final Set<UUID> agentIds = new HashSet<>();

        public SkillSummaryBuilder(String skillId, String name, String description, Set<String> tags) {
            this.skillId = skillId;
            this.name = name;
            this.description = description;
            this.tags = tags;
        }

        public void addAgent(UUID agentId) {
            this.agentIds.add(agentId);
        }

        public SkillSummary build() {
            return new SkillSummary(
                    skillId,
                    name != null ? name : skillId,
                    description,
                    new ArrayList<>(tags),
                    agentIds.size(),
                    new ArrayList<>(agentIds)
            );
        }
    }
}
