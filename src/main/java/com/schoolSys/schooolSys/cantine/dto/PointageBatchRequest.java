package com.schoolSys.schooolSys.cantine.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointageBatchRequest {
    @NotNull
    private LocalDate dateRepas;
    private String typeRepas;
    @NotNull
    private List<PointageItem> pointages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointageItem {
        private UUID eleveId;
        private Boolean present;
    }
}
