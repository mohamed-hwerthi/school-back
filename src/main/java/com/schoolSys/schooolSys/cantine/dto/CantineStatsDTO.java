package com.schoolSys.schooolSys.cantine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CantineStatsDTO {
    private long totalAbonnes;
    private long repasAujourdHui;
    private double tauxPresence;
    private BigDecimal revenuesMensuel;

    private Map<String, Long> repartitionTypeAbonnement;
    private List<RepasJourDTO> evolutionRepas;
    private List<TopPlatDTO> topPlats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepasJourDTO {
        private LocalDate date;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPlatDTO {
        private String platPrincipal;
        private long count;
    }
}
