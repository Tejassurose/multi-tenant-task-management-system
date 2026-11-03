package com.techpulse.taskmanagement.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import com.techpulse.taskmanagement.dto.request.*;
import com.techpulse.taskmanagement.dto.response.*;
import com.techpulse.taskmanagement.service.AuthService;

class AuthControllerTest {
    @Test void registrationDelegatesToServiceAndReturnsCreated() {
        AuthService service=mock(AuthService.class);
        AuthController controller=new AuthController(service);
        TenantRegistrationRequest request=new TenantRegistrationRequest();
        RegistrationResponse body=new RegistrationResponse(1L,"Acme",2L,"admin@acme.test","ok");
        when(service.registerTenant(request)).thenReturn(body);
        var response=controller.registerTenant(request);
        assertEquals(201,response.getStatusCode().value()); assertSame(body,response.getBody());
    }
    @Test void loginDelegatesToServiceAndReturnsOk() {
        AuthService service=mock(AuthService.class); AuthController controller=new AuthController(service);
        LoginRequest request=new LoginRequest(); LoginResponse body=new LoginResponse("jwt",1L,"a@test.com","ADMIN",3600);
        when(service.login(request)).thenReturn(body);
        var response=controller.login(request);
        assertEquals(200,response.getStatusCode().value()); assertSame(body,response.getBody());
    }
}
