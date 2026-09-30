package dev.a2ahub.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecurityProperties securityProperties;

    public ApiKeyAuthFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String configuredKey = securityProperties.getApiKey();

        // If no API key is configured, auth is disabled (local/open mode)
        if (configuredKey == null || configuredKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Check if route requires auth
        boolean isProtected = isProtectedEndpoint(path, method);

        if (!isProtected) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validate provided credentials
        String providedKey = extractApiKey(request);

        if (providedKey == null || !providedKey.equals(configuredKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid or missing API key\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isProtectedEndpoint(String path, String method) {
        // Mutating agent operations always require auth if a key is configured
        if (path.startsWith("/api/v1/agents") && ("POST".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method))) {
            return true;
        }

        // Optional full protection of read endpoints
        if (securityProperties.isRequireAuthForReads() && path.startsWith("/api/v1")) {
            return true;
        }

        return false;
    }

    private String extractApiKey(HttpServletRequest request) {
        String key = request.getHeader(API_KEY_HEADER);
        if (key != null && !key.isBlank()) {
            return key.trim();
        }

        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length()).trim();
        }

        return null;
    }
}
