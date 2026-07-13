package com.schoolSys.schooolSys.settings;

import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.settings.dto.SchoolSettingsDTO;
import com.schoolSys.schooolSys.tenant.Tenant;
import com.schoolSys.schooolSys.tenant.TenantRepository;
import com.schoolSys.schooolSys.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchoolSettingsService {

    private final SchoolSettingsRepository repository;
    private final TenantRepository tenantRepository;

    public SchoolSettingsDTO getSettings() {
        SchoolSettings s = getOrCreate();
        return toDTO(s);
    }

    @Transactional
    public SchoolSettingsDTO updateSettings(SchoolSettingsDTO dto) {
        SchoolSettings s = getOrCreate();
        String oldName = s.getSchoolName();
        s.setSchoolName(dto.getSchoolName());
        s.setSchoolNameAr(dto.getSchoolNameAr());
        s.setAnneeScolaire(dto.getAnneeScolaire());
        s.setAdresse(dto.getAdresse());
        s.setTelephone(dto.getTelephone());
        s.setDirecteurName(dto.getDirecteurName());
        s.setDirecteurNameAr(dto.getDirecteurNameAr());
        s.setDelegationRegionale(dto.getDelegationRegionale());
        s.setDelegationRegionaleAr(dto.getDelegationRegionaleAr());
        s.setLogo(dto.getLogo());
        s.setVille(dto.getVille());
        s.setVilleAr(dto.getVilleAr());
        s.setEmail(dto.getEmail());
        s.setSiteWeb(dto.getSiteWeb());
        s.setAnneeCreation(dto.getAnneeCreation());
        s.setDescription(dto.getDescription());
        s.setUpdatedAt(LocalDateTime.now());
        SchoolSettingsDTO saved = toDTO(repository.save(s));

        // If the school name changed, regenerate the tenant slug from it.
        if (dto.getSchoolName() != null && !dto.getSchoolName().equals(oldName)) {
            syncTenantSlug(dto.getSchoolName());
        }

        return saved;
    }

    /**
     * Regenerates the current tenant's URL slug from the school name.
     * Falls back to a numbered suffix if the desired slug is already taken.
     */
    private void syncTenantSlug(String schoolName) {
        String schemaName = TenantContext.getCurrentTenant();
        if (schemaName == null || schemaName.isBlank() || "public".equals(schemaName)) return;

        Optional<Tenant> tenantOpt = tenantRepository.findBySchemaName(schemaName);
        if (tenantOpt.isEmpty()) return;
        Tenant tenant = tenantOpt.get();

        String base = TenantService.slugify(schoolName);
        if (base.isBlank()) return;
        if (base.equals(tenant.getSlug())) return;

        String candidate = base;
        int suffix = 2;
        while (tenantRepository.findBySlugAndActiveTrue(candidate)
                .map(t -> !t.getId().equals(tenant.getId()))
                .orElse(false)) {
            candidate = base + "-" + suffix++;
        }
        tenant.setSlug(candidate);
        tenantRepository.save(tenant);
        log.info("Tenant {} slug updated: {} -> {}", schemaName, tenant.getSlug(), candidate);
    }

    private SchoolSettings getOrCreate() {
        return repository.findAll().stream().findFirst()
                .orElseGet(() -> repository.save(SchoolSettings.builder().build()));
    }

    private SchoolSettingsDTO toDTO(SchoolSettings s) {
        return SchoolSettingsDTO.builder()
                .schoolName(s.getSchoolName())
                .schoolNameAr(s.getSchoolNameAr())
                .anneeScolaire(s.getAnneeScolaire())
                .adresse(s.getAdresse())
                .telephone(s.getTelephone())
                .directeurName(s.getDirecteurName())
                .directeurNameAr(s.getDirecteurNameAr())
                .delegationRegionale(s.getDelegationRegionale())
                .delegationRegionaleAr(s.getDelegationRegionaleAr())
                .logo(s.getLogo())
                .ville(s.getVille())
                .villeAr(s.getVilleAr())
                .email(s.getEmail())
                .siteWeb(s.getSiteWeb())
                .anneeCreation(s.getAnneeCreation())
                .description(s.getDescription())
                .build();
    }
}
