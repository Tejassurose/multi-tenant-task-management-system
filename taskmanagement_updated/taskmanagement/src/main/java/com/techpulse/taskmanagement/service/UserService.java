package com.techpulse.taskmanagement.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techpulse.taskmanagement.dto.request.CreateUserRequest;
import com.techpulse.taskmanagement.dto.response.UserResponse;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.enums.Role;
import com.techpulse.taskmanagement.exception.ConflictException;
import com.techpulse.taskmanagement.exception.ResourceNotFoundException;
import com.techpulse.taskmanagement.repository.TenantRepository;
import com.techpulse.taskmanagement.repository.UserRepository;
import com.techpulse.taskmanagement.tenant.TenantContext;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        // 1. Get tenant ID from authenticated JWT context
        Long tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            throw new IllegalStateException(
                    "Tenant context is not available");
        }

        // 2. Do not allow creating another ADMIN through this API
        if (request.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException(
                    "ADMIN users cannot be created through this API");
        }

        // 3. Check whether email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        // 4. Find the tenant associated with the JWT
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        // 5. Encode password
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        // 6. Create user under the authenticated tenant
        User user = new User(
                request.getEmail(),
                encodedPassword,
                request.getRole(),
                tenant
        );

        user = userRepository.save(user);

        // 7. Return safe response without password
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                tenantId,
                "User created successfully"
        );
    }
}
