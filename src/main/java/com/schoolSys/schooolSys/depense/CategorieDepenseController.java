package com.schoolSys.schooolSys.depense;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.depense.dto.CategorieDepenseRequestDTO;
import com.schoolSys.schooolSys.depense.dto.CategorieDepenseResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories-depense")
@RequiredArgsConstructor
public class CategorieDepenseController {

    private final CategorieDepenseService categorieService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<CategorieDepenseResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(categorieService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<CategorieDepenseResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(categorieService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<CategorieDepenseResponseDTO>> create(
            @Valid @RequestBody CategorieDepenseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(categorieService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<CategorieDepenseResponseDTO>> update(
            @PathVariable UUID id, @Valid @RequestBody CategorieDepenseRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(categorieService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categorieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
