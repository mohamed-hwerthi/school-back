package com.schoolSys.schooolSys.teacher;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.teacher.dto.TeacherEvaluationDTO;
import com.schoolSys.schooolSys.teacher.dto.TeacherEvaluationStatsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher-evaluations")
@RequiredArgsConstructor
public class TeacherEvaluationController {

    private final TeacherEvaluationService evaluationService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<ApiResponse<List<TeacherEvaluationDTO>>> getAll(
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(evaluationService.findAll(teacherId, anneeScolaire)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<ApiResponse<TeacherEvaluationDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(evaluationService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<ApiResponse<TeacherEvaluationDTO>> create(
            @RequestBody TeacherEvaluationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(evaluationService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<ApiResponse<TeacherEvaluationDTO>> update(
            @PathVariable UUID id,
            @RequestBody TeacherEvaluationDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(evaluationService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        evaluationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/{teacherId}")
    @PreAuthorize("hasAuthority('MANAGE_TEACHERS')")
    public ResponseEntity<ApiResponse<TeacherEvaluationStatsDTO>> getStats(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(ApiResponse.ok(evaluationService.getStats(teacherId)));
    }
}
