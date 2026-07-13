package com.schoolSys.schooolSys.appreciation.dto;

import java.util.UUID;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommandationDTO {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private UUID domaineId;
    private String domaineName;
    private Integer trimestre;
    private String texte;
}
