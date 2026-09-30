package dev.a2ahub.api;

import dev.a2ahub.agent.Agent;
import dev.a2ahub.agent.AgentDiscoveryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class DiscoveryController {

    private final AgentDiscoveryService discoveryService;

    public DiscoveryController(AgentDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping("/discover")
    public List<Agent> discoverAgents(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String capability,
            @RequestParam(required = false) String q
    ) {
        return discoveryService.discover(skill, tag, capability, q);
    }

    @GetMapping("/skills")
    public List<AgentDiscoveryService.SkillSummary> getSkills() {
        return discoveryService.getDistinctSkills();
    }

    @GetMapping("/tags")
    public List<AgentDiscoveryService.TagSummary> getTags() {
        return discoveryService.getDistinctTags();
    }
}
