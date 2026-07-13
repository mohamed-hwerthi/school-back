package com.schoolSys.schooolSys.bulletin.dto;

import java.util.UUID;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulletinExamenDTO {
    private UUID examenId;
    private String examenName;
    private Double coeff;
    private Double note;
}
