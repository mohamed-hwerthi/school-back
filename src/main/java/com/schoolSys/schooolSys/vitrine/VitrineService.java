package com.schoolSys.schooolSys.vitrine;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.settings.SchoolSettingsRepository;
import com.schoolSys.schooolSys.vitrine.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VitrineService {

    private final VitrineConfigRepository configRepository;
    private final VitrinePageRepository pageRepository;
    private final VitrineSectionRepository sectionRepository;
    private final VitrineGalleryRepository galleryRepository;
    private final VitrineAnnouncementRepository announcementRepository;
    private final VitrineContactRepository contactRepository;
    private final VitrinePageViewRepository pageViewRepository;
    private final SchoolSettingsRepository schoolSettingsRepository;
    private final VitrineMapper mapper;

    // Placeholder seeded by V28 — we override it with the real school name
    // until the admin sets a custom display name.
    private static final String DEFAULT_PLACEHOLDER_NAME = "Notre École";

    /** If the display name is empty or still the seed placeholder, fall back
     *  to the tenant's actual school name from school_settings. */
    private VitrineConfigDTO withResolvedDisplayName(VitrineConfigDTO dto) {
        String current = dto.getSchoolDisplayName();
        if (current == null || current.isBlank() || DEFAULT_PLACEHOLDER_NAME.equals(current)) {
            schoolSettingsRepository.findAll().stream().findFirst()
                    .map(s -> s.getSchoolName())
                    .filter(n -> n != null && !n.isBlank())
                    .ifPresent(dto::setSchoolDisplayName);
        }
        return dto;
    }

    // ======================== CONFIG ========================

    public VitrineConfigDTO getConfig() {
        VitrineConfig config = configRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Vitrine config not found"));
        return withResolvedDisplayName(mapper.toConfigDTO(config));
    }

    @Transactional
    public VitrineConfigDTO updateConfig(VitrineConfigDTO dto) {
        VitrineConfig config = configRepository.findFirstByOrderByIdAsc()
                .orElse(VitrineConfig.builder().build());
        mapper.updateConfig(dto, config);
        return mapper.toConfigDTO(configRepository.save(config));
    }

    // ======================== PAGES ========================

    @Transactional(readOnly = true)
    public List<VitrinePageDTO> getAllPages() {
        return mapper.toPageDTOList(pageRepository.findAllByOrderByDisplayOrderAsc());
    }

    @Transactional(readOnly = true)
    public List<VitrinePageDTO> getVisiblePages() {
        return mapper.toPageDTOList(pageRepository.findByVisibleTrueOrderByDisplayOrderAsc());
    }

    @Transactional(readOnly = true)
    public VitrinePageDTO getPageBySlug(String slug) {
        VitrinePage page = pageRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Vitrine page not found: " + slug));
        return mapper.toPageDTO(page);
    }

    @Transactional
    public VitrinePageDTO createPage(VitrinePageDTO dto) {
        VitrinePage page = mapper.toPageEntity(dto);
        return mapper.toPageDTO(pageRepository.save(page));
    }

    @Transactional
    public VitrinePageDTO updatePage(UUID id, VitrinePageDTO dto) {
        VitrinePage page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VitrinePage", id));
        page.setTitle(dto.getTitle());
        page.setSlug(dto.getSlug());
        page.setDisplayOrder(dto.getDisplayOrder());
        page.setVisible(dto.isVisible());
        return mapper.toPageDTO(pageRepository.save(page));
    }

    @Transactional
    public void deletePage(UUID id) {
        pageRepository.deleteById(id);
    }

    // ======================== SECTIONS ========================

    public List<VitrineSectionDTO> getSectionsByPage(UUID pageId) {
        return mapper.toSectionDTOList(sectionRepository.findByPageIdOrderByDisplayOrderAsc(pageId));
    }

    @Transactional
    public VitrineSectionDTO createSection(UUID pageId, VitrineSectionDTO dto) {
        VitrinePage page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("VitrinePage", pageId));
        VitrineSection section = mapper.toSectionEntity(dto);
        section.setPage(page);
        return mapper.toSectionDTO(sectionRepository.save(section));
    }

    @Transactional
    public VitrineSectionDTO updateSection(UUID id, VitrineSectionDTO dto) {
        VitrineSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VitrineSection", id));
        section.setSectionType(dto.getSectionType());
        section.setTitle(dto.getTitle());
        section.setContent(dto.getContent());
        section.setDisplayOrder(dto.getDisplayOrder());
        section.setVisible(dto.isVisible());
        return mapper.toSectionDTO(sectionRepository.save(section));
    }

    @Transactional
    public void deleteSection(UUID id) {
        sectionRepository.deleteById(id);
    }

    // ======================== GALLERY ========================

    public List<VitrineGalleryDTO> getAllGallery() {
        return mapper.toGalleryDTOList(galleryRepository.findAllByOrderByDisplayOrderAsc());
    }

    @Transactional
    public VitrineGalleryDTO createGalleryItem(VitrineGalleryDTO dto) {
        VitrineGallery item = mapper.toGalleryEntity(dto);
        return mapper.toGalleryDTO(galleryRepository.save(item));
    }

    @Transactional
    public void deleteGalleryItem(UUID id) {
        galleryRepository.deleteById(id);
    }

    // ======================== ANNOUNCEMENTS ========================

    public List<VitrineAnnouncementDTO> getAllAnnouncements() {
        return mapper.toAnnouncementDTOList(announcementRepository.findAllByOrderByPinnedDescCreatedAtDesc());
    }

    public List<VitrineAnnouncementDTO> getActiveAnnouncements() {
        return mapper.toAnnouncementDTOList(
                announcementRepository.findByPublishedTrueAndExpiresAtIsNullOrExpiresAtAfterOrderByPinnedDescCreatedAtDesc(LocalDateTime.now()));
    }

    @Transactional
    public VitrineAnnouncementDTO createAnnouncement(VitrineAnnouncementDTO dto) {
        VitrineAnnouncement ann = mapper.toAnnouncementEntity(dto);
        return mapper.toAnnouncementDTO(announcementRepository.save(ann));
    }

    @Transactional
    public VitrineAnnouncementDTO updateAnnouncement(UUID id, VitrineAnnouncementDTO dto) {
        VitrineAnnouncement ann = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VitrineAnnouncement", id));
        ann.setTitle(dto.getTitle());
        ann.setBody(dto.getBody());
        ann.setPinned(dto.isPinned());
        ann.setPublished(dto.isPublished());
        ann.setExpiresAt(dto.getExpiresAt());
        return mapper.toAnnouncementDTO(announcementRepository.save(ann));
    }

    @Transactional
    public void deleteAnnouncement(UUID id) {
        announcementRepository.deleteById(id);
    }

    // ======================== ANALYTICS ========================

    @Transactional
    public void trackPageView(String pageSlug, String ip, String userAgent, String referer) {
        String hash = hashIp(ip);
        VitrinePageView view = VitrinePageView.builder()
                .pageSlug(pageSlug)
                .visitorHash(hash)
                .userAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent)
                .referer(referer != null && referer.length() > 500 ? referer.substring(0, 500) : referer)
                .build();
        pageViewRepository.save(view);
    }

    public VitrineAnalyticsDTO getAnalytics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        long total = pageViewRepository.count();
        long last7 = pageViewRepository.countByCreatedAtAfter(sevenDaysAgo);
        long last30 = pageViewRepository.countByCreatedAtAfter(thirtyDaysAgo);

        List<Map<String, Object>> byPage = pageViewRepository.countByPageSlugSince(thirtyDaysAgo)
                .stream()
                .map(row -> Map.<String, Object>of("page", row[0], "views", row[1]))
                .toList();

        List<Map<String, Object>> byDay = pageViewRepository.countByDaySince(thirtyDaysAgo)
                .stream()
                .map(row -> Map.<String, Object>of("date", row[0].toString(), "views", row[1]))
                .toList();

        return VitrineAnalyticsDTO.builder()
                .totalViews(total)
                .viewsLast7Days(last7)
                .viewsLast30Days(last30)
                .viewsByPage(byPage)
                .viewsByDay(byDay)
                .build();
    }

    private String hashIp(String ip) {
        if (ip == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(ip.getBytes());
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            return null;
        }
    }

    // ======================== CONTACTS ========================

    public List<VitrineContactDTO> getAllContacts() {
        return mapper.toContactDTOList(contactRepository.findAllByOrderByCreatedAtDesc());
    }

    public long getUnreadCount() {
        return contactRepository.countByIsReadFalse();
    }

    @Transactional
    public void markAsRead(UUID id) {
        VitrineContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VitrineContact", id));
        contact.setRead(true);
        contactRepository.save(contact);
    }

    @Transactional
    public void replyToContact(UUID id, String replyText) {
        VitrineContact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VitrineContact", id));
        contact.setReplied(true);
        contact.setReplyText(replyText);
        contact.setRepliedAt(LocalDateTime.now());
        contactRepository.save(contact);
    }

    @Transactional
    public void submitContact(VitrineContactRequestDTO dto) {
        VitrineContact contact = mapper.toContactEntity(dto);
        contactRepository.save(contact);
    }

    // ======================== PUBLIC AGGREGATION ========================

    /**
     * Returns all public vitrine data in a single DTO for fast initial load.
     */
    @Transactional(readOnly = true)
    public VitrinePublicDataDTO getPublicData(String slug) {
        VitrineConfigDTO config = getConfig();
        List<VitrinePageDTO> pages = getVisiblePages();
        List<VitrineAnnouncementDTO> announcements = getActiveAnnouncements();
        List<VitrineGalleryDTO> gallery = getAllGallery();

        return VitrinePublicDataDTO.builder()
                .slug(slug)
                .config(config)
                .pages(pages)
                .announcements(announcements)
                .gallery(gallery)
                .build();
    }
}
