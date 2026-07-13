package com.schoolSys.schooolSys.importexport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportErrorDTO {
    private int rowNumber;
    private String field;
    private String message;
}
