package com.schoolSys.schooolSys.tenant.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantUsageDTO {

    private long currentStudents;
    private Integer maxStudents;
    private long currentTeachers;
    private Integer maxTeachers;
    private long storageUsedMb;
    private Integer maxStorageMb;
}
