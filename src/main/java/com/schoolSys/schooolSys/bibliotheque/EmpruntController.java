package com.schoolSys.schooolSys.bibliotheque;

import java.util.UUID;

import com.schoolSys.schooolSys.bibliotheque.dto.BibliothequeStatsDTO;
import com.schoolSys.schooolSys.bibliotheque.dto.CreateEmpruntRequest;
import com.schoolSys.schooolSys.bibliotheque.dto.EmpruntDTO;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emprunts")
@RequiredArgsConstructor
public class EmpruntController {

    private final EmpruntService empruntService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<List<EmpruntDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(empruntService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<EmpruntDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(empruntService.findById(id)));
    }

    @GetMapping("/eleve/{eleveId}")
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<List<EmpruntDTO>>> getByEleve(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(empruntService.findByEleve(eleveId)));
    }

    @GetMapping("/livre/{livreId}")
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<List<EmpruntDTO>>> getByLivre(@PathVariable UUID livreId) {
        return ResponseEntity.ok(ApiResponse.ok(empruntService.findByLivre(livreId)));
    }

    @GetMapping("/en-retard")
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<List<EmpruntDTO>>> getEnRetard() {
        return ResponseEntity.ok(ApiResponse.ok(empruntService.findEnRetard()));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<BibliothequeStatsDTO>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(empruntService.getStats()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<EmpruntDTO>> create(@Valid @RequestBody CreateEmpruntRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(empruntService.create(request)));
    }

    @PutMapping("/{id}/retour")
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<EmpruntDTO>> retourner(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(empruntService.retourner(id)));
    }
}
