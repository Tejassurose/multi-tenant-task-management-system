
package com.techpulse.taskmanagement.dto.request;

import com.techpulse.taskmanagement.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateTaskStatusRequest {

    @NotNull(message = "Task status is required")
    private TaskStatus status;

    public UpdateTaskStatusRequest() {
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
