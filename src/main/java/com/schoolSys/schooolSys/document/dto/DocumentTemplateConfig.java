package com.schoolSys.schooolSys.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTemplateConfig {

    private String schoolName;

    private String schoolLogo;

    private String address;

    private String directorName;

    private String signatures;

    private String headerText;

    private String footerText;
}
