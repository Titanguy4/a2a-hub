package dev.a2ahub.agent;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SourceType;
import dev.a2ahub.common.UuidV7;

@Entity
@Table(name = "agents")
@Getter
@Setter
public class Agent {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion = 0L;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UuidV7.generate();
        }
    }

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, unique = true, length = 512)
    private String url;

    private String version;

    @Column(name = "provider_name")
    private String providerName;

    @Column(nullable = false)
    private String status = "UNKNOWN";

    @Column(name = "auth_type")
    private String authType = "NONE";

    @Convert(converter = dev.a2ahub.security.AesGcmAttributeConverter.class)
    @Column(name = "auth_token_enc")
    private String authTokenEnc;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "agent_card", nullable = false)
    private AgentCard agentCard;

    @Column(name = "registered_at", insertable = false, updatable = false)
    private ZonedDateTime registeredAt;

    @Column(name = "last_seen_at")
    private ZonedDateTime lastSeenAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
