package com.techpulse.taskmanagement.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.techpulse.taskmanagement.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // Disable CSRF for stateless REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Allow H2 console frames
            .headers(headers ->
                headers.frameOptions(frame ->
                    frame.sameOrigin()
                )
            )

            // Use JWT instead of HTTP sessions
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            // Configure endpoint permissions
            .authorizeHttpRequests(auth -> auth

                // Public endpoints: registration, login, Swagger and H2
                .requestMatchers(
                    "/tenant/register",
                    "/auth/login",
                    "/h2-console/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // ADMIN: create users
                .requestMatchers(HttpMethod.POST, "/users")
                    .hasRole("ADMIN")

                // MANAGER: create tasks
                .requestMatchers(HttpMethod.POST, "/tasks")
                    .hasRole("MANAGER")

                // MANAGER: assign tasks
                .requestMatchers(HttpMethod.PUT, "/tasks/*/assign")
                    .hasRole("MANAGER")

                // USER: update assigned task status
                .requestMatchers(HttpMethod.PUT, "/tasks/*/status")
                    .hasRole("USER")

                // ADMIN, MANAGER and USER: view tasks
                .requestMatchers(HttpMethod.GET, "/tasks")
                    .hasAnyRole("ADMIN", "MANAGER", "USER")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Register custom JWT filter
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    // Register the JWT filter only inside Spring Security's filter chain,
    // not a second time as a container-level servlet filter.
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    // BCrypt password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
