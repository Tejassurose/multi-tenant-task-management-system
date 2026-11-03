package com.techpulse.taskmanagement.dto.response;
//package com.techpulse.taskmanagement.dto.response;

public class UserResponse {

    private Long id;
    private String email;
    private String role;
    private Long tenantId;
    private String message;

    public UserResponse() {
    }

    public UserResponse(Long id, String email, String role,
            Long tenantId, String message) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.tenantId = tenantId;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}