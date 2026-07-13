package com.schoolSys.schooolSys.volumehoraire;

import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import com.schoolSys.schooolSys.volumehoraire.dto.ModuleClasseVolumeRequestDTO;
import com.schoolSys.schooolSys.volumehoraire.dto.ModuleClasseVolumeResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ModuleClasseVolumeMapper extends GenericMapper<
        ModuleClasseVolume,
        ModuleClasseVolumeRequestDTO,
        ModuleClasseVolumeResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    ModuleClasseVolume toEntity(ModuleClasseVolumeRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(ModuleClasseVolumeRequestDTO dto, @MappingTarget ModuleClasseVolume entity);
}
