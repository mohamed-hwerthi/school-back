package com.schoolSys.schooolSys.cantine;

import java.util.UUID;

import com.schoolSys.schooolSys.cantine.dto.AbonnementCantineDTO;
import com.schoolSys.schooolSys.cantine.dto.CreateAbonnementRequest;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cantine/abonnements")
@RequiredArgsConstructor
public class AbonnementCantineController {

    private final AbonnementCantineService abonnementService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<List<AbonnementCantineDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(abonnementService.getAll()));
    }

    @GetMapping("/actifs")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<List<AbonnementCantineDTO>>> getActifs() {
        return ResponseEntity.ok(ApiResponse.ok(abonnementService.getActifs()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<AbonnementCantineDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(abonnementService.getById(id)));
    }

    @GetMapping("/eleve/{eleveId}")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<List<AbonnementCantineDTO>>> getByEleve(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(abonnementService.getByEleve(eleveId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<AbonnementCantineDTO>> create(@Valid @RequestBody CreateAbonnementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(abonnementService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<AbonnementCantineDTO>> update(@PathVariable UUID id, @Valid @RequestBody CreateAbonnementRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(abonnementService.update(id, request)));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<AbonnementCantineDTO>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(abonnementService.deactivate(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        abonnementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
