package com.schoolSys.schooolSys.vitrine;

import com.schoolSys.schooolSys.vitrine.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VitrineMapper {

    // Config
    VitrineConfigDTO toConfigDTO(VitrineConfig entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateConfig(VitrineConfigDTO dto, @MappingTarget VitrineConfig entity);

    // Page
    @Mapping(target = "sections", source = "sections")
    VitrinePageDTO toPageDTO(VitrinePage entity);

    List<VitrinePageDTO> toPageDTOList(List<VitrinePage> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VitrinePage toPageEntity(VitrinePageDTO dto);

    // Section
    @Mapping(target = "pageId", source = "page.id")
    VitrineSectionDTO toSectionDTO(VitrineSection entity);

    List<VitrineSectionDTO> toSectionDTOList(List<VitrineSection> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "page", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VitrineSection toSectionEntity(VitrineSectionDTO dto);

    // Gallery
    VitrineGalleryDTO toGalleryDTO(VitrineGallery entity);

    List<VitrineGalleryDTO> toGalleryDTOList(List<VitrineGallery> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    VitrineGallery toGalleryEntity(VitrineGalleryDTO dto);

    // Announcement
    VitrineAnnouncementDTO toAnnouncementDTO(VitrineAnnouncement entity);

    List<VitrineAnnouncementDTO> toAnnouncementDTOList(List<VitrineAnnouncement> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    VitrineAnnouncement toAnnouncementEntity(VitrineAnnouncementDTO dto);

    // Contact
    VitrineContactDTO toContactDTO(VitrineContact entity);

    List<VitrineContactDTO> toContactDTOList(List<VitrineContact> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isRead", ignore = true)
    @Mapping(target = "replied", ignore = true)
    @Mapping(target = "replyText", ignore = true)
    @Mapping(target = "repliedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    VitrineContact toContactEntity(VitrineContactRequestDTO dto);
}
