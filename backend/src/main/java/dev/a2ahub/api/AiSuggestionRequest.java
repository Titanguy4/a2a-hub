package dev.a2ahub.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiSuggestionRequest {

    @NotBlank(message = "Task description must not be blank")
    @Size(min = 3, max = 1000, message = "Task description must be between 3 and 1000 characters")
    private String taskDescription;

    public AiSuggestionRequest() {}

    public AiSuggestionRequest(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }
}
