package com.schoolSys.schooolSys.teacher;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.teacher.dto.TeacherRequestDTO;
import com.schoolSys.schooolSys.teacher.dto.TeacherResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_TEACHERS')")
    public ResponseEntity<ApiResponse<List<TeacherResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.findAll()));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('READ_TEACHERS')")
    public ResponseEntity<ApiResponse<PagedResponse<TeacherResponseDTO>>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String statut) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.findPage(search, statut, page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_TEACHERS')")
    public ResponseEntity<ApiResponse<TeacherResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_TEACHERS')")
    public ResponseEntity<ApiResponse<TeacherResponseDTO>> create(
            @Valid @RequestBody TeacherRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(teacherService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_TEACHERS')")
    public ResponseEntity<ApiResponse<TeacherResponseDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TeacherRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_TEACHERS')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('WRITE_TEACHERS')")
    public ResponseEntity<ApiResponse<List<TeacherResponseDTO>>> importBulk(
            @RequestBody List<@Valid TeacherRequestDTO> dtos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(teacherService.importBulk(dtos)));
    }
}
