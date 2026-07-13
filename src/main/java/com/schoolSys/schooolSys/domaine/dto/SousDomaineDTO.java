package com.schoolSys.schooolSys.domaine.dto;

import java.util.UUID;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SousDomaineDTO {
    private UUID id;
    private String name;
    private String nameAr;
    private Integer ordre;
    private UUID domaineId;
}
