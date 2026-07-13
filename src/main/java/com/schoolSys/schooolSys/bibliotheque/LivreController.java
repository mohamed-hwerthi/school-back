package com.schoolSys.schooolSys.bibliotheque;

import java.util.UUID;

import com.schoolSys.schooolSys.bibliotheque.dto.CreateLivreRequest;
import com.schoolSys.schooolSys.bibliotheque.dto.LivreDTO;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.dto.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livres")
@RequiredArgsConstructor
public class LivreController {

    private final LivreService livreService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<PagedResponse<LivreDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categorie,
            @RequestParam(defaultValue = "titre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<LivreDTO> result = livreService.findAll(search, categorie, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<LivreDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(livreService.findById(id)));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(livreService.findAllCategories()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<LivreDTO>> create(@Valid @RequestBody CreateLivreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(livreService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<ApiResponse<LivreDTO>> update(@PathVariable UUID id,
                                                         @Valid @RequestBody CreateLivreRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(livreService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_BIBLIOTHEQUE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        livreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
