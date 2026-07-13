package com.schoolSys.schooolSys.niveau.dto;

import java.util.UUID;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClasseResponseDTO {
    private UUID id;
    private String letter;
    private UUID niveauId;
    private String niveauName;
    private String fullName;
}
