package com.schoolSys.schooolSys.rapport;

import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import com.schoolSys.schooolSys.rapport.dto.RapportRequestDTO;
import com.schoolSys.schooolSys.rapport.dto.RapportResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RapportMapper extends GenericMapper<Rapport, RapportRequestDTO, RapportResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateGeneration", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Rapport toEntity(RapportRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateGeneration", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(RapportRequestDTO dto, @MappingTarget Rapport rapport);
}
