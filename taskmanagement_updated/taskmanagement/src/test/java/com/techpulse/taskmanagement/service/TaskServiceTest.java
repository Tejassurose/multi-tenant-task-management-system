package com.techpulse.taskmanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.techpulse.taskmanagement.dto.request.*;
import com.techpulse.taskmanagement.entity.*;
import com.techpulse.taskmanagement.enums.*;
import com.techpulse.taskmanagement.repository.*;
import com.techpulse.taskmanagement.tenant.TenantContext;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock TaskRepository tasks; @Mock TenantRepository tenants; @Mock UserRepository users;
    @InjectMocks TaskService service;
    @AfterEach void clearTenant(){TenantContext.clear();}
    private Tenant tenant(){Tenant t=new Tenant("Acme");t.setId(1L);return t;}

    @Test void createsTaskWithTenantFromContext() {
        Tenant t=tenant(); TenantContext.setTenantId(1L); when(tenants.findById(1L)).thenReturn(Optional.of(t));
        when(tasks.save(any(Task.class))).thenAnswer(i->{Task task=i.getArgument(0);task.setId(4L);return task;});
        CreateTaskRequest req=new CreateTaskRequest();req.setTitle("Build API");req.setDescription("desc");
        var result=service.createTask(req); assertEquals(1L,result.getTenantId()); assertEquals(TaskStatus.TODO,result.getStatus());
        verify(tasks).save(argThat(task->task.getTenant()==t));
    }

    @Test void rejectsTaskFromAnotherTenantWhenAssigning() {
        TenantContext.setTenantId(1L); when(tasks.findByIdAndTenant_Id(50L,1L)).thenReturn(Optional.empty());
        AssignTaskRequest req=new AssignTaskRequest();req.setUserId(2L);
        assertThrows(RuntimeException.class,()->service.assignTask(50L,req));
        verify(users,never()).findByIdAndTenant_Id(any(),any());
    }

    @Test void rejectsAssigneeFromAnotherTenant() {
        TenantContext.setTenantId(1L); Task task=new Task("x","y",tenant());
        when(tasks.findByIdAndTenant_Id(5L,1L)).thenReturn(Optional.of(task));
        when(users.findByIdAndTenant_Id(99L,1L)).thenReturn(Optional.empty());
        AssignTaskRequest req=new AssignTaskRequest();req.setUserId(99L);
        assertThrows(RuntimeException.class,()->service.assignTask(5L,req));
    }

    @Test void userCanOnlyUpdateOwnAssignedTask() {
        TenantContext.setTenantId(1L); User user=new User("u@test.com","hash",Role.USER,tenant());user.setId(8L);
        when(tasks.findByIdAndTenant_IdAndAssignedTo_Id(12L,1L,8L)).thenReturn(Optional.empty());
        UpdateTaskStatusRequest req=new UpdateTaskStatusRequest();req.setStatus(TaskStatus.COMPLETED);
        assertThrows(RuntimeException.class,()->service.updateTaskStatus(12L,req,user));
    }

    @Test void adminGetsTenantTasksOnly() {
        TenantContext.setTenantId(1L); User admin=new User("a@test.com","hash",Role.ADMIN,tenant());
        when(tasks.findByTenant_Id(1L)).thenReturn(List.of(new Task("tenant task","",tenant())));
        var result=service.getTasks(admin); assertEquals(1,result.size());
        verify(tasks).findByTenant_Id(1L); verify(tasks,never()).findByTenant_IdAndAssignedTo_Id(any(),any());
    }

    @Test void userGetsOnlyAssignedTasks() {
        TenantContext.setTenantId(1L); User user=new User("u@test.com","hash",Role.USER,tenant());user.setId(8L);
        when(tasks.findByTenant_IdAndAssignedTo_Id(1L,8L)).thenReturn(List.of());
        assertTrue(service.getTasks(user).isEmpty()); verify(tasks).findByTenant_IdAndAssignedTo_Id(1L,8L);
    }

    @Test void serviceRequiresTenantContext() {
        TenantContext.clear(); User user=new User("u@test.com","hash",Role.USER,tenant());
        assertThrows(IllegalStateException.class,()->service.getTasks(user));
    }
}
