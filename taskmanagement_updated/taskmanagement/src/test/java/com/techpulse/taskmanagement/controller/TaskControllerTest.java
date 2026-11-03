package com.techpulse.taskmanagement.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.techpulse.taskmanagement.dto.request.*;
import com.techpulse.taskmanagement.dto.response.TaskResponse;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.enums.*;
import com.techpulse.taskmanagement.service.TaskService;

class TaskControllerTest {
    @Test void createTaskReturnsCreated() {
        TaskService service=mock(TaskService.class); TaskController controller=new TaskController(service);
        CreateTaskRequest req=new CreateTaskRequest(); TaskResponse body=new TaskResponse(1L,"t","d",TaskStatus.TODO,null,4L);
        when(service.createTask(req)).thenReturn(body); var response=controller.createTask(req);
        assertEquals(201,response.getStatusCode().value()); assertSame(body,response.getBody());
    }
    @Test void assignTaskReturnsOk() {
        TaskService service=mock(TaskService.class); TaskController controller=new TaskController(service);
        AssignTaskRequest req=new AssignTaskRequest(); TaskResponse body=new TaskResponse(1L,"t","d",TaskStatus.TODO,8L,4L);
        when(service.assignTask(1L,req)).thenReturn(body); var response=controller.assignTask(1L,req);
        assertEquals(200,response.getStatusCode().value()); assertSame(body,response.getBody());
    }
    @Test void updateStatusReturnsOk() {
        TaskService service=mock(TaskService.class); TaskController controller=new TaskController(service);
        UpdateTaskStatusRequest req=new UpdateTaskStatusRequest(); User user=new User();
        TaskResponse body=new TaskResponse(1L,"t","d",TaskStatus.COMPLETED,8L,4L);
        when(service.updateTaskStatus(1L,req,user)).thenReturn(body); var response=controller.updateTaskStatus(1L,req,user);
        assertEquals(200,response.getStatusCode().value()); assertSame(body,response.getBody());
    }
    @Test void getTasksReturnsServiceList() {
        TaskService service=mock(TaskService.class); TaskController controller=new TaskController(service);
        User user=new User(); List<TaskResponse> body=List.of(new TaskResponse(1L,"t","d",TaskStatus.TODO,null,4L));
        when(service.getTasks(user)).thenReturn(body); var response=controller.getTasks(user);
        assertEquals(200,response.getStatusCode().value()); assertSame(body,response.getBody());
    }
}
