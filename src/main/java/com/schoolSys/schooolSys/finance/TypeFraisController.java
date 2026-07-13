package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.finance.dto.TypeFraisRequestDTO;
import com.schoolSys.schooolSys.finance.dto.TypeFraisResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/types-frais")
@RequiredArgsConstructor
public class TypeFraisController {

    private final TypeFraisService typeFraisService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<TypeFraisResponseDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(typeFraisService.findAll()));
    }

    @GetMapping("/actifs")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<List<TypeFraisResponseDTO>>> getAllActifs() {
        return ResponseEntity.ok(ApiResponse.ok(typeFraisService.findAllActifs()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<TypeFraisResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(typeFraisService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<TypeFraisResponseDTO>> create(
            @Valid @RequestBody TypeFraisRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(typeFraisService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<TypeFraisResponseDTO>> update(
            @PathVariable UUID id, @Valid @RequestBody TypeFraisRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(typeFraisService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        typeFraisService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
