package com.schoolSys.schooolSys.tenant.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TenantPlanDTO {

    private String plan;
    private Integer maxStudents;
    private Integer maxTeachers;
    private Integer maxStorageMb;
    private BigDecimal monthlyRate;
    private List<String> features;
}
