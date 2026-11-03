package com.techpulse.taskmanagement.security;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.enums.Role;

class JwtServiceTest {
    private JwtService jwt;
    @BeforeEach void setup() throws Exception {
        jwt=new JwtService();
        set("jwtSecret","dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQ=");
        set("jwtExpiration",3600000L);
    }
    private void set(String name,Object value)throws Exception {
        Field f=JwtService.class.getDeclaredField(name);f.setAccessible(true);f.set(jwt,value);
    }
    @Test void tokenContainsSubjectTenantAndRoleClaims() {
        Tenant tenant=new Tenant("Acme");tenant.setId(77L);
        User user=new User("manager@acme.test","hash",Role.MANAGER,tenant);
        String token=jwt.generateToken(user);
        assertTrue(jwt.isTokenValid(token));
        assertEquals("manager@acme.test",jwt.extractEmail(token));
        assertEquals(77L,jwt.extractTenantId(token));
        assertEquals("MANAGER",jwt.extractRole(token));
    }
    @Test void malformedTokenIsRejected() {
        assertFalse(jwt.isTokenValid("not.a.jwt"));
    }
}
