package com.schoolSys.schooolSys.student;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.student.dto.BulkPassageDTO;
import com.schoolSys.schooolSys.student.dto.PassageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passages")
@RequiredArgsConstructor
public class PassageController {

    private final PassageService passageService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<List<PassageDTO>>> getByAnneeScolaire(
            @RequestParam(required = false) String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(passageService.findByAnneeScolaire(anneeScolaire)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<PassageDTO>> create(@RequestBody PassageDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(passageService.create(dto)));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<List<PassageDTO>>> bulkCreate(@RequestBody BulkPassageDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(passageService.bulkCreate(dto.getPassages())));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('MANAGE_ANNEE_SCOLAIRE')")
    public ResponseEntity<ApiResponse<List<PassageDTO>>> getByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(ApiResponse.ok(passageService.findByStudentId(studentId)));
    }
}
