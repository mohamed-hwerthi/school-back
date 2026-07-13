package com.schoolSys.schooolSys.tenant.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TenantBillingDTO {

    private String billingEmail;
    private String billingCycle;
    private LocalDate nextBillingDate;
    private BigDecimal monthlyRate;
}
