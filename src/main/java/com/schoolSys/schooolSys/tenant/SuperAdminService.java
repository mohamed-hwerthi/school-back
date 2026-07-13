package com.schoolSys.schooolSys.tenant;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.tenant.dto.SuperAdminDashboardDTO;
import com.schoolSys.schooolSys.tenant.dto.TenantResponseDTO;
import com.schoolSys.schooolSys.tenant.dto.TenantUsageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final TenantBillingService tenantBillingService;

    public SuperAdminDashboardDTO getDashboard() {
        List<Tenant> tenants = tenantRepository.findAll();

        long totalTenants = tenants.size();
        long activeTenants = tenants.stream().filter(Tenant::isActive).count();

        BigDecimal revenueMonthly = tenants.stream()
                .filter(Tenant::isActive)
                .map(t -> t.getMonthlyRate() != null ? t.getMonthlyRate() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Integer> tenantsByPlan = new HashMap<>();
        for (Tenant t : tenants) {
            String plan = t.getPlan() != null ? t.getPlan() : "FREE";
            tenantsByPlan.merge(plan, 1, Integer::sum);
        }

        return SuperAdminDashboardDTO.builder()
                .totalTenants(totalTenants)
                .activeTenants(activeTenants)
                .totalStudentsAllTenants(0)
                .revenueMonthly(revenueMonthly)
                .tenantsByPlan(tenantsByPlan)
                .build();
    }

    public List<TenantResponseDTO> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(tenantMapper::toResponseDTO)
                .toList();
    }

    public TenantUsageDTO getTenantUsage(UUID tenantId) {
        return tenantBillingService.getUsage(tenantId);
    }

    @Transactional
    public void activateTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", id));
        tenant.setActive(true);
        tenantRepository.save(tenant);
    }

    @Transactional
    public void deactivateTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", id));
        tenant.setActive(false);
        tenantRepository.save(tenant);
    }
}
