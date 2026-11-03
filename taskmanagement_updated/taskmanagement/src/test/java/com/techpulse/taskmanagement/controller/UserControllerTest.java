package com.techpulse.taskmanagement.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import com.techpulse.taskmanagement.dto.request.CreateUserRequest;
import com.techpulse.taskmanagement.dto.response.UserResponse;
import com.techpulse.taskmanagement.service.UserService;

class UserControllerTest {
    @Test void createUserReturnsCreatedResponse() {
        UserService service=mock(UserService.class); UserController controller=new UserController(service);
        CreateUserRequest request=new CreateUserRequest(); UserResponse body=new UserResponse(3L,"u@test.com","USER",1L,"created");
        when(service.createUser(request)).thenReturn(body);
        var response=controller.createUser(request);
        assertEquals(201,response.getStatusCode().value()); assertSame(body,response.getBody());
    }
}
