package com.schoolSys.schooolSys.rh;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.rh.dto.CreateFichePaieRequest;
import com.schoolSys.schooolSys.rh.dto.FichePaieDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rh/paie")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MANAGE_RH')")
public class FichePaieController {

    private final FichePaieService fichePaieService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FichePaieDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(fichePaieService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FichePaieDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(fichePaieService.getById(id)));
    }

    @GetMapping("/employe/{employeId}")
    public ResponseEntity<ApiResponse<List<FichePaieDTO>>> getByEmploye(
            @PathVariable UUID employeId) {
        return ResponseEntity.ok(ApiResponse.ok(fichePaieService.getByEmploye(employeId)));
    }

    @GetMapping("/mois")
    public ResponseEntity<ApiResponse<List<FichePaieDTO>>> getByMois(
            @RequestParam Integer mois, @RequestParam Integer annee) {
        return ResponseEntity.ok(ApiResponse.ok(fichePaieService.getByMoisAndAnnee(mois, annee)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FichePaieDTO>> create(
            @Valid @RequestBody CreateFichePaieRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(fichePaieService.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FichePaieDTO>> update(
            @PathVariable UUID id, @Valid @RequestBody CreateFichePaieRequest dto) {
        return ResponseEntity.ok(ApiResponse.ok(fichePaieService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        fichePaieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
