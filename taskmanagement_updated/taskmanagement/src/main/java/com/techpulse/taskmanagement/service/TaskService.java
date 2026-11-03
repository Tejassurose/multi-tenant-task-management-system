
package com.techpulse.taskmanagement.service;

import java.util.List;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.techpulse.taskmanagement.dto.request.AssignTaskRequest;
import com.techpulse.taskmanagement.enums.Role;
import com.techpulse.taskmanagement.dto.request.UpdateTaskStatusRequest;
import com.techpulse.taskmanagement.dto.request.CreateTaskRequest;
import com.techpulse.taskmanagement.dto.response.TaskResponse;
import com.techpulse.taskmanagement.entity.Task;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.enums.TaskStatus;
import com.techpulse.taskmanagement.exception.ResourceNotFoundException;
import com.techpulse.taskmanagement.repository.TaskRepository;
import com.techpulse.taskmanagement.repository.TenantRepository;
import com.techpulse.taskmanagement.repository.UserRepository;
import com.techpulse.taskmanagement.tenant.TenantContext;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    	

@Transactional
public TaskResponse createTask(CreateTaskRequest request) {

    // Step 1: Get tenant ID from JWT-based TenantContext
    Long tenantId = TenantContext.getTenantId();

    if (tenantId == null) {
        throw new IllegalStateException(
                "Tenant context is not available");
    }

    // Step 2: Find the tenant
    Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Tenant not found"));

    // Step 3: Create task
    Task task = new Task(
            request.getTitle(),
            request.getDescription(),
            tenant
    );

    // Step 4: Save task
    task = taskRepository.save(task);

    // Step 5: Return response
    return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            null,
            tenantId
    );
}


@Transactional
public TaskResponse assignTask(Long taskId, AssignTaskRequest request) {

    // Step 1: Get tenant ID from authenticated JWT context
    Long tenantId = TenantContext.getTenantId();

    if (tenantId == null) {
        throw new IllegalStateException(
                "Tenant context is not available");
    }

    // Step 2: Find task only within the manager's tenant
    Task task = taskRepository.findByIdAndTenant_Id(taskId, tenantId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("Task not found"));

    // Step 3: Find user only within the same tenant
    User user = userRepository
            .findByIdAndTenant_Id(request.getUserId(), tenantId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("User not found in this tenant"));

    // Step 4: Assign user to task
    task.setAssignedTo(user);

    // Step 5: Save updated task
    task = taskRepository.save(task);

    // Step 6: Return response
    return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            user.getId(),
            tenantId
    );
}


@Transactional
public TaskResponse updateTaskStatus(
        Long taskId,
        UpdateTaskStatusRequest request,
        User currentUser) {

    // Step 1: Get tenant ID from authenticated context
    Long tenantId = TenantContext.getTenantId();

    if (tenantId == null) {
        throw new IllegalStateException(
                "Tenant context is not available");
    }

    // Step 2: Find task belonging to this tenant
    // AND assigned to the currently logged-in user
    Task task = taskRepository
            .findByIdAndTenant_IdAndAssignedTo_Id(
                    taskId,
                    tenantId,
                    currentUser.getId())
            .orElseThrow(() ->
                    new ResourceNotFoundException("Task not found or not assigned to you"));

    // Step 3: Update task status
    task.setStatus(request.getStatus());

    // Step 4: Save updated task
    task = taskRepository.save(task);

    // Step 5: Return response
    return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            currentUser.getId(),
            tenantId
    );
}


@Transactional(readOnly = true)
public List<TaskResponse> getTasks(User currentUser) {

    // Step 1: Get tenant ID from authenticated context
    Long tenantId = TenantContext.getTenantId();

    if (tenantId == null) {
        throw new IllegalStateException(
                "Tenant context is not available");
    }

    // Step 2: Fetch tasks according to user role
    List<Task> tasks;

    if (currentUser.getRole() == Role.ADMIN ||
            currentUser.getRole() == Role.MANAGER) {

        // ADMIN and MANAGER see all tasks in their tenant
        tasks = taskRepository.findByTenant_Id(tenantId);

    } else if (currentUser.getRole() == Role.USER) {

        // USER sees only tasks assigned to them
        tasks = taskRepository.findByTenant_IdAndAssignedTo_Id(
                tenantId,
                currentUser.getId());

    } else {
        throw new IllegalArgumentException("Invalid user role");
    }

    // Step 3: Convert Task entities into TaskResponse DTOs
    return tasks.stream()
            .map(task -> new TaskResponse(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getAssignedTo() == null
                            ? null
                            : task.getAssignedTo().getId(),
                    tenantId
            ))
            .toList();
}


    
    public TaskService(TaskRepository taskRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }
}
