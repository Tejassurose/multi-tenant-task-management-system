package com.techpulse.taskmanagement.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.techpulse.taskmanagement.dto.request.CreateUserRequest;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.enums.Role;
import com.techpulse.taskmanagement.repository.TenantRepository;
import com.techpulse.taskmanagement.repository.UserRepository;
import com.techpulse.taskmanagement.tenant.TenantContext;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository users; @Mock TenantRepository tenants; @Mock PasswordEncoder encoder;
    @InjectMocks UserService service;
    @AfterEach void clearTenant(){ TenantContext.clear(); }

    @Test void createsUserInsideCurrentTenant() {
        Tenant tenant=new Tenant("Acme"); tenant.setId(3L); TenantContext.setTenantId(3L);
        CreateUserRequest req=new CreateUserRequest(); req.setEmail("worker@test.com"); req.setPassword("password123"); req.setRole(Role.USER);
        when(users.existsByEmail(req.getEmail())).thenReturn(false); when(tenants.findById(3L)).thenReturn(Optional.of(tenant));
        when(encoder.encode("password123")).thenReturn("hash");
        when(users.save(any(User.class))).thenAnswer(i->{User u=i.getArgument(0);u.setId(9L);return u;});
        var result=service.createUser(req);
        assertEquals(3L,result.getTenantId()); assertEquals("worker@test.com",result.getEmail());
        verify(users).save(argThat(u->u.getTenant()==tenant && u.getPassword().equals("hash")));
    }

    @Test void cannotCreateAdminThroughUsersApi() {
        TenantContext.setTenantId(3L); CreateUserRequest req=new CreateUserRequest(); req.setRole(Role.ADMIN);
        assertThrows(IllegalArgumentException.class,()->service.createUser(req)); verify(users,never()).save(any());
    }

    @Test void requiresTenantContext() {
        TenantContext.clear(); assertThrows(IllegalStateException.class,()->service.createUser(new CreateUserRequest()));
    }
}
