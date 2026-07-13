package com.schoolSys.schooolSys.vitrine;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.common.multitenancy.TenantContext;
import com.schoolSys.schooolSys.tenant.Tenant;
import com.schoolSys.schooolSys.tenant.TenantService;
import com.schoolSys.schooolSys.vitrine.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public-facing API for school vitrine websites.
 * No authentication required — accessible by visitors.
 * Tenant is resolved from the URL slug, not the X-Tenant-ID header.
 */
@RestController
@RequestMapping("/api/public/vitrine/{slug}")
@RequiredArgsConstructor
public class PublicVitrineController {

    private final TenantService tenantService;
    private final VitrineService vitrineService;

    /**
     * Returns all vitrine data in a single request (config, pages with sections, announcements, gallery).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<VitrinePublicDataDTO>> getFullVitrine(
            @PathVariable String slug,
            @RequestParam(required = false) String preview,
            HttpServletRequest request) {
        setTenantFromSlug(slug);
        try {
            VitrinePublicDataDTO data = vitrineService.getPublicData(slug);
            // Allow unpublished access in preview mode
            if (!data.getConfig().isPublished() && !"true".equals(preview)) {
                return ResponseEntity.notFound().build();
            }
            // Track page view (async-friendly, fire and forget)
            vitrineService.trackPageView("home", request.getRemoteAddr(),
                    request.getHeader("User-Agent"), request.getHeader("Referer"));
            return ResponseEntity.ok(ApiResponse.ok(data));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/pages")
    public ResponseEntity<ApiResponse<List<VitrinePageDTO>>> getPages(@PathVariable String slug) {
        setTenantFromSlug(slug);
        try {
            return ResponseEntity.ok(ApiResponse.ok(vitrineService.getVisiblePages()));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/pages/{pageSlug}")
    public ResponseEntity<ApiResponse<VitrinePageDTO>> getPage(@PathVariable String slug,
                                                                @PathVariable String pageSlug,
                                                                HttpServletRequest request) {
        setTenantFromSlug(slug);
        try {
            vitrineService.trackPageView(pageSlug, request.getRemoteAddr(),
                    request.getHeader("User-Agent"), request.getHeader("Referer"));
            return ResponseEntity.ok(ApiResponse.ok(vitrineService.getPageBySlug(pageSlug)));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/announcements")
    public ResponseEntity<ApiResponse<List<VitrineAnnouncementDTO>>> getAnnouncements(@PathVariable String slug) {
        setTenantFromSlug(slug);
        try {
            return ResponseEntity.ok(ApiResponse.ok(vitrineService.getActiveAnnouncements()));
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/gallery")
    public ResponseEntity<ApiResponse<List<VitrineGalleryDTO>>> getGallery(@PathVariable String slug) {
        setTenantFromSlug(slug);
        try {
            return ResponseEntity.ok(ApiResponse.ok(vitrineService.getAllGallery()));
        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/contact")
    public ResponseEntity<ApiResponse<String>> submitContact(
            @PathVariable String slug,
            @Valid @RequestBody VitrineContactRequestDTO dto) {
        setTenantFromSlug(slug);
        try {
            vitrineService.submitContact(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok("Message envoyé avec succès"));
        } finally {
            TenantContext.clear();
        }
    }

    private void setTenantFromSlug(String slug) {
        Tenant tenant = tenantService.findBySlug(slug);
        TenantContext.setCurrentTenant(tenant.getSchemaName());
    }
}
