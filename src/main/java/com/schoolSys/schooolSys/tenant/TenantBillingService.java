package com.schoolSys.schooolSys.tenant;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.tenant.dto.TenantBillingDTO;
import com.schoolSys.schooolSys.tenant.dto.TenantPlanDTO;
import com.schoolSys.schooolSys.tenant.dto.TenantUsageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TenantBillingService {

    private final TenantRepository tenantRepository;

    private static final Map<String, PlanInfo> PLAN_INFO = Map.of(
            "FREE", new PlanInfo(50, 10, 500, BigDecimal.ZERO,
                    List.of("50 eleves max", "10 enseignants max", "Modules de base", "Support communautaire")),
            "STANDARD", new PlanInfo(200, 30, 2000, BigDecimal.valueOf(49.99),
                    List.of("200 eleves max", "30 enseignants max", "Tous les modules", "Support email", "Rapports avances")),
            "PREMIUM", new PlanInfo(500, 100, 10000, BigDecimal.valueOf(149.99),
                    List.of("500 eleves max", "100 enseignants max", "Tous les modules", "Support prioritaire", "Analytics avances", "API access")),
            "ENTERPRISE", new PlanInfo(Integer.MAX_VALUE, Integer.MAX_VALUE, 50000, BigDecimal.valueOf(499.99),
                    List.of("Eleves illimites", "Enseignants illimites", "Tous les modules", "Support dedie 24/7", "Analytics BI", "API illimitee", "Personnalisation"))
    );

    public TenantPlanDTO getPlanDetails(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        PlanInfo info = PLAN_INFO.getOrDefault(tenant.getPlan(), PLAN_INFO.get("FREE"));

        return TenantPlanDTO.builder()
                .plan(tenant.getPlan())
                .maxStudents(tenant.getMaxStudents())
                .maxTeachers(tenant.getMaxTeachers())
                .maxStorageMb(tenant.getMaxStorageMb())
                .monthlyRate(tenant.getMonthlyRate())
                .features(info.features)
                .build();
    }

    @Transactional
    public TenantPlanDTO changePlan(UUID tenantId, String newPlan) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        PlanInfo info = PLAN_INFO.get(newPlan.toUpperCase());
        if (info == null) {
            throw new IllegalArgumentException("Invalid plan: " + newPlan);
        }

        tenant.setPlan(newPlan.toUpperCase());
        tenant.setMaxStudents(info.maxStudents);
        tenant.setMaxTeachers(info.maxTeachers);
        tenant.setMaxStorageMb(info.maxStorageMb);
        tenant.setMonthlyRate(info.monthlyRate);
        tenantRepository.save(tenant);

        return TenantPlanDTO.builder()
                .plan(tenant.getPlan())
                .maxStudents(tenant.getMaxStudents())
                .maxTeachers(tenant.getMaxTeachers())
                .maxStorageMb(tenant.getMaxStorageMb())
                .monthlyRate(tenant.getMonthlyRate())
                .features(info.features)
                .build();
    }

    @Transactional
    public TenantBillingDTO updateBilling(UUID tenantId, TenantBillingDTO dto) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        if (dto.getBillingEmail() != null) tenant.setBillingEmail(dto.getBillingEmail());
        if (dto.getBillingCycle() != null) tenant.setBillingCycle(dto.getBillingCycle());
        if (dto.getNextBillingDate() != null) tenant.setNextBillingDate(dto.getNextBillingDate());
        tenantRepository.save(tenant);

        return TenantBillingDTO.builder()
                .billingEmail(tenant.getBillingEmail())
                .billingCycle(tenant.getBillingCycle())
                .nextBillingDate(tenant.getNextBillingDate())
                .monthlyRate(tenant.getMonthlyRate())
                .build();
    }

    public TenantUsageDTO getUsage(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        // Usage would normally query the tenant schema; we return placeholder values
        return TenantUsageDTO.builder()
                .currentStudents(0)
                .maxStudents(tenant.getMaxStudents())
                .currentTeachers(0)
                .maxTeachers(tenant.getMaxTeachers())
                .storageUsedMb(0)
                .maxStorageMb(tenant.getMaxStorageMb())
                .build();
    }

    private record PlanInfo(int maxStudents, int maxTeachers, int maxStorageMb,
                            BigDecimal monthlyRate, List<String> features) {}
}
