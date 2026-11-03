package com.techpulse.taskmanagement.dto.response;

public class RegistrationResponse {

    private Long tenantId;
    private String tenantName;
    private Long adminId;
    private String adminEmail;
    private String message;

    public RegistrationResponse() {
    }

    public RegistrationResponse(Long tenantId, String tenantName,
            Long adminId, String adminEmail, String message) {
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.adminId = adminId;
        this.adminEmail = adminEmail;
        this.message = message;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}