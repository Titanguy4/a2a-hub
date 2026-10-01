package dev.a2ahub.health;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HealthCheckRepository extends JpaRepository<HealthCheckEntity, Long> {

    List<HealthCheckEntity> findTop10ByAgentIdOrderByCheckedAtDesc(UUID agentId);

    void deleteByAgentId(UUID agentId);
}
