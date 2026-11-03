package com.techpulse.taskmanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techpulse.taskmanagement.dto.request.LoginRequest;
import com.techpulse.taskmanagement.dto.request.TenantRegistrationRequest;
import com.techpulse.taskmanagement.dto.response.LoginResponse;
import com.techpulse.taskmanagement.dto.response.RegistrationResponse;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.enums.Role;
import com.techpulse.taskmanagement.exception.ConflictException;
import com.techpulse.taskmanagement.repository.TenantRepository;
import com.techpulse.taskmanagement.repository.UserRepository;
import com.techpulse.taskmanagement.security.JwtService;

@Service
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${security.jwt.expiration}")
    private long jwtExpiration;

    public AuthService(TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Register a new tenant and its first ADMIN user
    @Transactional
    public RegistrationResponse registerTenant(
            TenantRegistrationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        // Step 1: Create and save the tenant
        Tenant tenant = new Tenant(request.getTenantName());
        tenant = tenantRepository.save(tenant);

        // Step 2: Encode the admin password
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        // Step 3: Create the first ADMIN user
        User admin = new User(
                request.getEmail(),
                encodedPassword,
                Role.ADMIN,
                tenant
        );

        admin = userRepository.save(admin);

        // Step 4: Return registration response
        return new RegistrationResponse(
                tenant.getId(),
                tenant.getName(),
                admin.getId(),
                admin.getEmail(),
                "Tenant registered successfully"
        );
    }

    // Authenticate a user and generate JWT
    public LoginResponse login(LoginRequest request) {

        // Step 1: Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid email or password"));

        // Step 2: Verify the raw password against the stored hash
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new IllegalArgumentException(
                    "Invalid email or password");
        }

        // Step 3: Generate JWT containing email, tenant_id and role
        String token = jwtService.generateToken(user);

        // Step 4: Return token and user information
        return new LoginResponse(
                token,
                user.getTenant().getId(),
                user.getEmail(),
                user.getRole().name(),
                jwtExpiration
        );
    }
}