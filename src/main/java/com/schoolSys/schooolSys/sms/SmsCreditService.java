package com.schoolSys.schooolSys.sms;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsCreditService {

    private final SchoolSmsCreditRepository repository;

    public SchoolSmsCredit getCredits(String tenantId) {
        return repository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("SMS credits not found for tenant: " + tenantId));
    }

    public int getRemaining(String tenantId) {
        return repository.findByTenantId(tenantId)
                .map(SchoolSmsCredit::getRemaining)
                .orElse(0);
    }

    public List<SchoolSmsCredit> getAllCredits() {
        return repository.findAll();
    }

    @Transactional
    public SchoolSmsCredit setCredits(String tenantId, int totalCredits) {
        SchoolSmsCredit credits = repository.findByTenantId(tenantId)
                .orElse(SchoolSmsCredit.builder()
                        .tenantId(tenantId)
                        .totalCredits(0)
                        .usedCredits(0)
                        .build());
        credits.setTotalCredits(totalCredits);
        credits.setUpdatedAt(LocalDateTime.now());
        SchoolSmsCredit saved = repository.save(credits);
        log.info("SMS credits set for tenant '{}': total={}, used={}", tenantId, totalCredits, credits.getUsedCredits());
        return saved;
    }

    @Transactional
    public SchoolSmsCredit addCredits(String tenantId, int additionalCredits) {
        SchoolSmsCredit credits = repository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("SMS credits not found for tenant: " + tenantId));
        credits.setTotalCredits(credits.getTotalCredits() + additionalCredits);
        credits.setUpdatedAt(LocalDateTime.now());
        SchoolSmsCredit saved = repository.save(credits);
        log.info("Added {} SMS credits to tenant '{}': total={}, used={}",
                additionalCredits, tenantId, saved.getTotalCredits(), saved.getUsedCredits());
        return saved;
    }

    @Transactional
    public void initializeCredits(String tenantId, int totalCredits) {
        if (repository.existsByTenantId(tenantId)) {
            log.warn("SMS credits already exist for tenant '{}', skipping initialization", tenantId);
            return;
        }
        SchoolSmsCredit credits = SchoolSmsCredit.builder()
                .tenantId(tenantId)
                .totalCredits(totalCredits)
                .usedCredits(0)
                .updatedAt(LocalDateTime.now())
                .build();
        repository.save(credits);
        log.info("Initialized SMS credits for tenant '{}': total={}", tenantId, totalCredits);
    }

    @Transactional
    public boolean checkAndDeduct(String tenantId, int count) {
        SchoolSmsCredit credits = repository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("SMS credits not found for tenant: " + tenantId));

        int remaining = credits.getRemaining();
        if (remaining < count) {
            log.warn("Insufficient SMS credits for tenant '{}': required={}, remaining={}",
                    tenantId, count, remaining);
            return false;
        }

        credits.setUsedCredits(credits.getUsedCredits() + count);
        credits.setUpdatedAt(LocalDateTime.now());
        repository.save(credits);
        log.info("Deducted {} SMS credits from tenant '{}': remaining={}",
                count, tenantId, credits.getRemaining());
        return true;
    }
}
