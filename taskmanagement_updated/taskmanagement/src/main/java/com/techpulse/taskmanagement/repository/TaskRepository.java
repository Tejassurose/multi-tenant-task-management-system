
package com.techpulse.taskmanagement.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techpulse.taskmanagement.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find all tasks belonging to a tenant
    List<Task> findByTenant_Id(Long tenantId);

    // Find a particular task belonging to a tenant
    Optional<Task> findByIdAndTenant_Id(Long id, Long tenantId);

    // Find tasks assigned to a particular user within a tenant
    List<Task> findByTenant_IdAndAssignedTo_Id(
            Long tenantId, Long userId);

    // Find a task only if it belongs to the tenant
    // and is assigned to the specified user
    Optional<Task> findByIdAndTenant_IdAndAssignedTo_Id(
            Long taskId, Long tenantId, Long userId);
}
