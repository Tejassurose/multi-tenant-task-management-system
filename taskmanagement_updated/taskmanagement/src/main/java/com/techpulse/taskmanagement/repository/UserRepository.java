package com.techpulse.taskmanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techpulse.taskmanagement.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by email during login
    Optional<User> findByEmail(String email);

    // Check whether an email is already registered
    boolean existsByEmail(String email);

    // Authenticated requests must resolve the subject inside the JWT tenant.
    Optional<User> findByEmailAndTenant_Id(String email, Long tenantId);

    // Find a user only if they belong to the given tenant.
    Optional<User> findByIdAndTenant_Id(Long id, Long tenantId);

}