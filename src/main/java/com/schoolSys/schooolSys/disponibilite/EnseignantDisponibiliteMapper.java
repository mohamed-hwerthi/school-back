package com.schoolSys.schooolSys.disponibilite;

import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import com.schoolSys.schooolSys.disponibilite.dto.EnseignantDisponibiliteRequestDTO;
import com.schoolSys.schooolSys.disponibilite.dto.EnseignantDisponibiliteResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EnseignantDisponibiliteMapper extends GenericMapper<
        EnseignantDisponibilite,
        EnseignantDisponibiliteRequestDTO,
        EnseignantDisponibiliteResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    EnseignantDisponibilite toEntity(EnseignantDisponibiliteRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(EnseignantDisponibiliteRequestDTO dto, @MappingTarget EnseignantDisponibilite entity);
}
