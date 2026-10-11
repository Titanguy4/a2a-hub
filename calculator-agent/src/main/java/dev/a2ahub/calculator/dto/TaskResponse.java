package dev.a2ahub.calculator.dto;

public record TaskResponse(
        String status,
        String result,
        String contextId
) {
    public static TaskResponse ok(String result, String contextId) {
        return new TaskResponse("ok", result, contextId);
    }

    public static TaskResponse error(String message, String contextId) {
        return new TaskResponse("error", message, contextId);
    }
}
