package com.schoolSys.schooolSys.evenement;

import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import com.schoolSys.schooolSys.evenement.dto.EvenementCalendrierRequestDTO;
import com.schoolSys.schooolSys.evenement.dto.EvenementCalendrierResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EvenementCalendrierMapper
        extends GenericMapper<EvenementCalendrier, EvenementCalendrierRequestDTO, EvenementCalendrierResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EvenementCalendrier toEntity(EvenementCalendrierRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(EvenementCalendrierRequestDTO dto, @MappingTarget EvenementCalendrier entity);
}
