package com.schoolSys.schooolSys.inscription;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.inscription.dto.CreateInscriptionRequest;
import com.schoolSys.schooolSys.inscription.dto.InscriptionDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public endpoints for inscription (no authentication required).
 * Mapped under /api/public/ which is whitelisted in SecurityConfig.
 */
@RestController
@RequestMapping("/api/public/inscriptions")
@RequiredArgsConstructor
public class InscriptionPublicController {

    private final InscriptionService inscriptionService;

    /**
     * POST /api/public/inscriptions — Submit a new inscription (public form).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InscriptionDTO>> create(
            @Valid @RequestBody CreateInscriptionRequest request) {
        InscriptionDTO created = inscriptionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created));
    }

    /**
     * GET /api/public/inscriptions/numero/{numeroDossier} — Check inscription status by dossier number.
     */
    @GetMapping("/numero/{numeroDossier}")
    public ResponseEntity<ApiResponse<InscriptionDTO>> getByNumeroDossier(
            @PathVariable String numeroDossier) {
        return ResponseEntity.ok(ApiResponse.ok(inscriptionService.findByNumeroDossier(numeroDossier)));
    }
}
