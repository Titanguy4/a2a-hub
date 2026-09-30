package dev.a2ahub.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "a2ahub.security")
@Getter
@Setter
public class SecurityProperties {

    /**
     * Secret API key required for mutating endpoints (POST /api/v1/agents, DELETE /api/v1/agents/*).
     * If null or empty, authentication is disabled (suitable for local air-gapped demo).
     */
    private String apiKey;

    /**
     * Whether read endpoints (GET /api/v1/*) also require authentication.
     */
    private boolean requireAuthForReads = false;

    /**
     * SSRF defense settings.
     */
    private Ssrf ssrf = new Ssrf();

    @Getter
    @Setter
    public static class Ssrf {
        /**
         * Allow loopback and private subnets (ONLY for local tests / development).
         */
        private boolean allowPrivateNetworks = false;
    }
}
