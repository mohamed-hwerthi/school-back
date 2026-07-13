package com.schoolSys.schooolSys.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enable2FAResponseDTO {

    private String secret;
    private String qrCodeUri;
}
