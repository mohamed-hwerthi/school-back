package com.schoolSys.schooolSys.examenonline.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChoixReponseDTO {

    private UUID id;
    private String texte;
    private Boolean correct;
    private Integer ordre;
}
