package com.schoolSys.schooolSys.finance.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TresorerieDTO {

    private BigDecimal totalEntrees;
    private BigDecimal totalSorties;
    private BigDecimal solde;
    private BigDecimal totalDu;
    private BigDecimal totalImpayes;
    private BigDecimal tauxRecouvrement;

    private long elevesAJour;
    private long elevesEnRetard;
    private long totalEleves;

    private List<FluxMensuel> fluxMensuels;
    private List<TopDebiteur> topDebiteurs;
    private List<RepartitionDepense> repartitionDepenses;

    @Data
    @Builder
    public static class FluxMensuel {
        private String mois;
        private BigDecimal entrees;
        private BigDecimal sorties;
        private BigDecimal solde;
    }

    @Data
    @Builder
    public static class TopDebiteur {
        private UUID studentId;
        private String studentName;
        private String classe;
        private BigDecimal montantDu;
        private BigDecimal montantPaye;
        private BigDecimal solde;
    }

    @Data
    @Builder
    public static class RepartitionDepense {
        private String categorie;
        private BigDecimal montant;
    }
}
