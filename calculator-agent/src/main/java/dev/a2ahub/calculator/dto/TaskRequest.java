package dev.a2ahub.calculator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TaskRequest {

    @NotBlank(message = "query must not be blank")
    @Size(max = 500, message = "query must not exceed 500 characters")
    private String query;

    @Size(max = 100, message = "contextId must not exceed 100 characters")
    private String contextId;

    public TaskRequest() {
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

    public String getSanitizedQuery() {
        return query != null ? query.trim() : "";
    }

    public String getEffectiveContextId() {
        return (contextId != null && !contextId.isBlank()) ? contextId.trim() : "default";
    }

    public String getEffectiveContextId(String headerContextId) {
        if (headerContextId != null && !headerContextId.isBlank()) {
            return headerContextId.trim();
        }
        return getEffectiveContextId();
    }
}
