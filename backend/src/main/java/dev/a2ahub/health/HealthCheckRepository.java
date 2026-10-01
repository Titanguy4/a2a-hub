package dev.a2ahub.health;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface HealthCheckRepository extends JpaRepository<HealthCheckLog, Long> {

    @Query("SELECT h FROM HealthCheckLog h WHERE h.agent.id = :agentId ORDER BY h.checkedAt DESC")
    List<HealthCheckLog> findRecentByAgentId(@Param("agentId") UUID agentId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM HealthCheckLog h WHERE h.checkedAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") ZonedDateTime cutoff);

    @Query("SELECT AVG(h.latencyMs) FROM HealthCheckLog h WHERE h.status = 'HEALTHY' AND h.checkedAt >= :since")
    Double calculateAverageHealthyLatency(@Param("since") ZonedDateTime since);
}
