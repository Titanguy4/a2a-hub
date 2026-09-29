package dev.a2ahub.agent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterAgentRequest(
    @NotBlank
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    String url
) {}
