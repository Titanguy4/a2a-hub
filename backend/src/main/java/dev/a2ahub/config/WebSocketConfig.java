package dev.a2ahub.config;

import dev.a2ahub.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final SecurityProperties securityProperties;

    public WebSocketConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = securityProperties.getAllowedOrigins() != null && !securityProperties.getAllowedOrigins().isEmpty()
                ? securityProperties.getAllowedOrigins().toArray(new String[0])
                : new String[]{"*"};

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(origins);

        registry.addEndpoint("/ws/sockjs")
                .setAllowedOriginPatterns(origins)
                .withSockJS();
    }
}
