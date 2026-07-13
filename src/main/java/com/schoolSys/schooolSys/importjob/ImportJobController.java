package com.schoolSys.schooolSys.importjob;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.importjob.dto.ImportJobDTO;
import com.schoolSys.schooolSys.importjob.dto.StudentImportAsyncRequest;
import com.schoolSys.schooolSys.student.dto.StudentBulkImportResultDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/import-jobs")
@RequiredArgsConstructor
public class ImportJobController {

    private final ImportJobService service;
    private final ObjectMapper objectMapper;

    @PostMapping("/students")
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<ImportJobDTO>> startStudentsImport(
            @RequestBody StudentImportAsyncRequest request) {
        ImportJob job = service.createStudentsJob(
                request.getRows() == null ? 0 : request.getRows().size(),
                request.getStrategy(),
                request.getCreatedBy());

        // Capture tenant on the request thread so the worker can re-set it
        String tenant = TenantContext.getCurrentTenant();
        service.runStudentsImport(job.getId(), request.getRows(), request.getStrategy(), tenant);

        return ResponseEntity.accepted().body(ApiResponse.ok(toDto(job)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<ImportJobDTO>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(toDto(service.findById(id))));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<List<ImportJobDTO>>> list(
            @RequestParam(defaultValue = "STUDENTS") String type) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.recentByType(type).stream().map(this::toDto).toList()));
    }

    private ImportJobDTO toDto(ImportJob j) {
        List<StudentBulkImportResultDTO.RowError> errs = Collections.emptyList();
        if (j.getErrorsJson() != null && !j.getErrorsJson().isBlank()) {
            try {
                errs = objectMapper.readValue(
                        j.getErrorsJson(),
                        new TypeReference<List<StudentBulkImportResultDTO.RowError>>() {});
            } catch (Exception ignore) { /* keep empty */ }
        }
        return ImportJobDTO.builder()
                .id(j.getId())
                .type(j.getType())
                .status(j.getStatus())
                .strategy(j.getStrategy())
                .totalRows(j.getTotalRows())
                .processed(j.getProcessed())
                .successCount(j.getSuccessCount())
                .skippedCount(j.getSkippedCount())
                .errorCount(j.getErrorCount())
                .errors(errs)
                .createdBy(j.getCreatedBy())
                .startedAt(j.getStartedAt())
                .finishedAt(j.getFinishedAt())
                .createdAt(j.getCreatedAt())
                .build();
    }
}
