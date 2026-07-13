package com.schoolSys.schooolSys.ai.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDetectAnomaliesRequest {
    private UUID studentId;
    private List<Double> notes;
    private List<Integer> absences;
}
