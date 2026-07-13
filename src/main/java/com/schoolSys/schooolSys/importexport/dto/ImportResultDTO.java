package com.schoolSys.schooolSys.importexport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {
    @Builder.Default
    private int totalRows = 0;
    @Builder.Default
    private int imported = 0;
    @Builder.Default
    private int skipped = 0;
    @Builder.Default
    private List<ImportErrorDTO> errors = new ArrayList<>();
}
