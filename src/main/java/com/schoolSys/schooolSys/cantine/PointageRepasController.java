package com.schoolSys.schooolSys.cantine;

import java.util.UUID;

import com.schoolSys.schooolSys.cantine.dto.CantineStatsDTO;
import com.schoolSys.schooolSys.cantine.dto.PointageBatchRequest;
import com.schoolSys.schooolSys.cantine.dto.PointageRepasDTO;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
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
@RequestMapping("/api/cantine/pointages")
@RequiredArgsConstructor
public class PointageRepasController {

    private final PointageRepasService pointageService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<List<PointageRepasDTO>>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String typeRepas) {
        if (typeRepas != null && !typeRepas.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(pointageService.getByDateAndType(date, typeRepas)));
        }
        return ResponseEntity.ok(ApiResponse.ok(pointageService.getByDate(date)));
    }

    @GetMapping("/eleve/{eleveId}")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<List<PointageRepasDTO>>> getByEleve(@PathVariable UUID eleveId) {
        return ResponseEntity.ok(ApiResponse.ok(pointageService.getByEleve(eleveId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<List<PointageRepasDTO>>> pointer(@Valid @RequestBody PointageBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(pointageService.pointer(request)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<CantineStatsDTO>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(pointageService.getStats()));
    }
}
