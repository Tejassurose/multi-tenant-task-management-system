
package com.techpulse.taskmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    public CreateTaskRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
