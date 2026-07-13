package com.schoolSys.schooolSys.cantine;

import java.util.UUID;

import com.schoolSys.schooolSys.cantine.dto.CreateMenuRequest;
import com.schoolSys.schooolSys.cantine.dto.MenuDTO;
import com.schoolSys.schooolSys.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cantine/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final CantineFileService cantineFileService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<List<MenuDTO>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<MenuDTO>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getById(id)));
    }

    @GetMapping("/semaine/{semaine}")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<List<MenuDTO>>> getMenuSemaine(@PathVariable Integer semaine) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getMenuSemaine(semaine)));
    }

    @GetMapping("/range")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<List<MenuDTO>>> getMenuByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getMenuByDateRange(start, end)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<MenuDTO>> create(@Valid @RequestBody CreateMenuRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(menuService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<MenuDTO>> update(@PathVariable UUID id, @Valid @RequestBody CreateMenuRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('MANAGE_CANTINE')")
    public ResponseEntity<ApiResponse<MenuDTO>> uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        String url = cantineFileService.upload(file);
        return ResponseEntity.ok(ApiResponse.ok(menuService.setImage(id, url)));
    }
}
