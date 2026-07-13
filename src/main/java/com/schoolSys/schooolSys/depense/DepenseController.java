package com.schoolSys.schooolSys.depense;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import com.schoolSys.schooolSys.depense.dto.DepenseRequestDTO;
import com.schoolSys.schooolSys.depense.dto.DepenseResponseDTO;
import com.schoolSys.schooolSys.depense.dto.DepenseStatsDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/depenses")
@RequiredArgsConstructor
public class DepenseController {

    private final DepenseService depenseService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<PagedResponse<DepenseResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String anneeScolaire,
            @RequestParam(required = false) UUID categorieId,
            @RequestParam(required = false) Depense.ModePaiement modePaiement,
            @RequestParam(required = false) Boolean recurrente,
            @RequestParam(defaultValue = "dateDepense") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<DepenseResponseDTO> result = depenseService.findAll(
                search, anneeScolaire, categorieId, modePaiement, recurrente, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<DepenseResponseDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(depenseService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<DepenseResponseDTO>> create(
            @Valid @RequestBody DepenseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(depenseService.create(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<ApiResponse<DepenseResponseDTO>> update(
            @PathVariable UUID id, @Valid @RequestBody DepenseRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(depenseService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_FINANCE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        depenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<DepenseStatsDTO>> getStats(
            @RequestParam String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(depenseService.getStats(anneeScolaire)));
    }
}
