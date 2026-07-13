package com.schoolSys.schooolSys.vitrine;

import java.util.UUID;

import com.schoolSys.schooolSys.common.dto.ApiResponse;
import com.schoolSys.schooolSys.vitrine.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Admin API for managing the school's vitrine website.
 * Requires authentication and uses the X-Tenant-ID header.
 */
@RestController
@RequestMapping("/api/vitrine")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DIRECTEUR')")
public class VitrineAdminController {

    private final VitrineService vitrineService;
    private final VitrineFileService fileService;

    // ======================== ANALYTICS ========================

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<VitrineAnalyticsDTO>> getAnalytics() {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.getAnalytics()));
    }

    // ======================== UPLOAD ========================

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(Map.of("url", url)));
    }

    // ======================== CONFIG ========================

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<VitrineConfigDTO>> getConfig() {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.getConfig()));
    }

    @PutMapping("/config")
    public ResponseEntity<ApiResponse<VitrineConfigDTO>> updateConfig(@RequestBody VitrineConfigDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.updateConfig(dto)));
    }

    // ======================== PAGES ========================

    @GetMapping("/pages")
    public ResponseEntity<ApiResponse<List<VitrinePageDTO>>> getPages() {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.getAllPages()));
    }

    @GetMapping("/pages/{id}")
    public ResponseEntity<ApiResponse<VitrinePageDTO>> getPage(@PathVariable UUID id) {
        // Get by ID for admin (not by slug)
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.getPageBySlug(
                vitrineService.getAllPages().stream()
                        .filter(p -> p.getId().equals(id))
                        .findFirst()
                        .orElseThrow()
                        .getSlug()
        )));
    }

    @PostMapping("/pages")
    public ResponseEntity<ApiResponse<VitrinePageDTO>> createPage(@RequestBody VitrinePageDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(vitrineService.createPage(dto)));
    }

    @PutMapping("/pages/{id}")
    public ResponseEntity<ApiResponse<VitrinePageDTO>> updatePage(@PathVariable UUID id,
                                                                    @RequestBody VitrinePageDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.updatePage(id, dto)));
    }

    @DeleteMapping("/pages/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable UUID id) {
        vitrineService.deletePage(id);
        return ResponseEntity.noContent().build();
    }

    // ======================== SECTIONS ========================

    @GetMapping("/pages/{pageId}/sections")
    public ResponseEntity<ApiResponse<List<VitrineSectionDTO>>> getSections(@PathVariable UUID pageId) {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.getSectionsByPage(pageId)));
    }

    @PostMapping("/pages/{pageId}/sections")
    public ResponseEntity<ApiResponse<VitrineSectionDTO>> createSection(@PathVariable UUID pageId,
                                                                         @RequestBody VitrineSectionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(vitrineService.createSection(pageId, dto)));
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<ApiResponse<VitrineSectionDTO>> updateSection(@PathVariable UUID id,
                                                                         @RequestBody VitrineSectionDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.updateSection(id, dto)));
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable UUID id) {
        vitrineService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }

    // ======================== GALLERY ========================

    @GetMapping("/gallery")
    public ResponseEntity<ApiResponse<List<VitrineGalleryDTO>>> getGallery() {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.getAllGallery()));
    }

    @PostMapping("/gallery")
    public ResponseEntity<ApiResponse<VitrineGalleryDTO>> addGalleryItem(@RequestBody VitrineGalleryDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(vitrineService.createGalleryItem(dto)));
    }

    @DeleteMapping("/gallery/{id}")
    public ResponseEntity<Void> deleteGalleryItem(@PathVariable UUID id) {
        vitrineService.deleteGalleryItem(id);
        return ResponseEntity.noContent().build();
    }

    // ======================== ANNOUNCEMENTS ========================

    @GetMapping("/announcements")
    public ResponseEntity<ApiResponse<List<VitrineAnnouncementDTO>>> getAnnouncements() {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.getAllAnnouncements()));
    }

    @PostMapping("/announcements")
    public ResponseEntity<ApiResponse<VitrineAnnouncementDTO>> createAnnouncement(@RequestBody VitrineAnnouncementDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(vitrineService.createAnnouncement(dto)));
    }

    @PutMapping("/announcements/{id}")
    public ResponseEntity<ApiResponse<VitrineAnnouncementDTO>> updateAnnouncement(@PathVariable UUID id,
                                                                                    @RequestBody VitrineAnnouncementDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.updateAnnouncement(id, dto)));
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable UUID id) {
        vitrineService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }

    // ======================== CONTACTS ========================

    @GetMapping("/contacts")
    public ResponseEntity<ApiResponse<List<VitrineContactDTO>>> getContacts() {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.getAllContacts()));
    }

    @GetMapping("/contacts/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.ok(vitrineService.getUnreadCount()));
    }

    @PutMapping("/contacts/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        vitrineService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/contacts/{id}/reply")
    public ResponseEntity<ApiResponse<String>> replyToContact(
            @PathVariable UUID id,
            @Valid @RequestBody VitrineReplyDTO dto) {
        vitrineService.replyToContact(id, dto.getReplyText());
        return ResponseEntity.ok(ApiResponse.ok("Réponse envoyée"));
    }
}
