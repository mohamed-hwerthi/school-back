package com.schoolSys.schooolSys.transport;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.transport.dto.ArretDTO;
import com.schoolSys.schooolSys.transport.dto.CircuitDTO;
import com.schoolSys.schooolSys.transport.dto.CreateCircuitRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/circuits")
@RequiredArgsConstructor
public class CircuitController {

    private final CircuitService circuitService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<List<CircuitDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(circuitService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<CircuitDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(circuitService.getById(id)));
    }

    @GetMapping("/{id}/arrets")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<List<ArretDTO>>> getArrets(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(circuitService.getArretsByCircuit(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<CircuitDTO>> create(@Valid @RequestBody CreateCircuitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(circuitService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<ApiResponse<CircuitDTO>> update(@PathVariable UUID id, @Valid @RequestBody CreateCircuitRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(circuitService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_TRANSPORT')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        circuitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
