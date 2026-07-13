package com.schoolSys.schooolSys.importjob.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.student.dto.StudentBulkImportResultDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportJobDTO {
    private UUID id;
    private String type;
    private String status;
    private String strategy;
    private Integer totalRows;
    private Integer processed;
    private Integer successCount;
    private Integer skippedCount;
    private Integer errorCount;
    private List<StudentBulkImportResultDTO.RowError> errors;
    private String createdBy;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
}
