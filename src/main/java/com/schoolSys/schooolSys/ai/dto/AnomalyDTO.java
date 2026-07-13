package com.schoolSys.schooolSys.ai.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDTO {
    private AnomalyType type;
    private String description;
    private String severity; // LOW, MEDIUM, HIGH
    private UUID studentId;

    public enum AnomalyType {
        GRADE_DROP, HIGH_ABSENCE, BEHAVIOR_PATTERN
    }
}
