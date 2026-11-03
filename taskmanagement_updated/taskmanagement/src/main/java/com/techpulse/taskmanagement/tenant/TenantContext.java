package com.techpulse.taskmanagement.tenant;
public class TenantContext {

    // Separate tenant ID for each request thread
    private static final ThreadLocal<Long> CURRENT_TENANT =
            new ThreadLocal<>();

    // Store the current tenant ID
    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    // Retrieve the current tenant ID
    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    // Remove the tenant ID after request processing
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}