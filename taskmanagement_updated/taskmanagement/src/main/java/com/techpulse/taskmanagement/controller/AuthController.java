package com.techpulse.taskmanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techpulse.taskmanagement.dto.request.LoginRequest;
import com.techpulse.taskmanagement.dto.request.TenantRegistrationRequest;
import com.techpulse.taskmanagement.dto.response.LoginResponse;
import com.techpulse.taskmanagement.dto.response.RegistrationResponse;
import com.techpulse.taskmanagement.service.AuthService;

import jakarta.validation.Valid;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // API 1: Register a new tenant
    @PostMapping("/tenant/register")
    public ResponseEntity<RegistrationResponse> registerTenant(
            @Valid @RequestBody TenantRegistrationRequest request) {

        RegistrationResponse response =
                authService.registerTenant(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // API 2: Login and generate JWT
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}