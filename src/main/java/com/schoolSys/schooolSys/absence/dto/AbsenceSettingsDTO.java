package com.schoolSys.schooolSys.absence.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceSettingsDTO {
    private UUID id;
    private Integer seuilAlerteJaune;
    private Integer seuilAlerteRouge;
    private Boolean notificationAuto;
    private Boolean notificationEmail;
    private Boolean notificationSms;
}
