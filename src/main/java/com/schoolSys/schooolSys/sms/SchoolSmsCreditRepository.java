package com.schoolSys.schooolSys.sms;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SchoolSmsCreditRepository extends JpaRepository<SchoolSmsCredit, String> {
    Optional<SchoolSmsCredit> findByTenantId(String tenantId);
    boolean existsByTenantId(String tenantId);
}
