package com.techpulse.taskmanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.techpulse.taskmanagement.dto.request.LoginRequest;
import com.techpulse.taskmanagement.dto.request.TenantRegistrationRequest;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.enums.Role;
import com.techpulse.taskmanagement.repository.TenantRepository;
import com.techpulse.taskmanagement.repository.UserRepository;
import com.techpulse.taskmanagement.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock TenantRepository tenants;
    @Mock UserRepository users;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @InjectMocks AuthService service;

    @BeforeEach void setExpiration() throws Exception {
        Field f = AuthService.class.getDeclaredField("jwtExpiration");
        f.setAccessible(true); f.set(service, 3600000L);
    }

    @Test void registerCreatesTenantAndAdminWithEncodedPassword() {
        TenantRegistrationRequest req = new TenantRegistrationRequest();
        req.setTenantName("Acme"); req.setEmail("admin@acme.test"); req.setPassword("password123");
        when(users.existsByEmail(req.getEmail())).thenReturn(false);
        when(tenants.save(any(Tenant.class))).thenAnswer(i -> { Tenant t=i.getArgument(0); t.setId(10L); return t; });
        when(encoder.encode("password123")).thenReturn("encoded");
        when(users.save(any(User.class))).thenAnswer(i -> { User u=i.getArgument(0); u.setId(20L); return u; });
        var result=service.registerTenant(req);
        assertEquals(10L,result.getTenantId()); assertEquals(20L,result.getAdminId());
        verify(users).save(argThat(u -> u.getRole()==Role.ADMIN && u.getPassword().equals("encoded")));
    }

    @Test void registrationRejectsDuplicateEmail() {
        TenantRegistrationRequest req=new TenantRegistrationRequest(); req.setEmail("taken@test.com");
        when(users.existsByEmail("taken@test.com")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> service.registerTenant(req));
        verify(tenants, never()).save(any());
    }

    @Test void loginReturnsJwtAfterPasswordMatches() {
        Tenant tenant=new Tenant("Acme"); tenant.setId(7L);
        User user=new User("user@acme.test","hash",Role.USER,tenant); user.setId(8L);
        LoginRequest req=new LoginRequest(); req.setEmail(user.getEmail()); req.setPassword("password123");
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches("password123","hash")).thenReturn(true);
        when(jwt.generateToken(user)).thenReturn("signed.jwt.token");
        var result=service.login(req);
        assertEquals("signed.jwt.token",result.getToken()); assertEquals(7L,result.getTenantId());
        assertEquals("USER",result.getRole());
    }

    @Test void loginRejectsWrongPassword() {
        User user=new User("user@test.com","hash",Role.USER,new Tenant("Acme"));
        LoginRequest req=new LoginRequest(); req.setEmail(user.getEmail()); req.setPassword("wrong");
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches("wrong","hash")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.login(req));
        verify(jwt,never()).generateToken(any());
    }
}
