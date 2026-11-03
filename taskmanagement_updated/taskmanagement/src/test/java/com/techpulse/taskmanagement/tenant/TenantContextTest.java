package com.techpulse.taskmanagement.tenant;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextTest {
    @AfterEach void cleanup(){TenantContext.clear();}
    @Test void storesAndClearsTenantId(){
        TenantContext.setTenantId(123L); assertEquals(123L,TenantContext.getTenantId());
        TenantContext.clear(); assertNull(TenantContext.getTenantId());
    }
}
