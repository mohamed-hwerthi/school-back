package com.schoolSys.schooolSys.course;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.course.dto.CourseRequestDTO;
import com.schoolSys.schooolSys.course.dto.CourseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for course CRUD operations.
 * <p>
 * Requires the {@code X-Tenant-ID} header to identify the school schema.
 * </p>
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /** Lists all courses in the current school. */
    @GetMapping
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<List<CourseResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(courseService.findAll()));
    }

    /** Gets a course by ID. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_NOTES')")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.findById(id)));
    }

    /** Creates a new course. */
    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> create(@RequestBody CourseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(courseService.create(dto)));
    }

    /** Updates an existing course. */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> update(@PathVariable UUID id,
                                                                  @RequestBody CourseRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(courseService.update(id, dto)));
    }

    /** Deletes a course. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_NOTES')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
