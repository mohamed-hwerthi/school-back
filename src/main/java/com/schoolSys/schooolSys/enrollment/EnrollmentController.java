package com.schoolSys.schooolSys.enrollment;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.enrollment.dto.EnrollmentRequestDTO;
import com.schoolSys.schooolSys.enrollment.dto.EnrollmentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for enrollment CRUD operations.
 * <p>
 * Requires the {@code X-Tenant-ID} header to identify the school schema.
 * </p>
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /** Lists all enrollments in the current school. */
    @GetMapping
    @PreAuthorize("hasAuthority('READ_STUDENTS')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(enrollmentService.findAll()));
    }

    /** Gets an enrollment by ID. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_STUDENTS')")
    public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(enrollmentService.findById(id)));
    }

    /** Creates a new enrollment. */
    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> create(@RequestBody EnrollmentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(enrollmentService.create(dto)));
    }

    /** Updates an existing enrollment. */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_STUDENTS')")
    public ResponseEntity<ApiResponse<EnrollmentResponseDTO>> update(@PathVariable UUID id,
                                                                      @RequestBody EnrollmentRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(enrollmentService.update(id, dto)));
    }

    /** Deletes an enrollment. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_STUDENTS')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
