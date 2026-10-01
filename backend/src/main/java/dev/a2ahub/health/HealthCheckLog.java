package dev.a2ahub.health;

import dev.a2ahub.agent.Agent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "health_checks")
@Getter
@Setter
@NoArgsConstructor
public class HealthCheckLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(name = "checked_at", nullable = false)
    private ZonedDateTime checkedAt = ZonedDateTime.now();

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "error_msg")
    private String errorMsg;

    public HealthCheckLog(Agent agent, String status, Integer latencyMs, String errorMsg) {
        this.agent = agent;
        this.status = status;
        this.latencyMs = latencyMs;
        this.errorMsg = errorMsg;
        this.checkedAt = ZonedDateTime.now();
    }
}
