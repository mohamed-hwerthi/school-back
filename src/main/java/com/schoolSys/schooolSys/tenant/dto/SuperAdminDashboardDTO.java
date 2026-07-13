package com.schoolSys.schooolSys.tenant.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class SuperAdminDashboardDTO {

    private long totalTenants;
    private long activeTenants;
    private long totalStudentsAllTenants;
    private BigDecimal revenueMonthly;
    private Map<String, Integer> tenantsByPlan;
}
