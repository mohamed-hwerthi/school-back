package com.schoolSys.schooolSys.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export-comptable")
@RequiredArgsConstructor
public class ExportComptableController {

    private final ExportComptableService exportComptableService;

    @GetMapping("/csv")
    @PreAuthorize("hasAuthority('READ_FINANCE')")
    public ResponseEntity<String> exportCSV(@RequestParam String anneeScolaire) {
        String csv = exportComptableService.exportCSV(anneeScolaire);
        String filename = "export-comptable-" + anneeScolaire + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
