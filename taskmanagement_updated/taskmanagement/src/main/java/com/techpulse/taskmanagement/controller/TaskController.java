
package com.techpulse.taskmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.techpulse.taskmanagement.dto.request.AssignTaskRequest;
import com.techpulse.taskmanagement.dto.request.CreateTaskRequest;
import com.techpulse.taskmanagement.dto.request.UpdateTaskStatusRequest;
import com.techpulse.taskmanagement.dto.response.TaskResponse;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // 1. MANAGER creates a task
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request) {

        TaskResponse response = taskService.createTask(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // 2. MANAGER assigns a task to a user
    @PutMapping("/{id}/assign")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long id,
            @Valid @RequestBody AssignTaskRequest request) {

        TaskResponse response =
                taskService.assignTask(id, request);

        return ResponseEntity.ok(response);
    }

    // 3. USER updates the status of an assigned task
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            @AuthenticationPrincipal User currentUser) {

        TaskResponse response =
                taskService.updateTaskStatus(id, request, currentUser);

        return ResponseEntity.ok(response);
    }

    // 4. Retrieve tasks according to the logged-in user's role
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(
            @AuthenticationPrincipal User currentUser) {

        List<TaskResponse> tasks = taskService.getTasks(currentUser);

        return ResponseEntity.ok(tasks);
    }
}
