package com.schoolSys.schooolSys.importexport;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.importexport.dto.ImportResultDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImportExportController {

    private final CsvExportService csvExportService;
    private final ExcelExportService excelExportService;
    private final CsvImportService csvImportService;
    private final ExcelImportService excelImportService;

    // ===================== IMPORT =====================

    @PostMapping("/import/students")
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<ImportResultDTO>> importStudents(
            @RequestParam("file") MultipartFile file) throws IOException {
        ImportResultDTO result = isExcel(file)
                ? excelImportService.importStudents(file.getInputStream())
                : csvImportService.importStudents(file.getInputStream());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/import/teachers")
    @PreAuthorize("hasAuthority('WRITE_TEACHERS')")
    public ResponseEntity<ApiResponse<ImportResultDTO>> importTeachers(
            @RequestParam("file") MultipartFile file) throws IOException {
        ImportResultDTO result = isExcel(file)
                ? excelImportService.importTeachers(file.getInputStream())
                : csvImportService.importTeachers(file.getInputStream());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/import/notes")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<ImportResultDTO>> importNotes(
            @RequestParam("file") MultipartFile file) throws IOException {
        ImportResultDTO result = isExcel(file)
                ? excelImportService.importNotes(file.getInputStream())
                : csvImportService.importNotes(file.getInputStream());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // ===================== EXPORT =====================

    @GetMapping("/export/students")
    @PreAuthorize("hasAuthority('READ_STUDENTS')")
    public ResponseEntity<StreamingResponseBody> exportStudents(
            @RequestParam(defaultValue = "csv") String format) {
        boolean xlsx = "xlsx".equalsIgnoreCase(format);
        String filename = "eleves." + (xlsx ? "xlsx" : "csv");

        StreamingResponseBody body = withTenant(out -> {
            if (xlsx) {
                excelExportService.exportStudents(out);
            } else {
                csvExportService.exportStudents(out);
            }
        });

        return ResponseEntity.ok()
                .contentType(xlsx ? excelMediaType() : csvMediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    @GetMapping("/export/teachers")
    @PreAuthorize("hasAuthority('READ_TEACHERS')")
    public ResponseEntity<StreamingResponseBody> exportTeachers(
            @RequestParam(defaultValue = "csv") String format) {
        boolean xlsx = "xlsx".equalsIgnoreCase(format);
        String filename = "enseignants." + (xlsx ? "xlsx" : "csv");

        StreamingResponseBody body = withTenant(out -> {
            if (xlsx) {
                excelExportService.exportTeachers(out);
            } else {
                csvExportService.exportTeachers(out);
            }
        });

        return ResponseEntity.ok()
                .contentType(xlsx ? excelMediaType() : csvMediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    @GetMapping("/export/notes")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<StreamingResponseBody> exportNotes(
            @RequestParam UUID classeId,
            @RequestParam Integer trimestre,
            @RequestParam(defaultValue = "csv") String format) {
        boolean xlsx = "xlsx".equalsIgnoreCase(format);
        String filename = "notes_classe" + classeId + "_T" + trimestre + "." + (xlsx ? "xlsx" : "csv");

        StreamingResponseBody body = withTenant(out -> {
            if (xlsx) {
                excelExportService.exportNotes(classeId, trimestre, out);
            } else {
                csvExportService.exportNotes(classeId, trimestre, out);
            }
        });

        return ResponseEntity.ok()
                .contentType(xlsx ? excelMediaType() : csvMediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    @GetMapping("/export/paiements")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<StreamingResponseBody> exportPaiements(
            @RequestParam String anneeScolaire,
            @RequestParam(defaultValue = "csv") String format) {
        boolean xlsx = "xlsx".equalsIgnoreCase(format);
        String filename = "paiements_" + anneeScolaire + "." + (xlsx ? "xlsx" : "csv");

        StreamingResponseBody body = withTenant(out -> {
            if (xlsx) {
                excelExportService.exportPaiements(anneeScolaire, out);
            } else {
                csvExportService.exportPaiements(anneeScolaire, out);
            }
        });

        return ResponseEntity.ok()
                .contentType(xlsx ? excelMediaType() : csvMediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    @GetMapping("/export/absences")
    @PreAuthorize("hasAuthority('READ_ABSENCES')")
    public ResponseEntity<StreamingResponseBody> exportAbsences(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(defaultValue = "csv") String format) {
        boolean xlsx = "xlsx".equalsIgnoreCase(format);
        String filename = "absences_" + from + "_" + to + "." + (xlsx ? "xlsx" : "csv");

        StreamingResponseBody body = withTenant(out -> {
            if (xlsx) {
                excelExportService.exportAbsences(from, to, out);
            } else {
                csvExportService.exportAbsences(from, to, out);
            }
        });

        return ResponseEntity.ok()
                .contentType(xlsx ? excelMediaType() : csvMediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    // ===================== TEMPLATES =====================

    @GetMapping("/import/template/students")
    public ResponseEntity<StreamingResponseBody> studentTemplate(
            @RequestParam(defaultValue = "csv") String format) {
        boolean xlsx = "xlsx".equalsIgnoreCase(format);
        String filename = "template_eleves." + (xlsx ? "xlsx" : "csv");

        StreamingResponseBody body = out -> {
            if (xlsx) {
                excelExportService.writeStudentTemplate(out);
            } else {
                csvExportService.writeStudentTemplate(out);
            }
        };

        return ResponseEntity.ok()
                .contentType(xlsx ? excelMediaType() : csvMediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    @GetMapping("/import/template/teachers")
    public ResponseEntity<StreamingResponseBody> teacherTemplate(
            @RequestParam(defaultValue = "csv") String format) {
        boolean xlsx = "xlsx".equalsIgnoreCase(format);
        String filename = "template_enseignants." + (xlsx ? "xlsx" : "csv");

        StreamingResponseBody body = out -> {
            if (xlsx) {
                excelExportService.writeTeacherTemplate(out);
            } else {
                csvExportService.writeTeacherTemplate(out);
            }
        };

        return ResponseEntity.ok()
                .contentType(xlsx ? excelMediaType() : csvMediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    // ===================== HELPERS =====================

    private boolean isExcel(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name != null && (name.endsWith(".xlsx") || name.endsWith(".xls"));
    }

    private MediaType excelMediaType() {
        return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    private MediaType csvMediaType() {
        return MediaType.parseMediaType("text/csv; charset=UTF-8");
    }

    /**
     * StreamingResponseBody s'exécute dans un thread asynchrone où le
     * ThreadLocal de TenantContext n'est pas propagé. On capture le tenant
     * dans le thread de requête puis on le réinjecte dans le thread async.
     */
    private StreamingResponseBody withTenant(StreamingResponseBody inner) {
        String tenant = TenantContext.getCurrentTenant();
        return out -> {
            TenantContext.setCurrentTenant(tenant);
            try {
                inner.writeTo(out);
            } finally {
                TenantContext.clear();
            }
        };
    }
}
