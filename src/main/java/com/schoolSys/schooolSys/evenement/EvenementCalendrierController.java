package com.schoolSys.schooolSys.evenement;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.evenement.dto.EvenementCalendrierRequestDTO;
import com.schoolSys.schooolSys.evenement.dto.EvenementCalendrierResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/evenements-calendrier")
@RequiredArgsConstructor
public class EvenementCalendrierController {

    private final EvenementCalendrierService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EvenementCalendrierResponseDTO>>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(ApiResponse.ok(service.findAll(from, to, type)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EvenementCalendrierResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<ApiResponse<EvenementCalendrierResponseDTO>> create(
            @Valid @RequestBody EvenementCalendrierRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<ApiResponse<EvenementCalendrierResponseDTO>> update(
            @PathVariable UUID id, @Valid @RequestBody EvenementCalendrierRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_COMMUNICATION')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
