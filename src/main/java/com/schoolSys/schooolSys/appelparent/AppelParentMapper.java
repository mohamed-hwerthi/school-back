package com.schoolSys.schooolSys.appelparent;

import com.schoolSys.schooolSys.appelparent.dto.AppelParentRequestDTO;
import com.schoolSys.schooolSys.appelparent.dto.AppelParentResponseDTO;
import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AppelParentMapper extends GenericMapper<
        AppelParent,
        AppelParentRequestDTO,
        AppelParentResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    AppelParent toEntity(AppelParentRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    // Preserve existing dateAppel during PUT — clients rarely re-send it.
    // (Use a dedicated reschedule endpoint if/when changing the call timestamp matters.)
    @Mapping(target = "dateAppel", ignore = true)
    void updateEntity(AppelParentRequestDTO dto, @MappingTarget AppelParent entity);
}
