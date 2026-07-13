package com.schoolSys.schooolSys.finance;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.finance.dto.TresorerieDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tresorerie")
@RequiredArgsConstructor
public class TresorerieController {

    private final TresorerieService tresorerieService;

    @GetMapping
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<ApiResponse<TresorerieDTO>> getTresorerie(
            @RequestParam String anneeScolaire) {
        return ResponseEntity.ok(ApiResponse.ok(tresorerieService.getTresorerie(anneeScolaire)));
    }
}
