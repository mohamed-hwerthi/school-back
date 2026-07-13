package com.schoolSys.schooolSys.vitrine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated public data for the entire vitrine site.
 * Returned by the public API in a single request for fast initial load.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitrinePublicDataDTO {

    private String slug;
    private VitrineConfigDTO config;
    private List<VitrinePageDTO> pages;
    private List<VitrineAnnouncementDTO> announcements;
    private List<VitrineGalleryDTO> gallery;
}
