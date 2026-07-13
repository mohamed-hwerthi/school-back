package com.schoolSys.schooolSys.cantine.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDTO {
    private UUID id;
    private LocalDate dateMenu;
    private String jourSemaine;
    private String entree;
    private String platPrincipal;
    private String accompagnement;
    private String dessert;
    private List<String> allergenes;
    private String typeRegime;
    private Integer semaine;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
