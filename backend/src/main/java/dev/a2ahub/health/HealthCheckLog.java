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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "health_checks_seq_gen")
    @SequenceGenerator(
        name = "health_checks_seq_gen",
        sequenceName = "health_checks_seq",
        allocationSize = 50
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(name = "checked_at", nullable = false)
    private ZonedDateTime checkedAt;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "error_msg")
    private String errorMsg;

    @PrePersist
    public void prePersist() {
        if (this.checkedAt == null) {
            this.checkedAt = ZonedDateTime.now();
        }
    }

    public HealthCheckLog(Agent agent, String status, Integer latencyMs, String errorMsg) {
        this.agent = agent;
        this.status = status;
        this.latencyMs = latencyMs;
        this.errorMsg = errorMsg;
        this.checkedAt = ZonedDateTime.now();
    }
}
