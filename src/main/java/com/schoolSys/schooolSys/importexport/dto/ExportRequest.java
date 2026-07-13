package com.schoolSys.schooolSys.importexport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {

    public enum ExportType {
        STUDENTS, TEACHERS, NOTES, PAIEMENTS, ABSENCES
    }

    public enum ExportFormat {
        CSV, XLSX
    }

    private ExportType type;
    private ExportFormat format;
    @Builder.Default
    private Map<String, String> filters = new HashMap<>();
}
