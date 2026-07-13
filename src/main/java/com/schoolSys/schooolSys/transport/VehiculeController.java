package com.schoolSys.schooolSys.transport;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.transport.dto.VehiculeDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<List<VehiculeDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(vehiculeService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<VehiculeDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(vehiculeService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<VehiculeDTO>> create(@Valid @RequestBody VehiculeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(vehiculeService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<VehiculeDTO>> update(@PathVariable UUID id, @Valid @RequestBody VehiculeDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(vehiculeService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        vehiculeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
