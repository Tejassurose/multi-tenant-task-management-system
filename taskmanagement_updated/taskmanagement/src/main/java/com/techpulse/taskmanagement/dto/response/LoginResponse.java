package com.techpulse.taskmanagement.dto.response;

public class LoginResponse {

    private String token;
    private String tokenType;
    private Long tenantId;
    private String email;
    private String role;
    private long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long tenantId, String email,
            String role, long expiresIn) {
        this.token = token;
        this.tokenType = "Bearer";
        this.tenantId = tenantId;
        this.email = email;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
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

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}