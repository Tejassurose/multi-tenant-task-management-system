package com.techpulse.taskmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techpulse.taskmanagement.entity.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

}