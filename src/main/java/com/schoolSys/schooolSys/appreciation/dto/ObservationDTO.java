package com.schoolSys.schooolSys.appreciation.dto;

import java.util.UUID;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObservationDTO {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private Integer trimestre;
    private String comportement;
    private String certificatType;
}
