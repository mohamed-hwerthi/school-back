package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.finance.dto.BourseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bourses")
@RequiredArgsConstructor
public class BourseController {

    private final BourseService bourseService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<BourseDTO>>> getAll(
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(bourseService.findAll(anneeScolaire)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<BourseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(bourseService.findById(id)));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<BourseDTO>>> getByStudent(
            @PathVariable UUID studentId,
            @RequestParam(required = false) String anneeScolaire) {
        List<BourseDTO> result;
        if (anneeScolaire != null && !anneeScolaire.isBlank()) {
            result = bourseService.findByStudentIdAndAnneeScolaire(studentId, anneeScolaire);
        } else {
            result = bourseService.findByStudentId(studentId);
        }
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<BourseDTO>> create(@Valid @RequestBody BourseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(bourseService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<BourseDTO>> update(
            @PathVariable UUID id, @Valid @RequestBody BourseDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(bourseService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bourseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
