
package com.techpulse.taskmanagement.dto.response;

import com.techpulse.taskmanagement.enums.TaskStatus;

public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Long assignedToId;
    private Long tenantId;

    public TaskResponse() {
    }

    public TaskResponse(Long id, String title, String description,
            TaskStatus status, Long assignedToId, Long tenantId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.assignedToId = assignedToId;
        this.tenantId = tenantId;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public Long getAssignedToId() {
        return assignedToId;
    }

    public Long getTenantId() {
        return tenantId;
    }
}
