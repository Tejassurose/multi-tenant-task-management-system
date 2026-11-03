package com.techpulse.taskmanagement.filter;


import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.security.JwtService;
import com.techpulse.taskmanagement.tenant.TenantContext;
import com.techpulse.taskmanagement.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader =
                    request.getHeader("Authorization");

            // Check whether the request contains a Bearer token
            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                // Validate JWT signature and expiration
                if (jwtService.isTokenValid(token)) {

                    String email = jwtService.extractEmail(token);
                    Long tenantId = jwtService.extractTenantId(token);
                    String role = jwtService.extractRole(token);

                    // Resolve the principal only inside the tenant from the signed JWT.
                    User user = tenantId == null ? null : userRepository
                            .findByEmailAndTenant_Id(email, tenantId)
                            .orElse(null);

                    // Verify that token claims match current user data
                    if (user != null
                            && tenantId != null
                            && tenantId.equals(user.getTenant().getId())
                            && role != null
                            && role.equals(user.getRole().name())
                            && SecurityContextHolder.getContext()
                                    .getAuthentication() == null) {

                        // Store authenticated tenant
                        TenantContext.setTenantId(tenantId);

                        // Create Spring Security authority
                        List<SimpleGrantedAuthority> authorities =
                                List.of(
                                    new SimpleGrantedAuthority(
                                        "ROLE_" + role)
                                );

                        // Create authenticated principal
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        authorities
                                );

                        // Store authentication in SecurityContext
                        SecurityContextHolder.getContext()
                                .setAuthentication(authentication);
                    }
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            // Prevent tenant information leaking between requests
            TenantContext.clear();
        }
    }
}