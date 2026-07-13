package com.schoolSys.schooolSys.importjob;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.student.StudentService;
import com.schoolSys.schooolSys.student.dto.StudentBulkImportResultDTO;
import com.schoolSys.schooolSys.student.dto.StudentRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orchestrates background bulk-import jobs. The pattern:
 *
 * <ol>
 *   <li>{@link #createStudentsJob} synchronously inserts a row in {@code import_jobs}
 *       with status PENDING and returns its id immediately.</li>
 *   <li>{@link #runStudentsImport} is annotated {@code @Async} so it runs on a
 *       worker thread. It updates the job row as it progresses.</li>
 *   <li>The UI polls {@link #findById} until status DONE/FAILED.</li>
 * </ol>
 *
 * Multi-tenant note: the worker thread does NOT inherit the request's tenant
 * context, so we capture it before scheduling and re-set it on entry.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportJobService {

    private final ImportJobRepository repository;
    private final StudentService studentService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ImportJob createStudentsJob(int totalRows, String strategy, String createdBy) {
        ImportJob job = ImportJob.builder()
                .type("STUDENTS")
                .status("PENDING")
                .strategy(strategy != null ? strategy : "SKIP")
                .totalRows(totalRows)
                .createdBy(createdBy)
                .build();
        return repository.save(job);
    }

    public ImportJob findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ImportJob", id));
    }

    public List<ImportJob> recentByType(String type) {
        return repository.findTop20ByTypeOrderByCreatedAtDesc(type);
    }

    /**
     * Runs the import on a background thread. Caller MUST first invoke
     * {@link #createStudentsJob} so that the job row already exists.
     *
     * @param tenantId tenant captured by the caller before scheduling
     */
    @Async
    public void runStudentsImport(UUID jobId, List<StudentRequestDTO> rows, String strategy, String tenantId) {
        TenantContext.setCurrentTenant(tenantId);
        try {
            markRunning(jobId);
            StudentBulkImportResultDTO result;
            try {
                result = studentService.importBulkRobust(rows, strategy);
            } catch (Exception ex) {
                log.error("Import job {} failed catastrophically", jobId, ex);
                markFailed(jobId, ex.getMessage());
                return;
            }
            markDone(jobId, result);
        } finally {
            TenantContext.clear();
        }
    }

    /* ─── Internal status updates (each in its own tx so the UI sees progress) ─── */

    @Transactional
    void markRunning(UUID jobId) {
        ImportJob job = repository.findById(jobId).orElseThrow();
        job.setStatus("RUNNING");
        job.setStartedAt(LocalDateTime.now());
        repository.save(job);
    }

    @Transactional
    void markDone(UUID jobId, StudentBulkImportResultDTO result) {
        ImportJob job = repository.findById(jobId).orElseThrow();
        job.setStatus("DONE");
        job.setProcessed(result.getCreated() + result.getSkipped() + result.getFailed());
        job.setSuccessCount(result.getCreated());
        job.setSkippedCount(result.getSkipped());
        job.setErrorCount(result.getFailed());
        job.setErrorsJson(serialize(result.getErrors()));
        job.setFinishedAt(LocalDateTime.now());
        repository.save(job);
    }

    @Transactional
    void markFailed(UUID jobId, String reason) {
        ImportJob job = repository.findById(jobId).orElseThrow();
        job.setStatus("FAILED");
        job.setErrorsJson(serialize(List.of(StudentBulkImportResultDTO.RowError.builder()
                .row(0).code("ERROR").message(reason).build())));
        job.setFinishedAt(LocalDateTime.now());
        repository.save(job);
    }

    private String serialize(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
