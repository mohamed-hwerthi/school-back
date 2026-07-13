package com.schoolSys.schooolSys.classroom;

import java.util.UUID;

import com.schoolSys.schooolSys.classroom.dto.ClassroomRequestDTO;
import com.schoolSys.schooolSys.classroom.dto.ClassroomResponseDTO;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for classroom CRUD operations.
 * <p>
 * Requires the {@code X-Tenant-ID} header to identify the school schema.
 * </p>
 */
@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    /** Lists all classrooms in the current school. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassroomResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(classroomService.findAll()));
    }

    /** Gets a classroom by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(classroomService.findById(id)));
    }

    /** Creates a new classroom. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<ApiResponse<ClassroomResponseDTO>> create(@RequestBody ClassroomRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(classroomService.create(dto)));
    }

    /** Updates an existing classroom. */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<ApiResponse<ClassroomResponseDTO>> update(@PathVariable UUID id,
                                                                     @RequestBody ClassroomRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(classroomService.update(id, dto)));
    }

    /** Deletes a classroom. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','DIRECTEUR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        classroomService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
