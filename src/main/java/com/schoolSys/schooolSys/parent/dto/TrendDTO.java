package com.schoolSys.schooolSys.parent.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Moyenne par trimestre pour l'enfant — alimente la sparkline du home parent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDTO {

    private List<TrendPoint> points;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private Integer trimestre;
        private Double moyenne;
        private Integer noteCount;
    }
}
