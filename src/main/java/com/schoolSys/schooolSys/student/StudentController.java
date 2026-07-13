package com.schoolSys.schooolSys.student;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.student.dto.ReinscriptionRequestDTO;
import com.schoolSys.schooolSys.student.dto.StudentBulkImportResultDTO;
import com.schoolSys.schooolSys.student.dto.StudentRequestDTO;
import com.schoolSys.schooolSys.student.dto.StudentResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final DocumentService documentService;

    /**
     * Paginated + filtered list.
     * GET /api/students?page=0&size=20&search=ben&niveau=3eme&classe=3A&status=Actif&sex=M&blocked=false&sort=lastName,asc
     */
    @GetMapping
    @PreAuthorize("hasAuthority('READ_STUDENTS')")
    public ResponseEntity<ApiResponse<PagedResponse<StudentResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String niveau,
            @RequestParam(required = false) String classe,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sex,
            @RequestParam(required = false) Boolean blocked,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<StudentResponseDTO> result = studentService.findAll(
                search, niveau, classe, status, sex, blocked, pageable
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_STUDENTS')")
    public ResponseEntity<ApiResponse<StudentResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<StudentResponseDTO>> create(@Valid @RequestBody StudentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(studentService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<StudentResponseDTO>> update(@PathVariable UUID id,
                                                                   @Valid @RequestBody StudentRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_STUDENTS')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<List<StudentResponseDTO>>> importBulk(
            @Valid @RequestBody List<StudentRequestDTO> dtos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(studentService.importBulk(dtos)));
    }

    /**
     * Robust bulk import — returns a per-row result instead of all-or-nothing.
     * Conflicts (e.g. duplicate email) are reported as skipped rows; the rest
     * of the batch still goes through.
     */
    @PostMapping("/import/robust")
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<StudentBulkImportResultDTO>> importBulkRobust(
            @RequestBody List<StudentRequestDTO> dtos) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.importBulkRobust(dtos)));
    }

    // ===================== ELV-007: Document upload endpoints =====================

    @PostMapping("/{id}/documents")
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<DocumentEleve>> uploadDocument(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "AUTRE") DocumentEleve.DocumentType type) {
        DocumentEleve doc = documentService.upload(id, file, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(doc));
    }

    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAuthority('READ_STUDENTS')")
    public ResponseEntity<ApiResponse<List<DocumentEleve>>> listDocuments(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(documentService.listByStudent(id)));
    }

    @GetMapping("/documents/{docId}/download")
    @PreAuthorize("hasAuthority('READ_STUDENTS')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID docId) throws IOException {
        DocumentEleve doc = documentService.findById(docId);
        Path path = Paths.get(doc.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        doc.getContentType() != null ? doc.getContentType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + doc.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/documents/{docId}")
    @PreAuthorize("hasAuthority('DELETE_STUDENTS')")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID docId) {
        documentService.delete(docId);
        return ResponseEntity.noContent().build();
    }

    // ===================== ELV-009: Reinscription en masse =====================

    @PostMapping("/reinscription-masse")
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<List<StudentResponseDTO>>> reinscriptionMasse(
            @RequestBody ReinscriptionRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.reinscriptionMasse(dto)));
    }

    // ===================== ELV-010: Excel import template =====================

    @GetMapping("/import/template")
    @PreAuthorize("hasAuthority('READ_STUDENTS')")
    public ResponseEntity<Resource> downloadImportTemplate() {
        byte[] templateBytes = studentService.generateImportTemplate();
        ByteArrayResource resource = new ByteArrayResource(templateBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"template_import_eleves.xlsx\"")
                .body(resource);
    }

    // ===================== ELV-012: Fiche eleve PDF =====================

    @GetMapping("/{id}/fiche-pdf")
    @PreAuthorize("hasAuthority('READ_STUDENTS')")
    public ResponseEntity<String> fichePdf(@PathVariable UUID id) {
        String html = studentService.generateFicheHtml(id);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
