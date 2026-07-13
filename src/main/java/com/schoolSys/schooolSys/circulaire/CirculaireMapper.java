package com.schoolSys.schooolSys.circulaire;

import com.schoolSys.schooolSys.circulaire.dto.CirculaireRequestDTO;
import com.schoolSys.schooolSys.circulaire.dto.CirculaireResponseDTO;
import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CirculaireMapper extends GenericMapper<Circulaire, CirculaireRequestDTO, CirculaireResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "datePublication", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Circulaire toEntity(CirculaireRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "datePublication", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CirculaireRequestDTO dto, @MappingTarget Circulaire circulaire);
}
