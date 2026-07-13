package com.schoolSys.schooolSys.notification;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.notification.dto.AnnonceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/annonces")
@RequiredArgsConstructor
public class AnnonceController {

    private final AnnonceService annonceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnnonceDTO>>> getAll(
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        List<AnnonceDTO> annonces = activeOnly ? annonceService.getActive() : annonceService.getAll();
        return ResponseEntity.ok(ApiResponse.ok(annonces));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnonceDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(annonceService.getById(id)));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<AnnonceDTO>>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(ApiResponse.ok(annonceService.getByType(type)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<ApiResponse<AnnonceDTO>> create(@RequestBody AnnonceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(annonceService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<ApiResponse<AnnonceDTO>> update(
            @PathVariable UUID id, @RequestBody AnnonceDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(annonceService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        annonceService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
