package dev.a2ahub.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class TaskRequest {

    @NotBlank(message = "query must not be blank")
    @Size(max = 500, message = "query must not exceed 500 characters")
    private String query;

    @Size(max = 100, message = "contextId must not exceed 100 characters")
    private String contextId;

    public TaskRequest() {
    }

    public TaskRequest(String query) {
        this.query = query;
    }

    public TaskRequest(String query, String contextId) {
        this.query = query;
        this.contextId = contextId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    @JsonIgnore
    public String getSanitizedQuery() {
        return query != null ? query.trim() : "";
    }

    @JsonIgnore
    public String getEffectiveContextId() {
        return (contextId != null && !contextId.isBlank()) ? contextId.trim() : UUID.randomUUID().toString();
    }
}
