package com.schoolSys.schooolSys.finance;

import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import com.schoolSys.schooolSys.finance.dto.TypeFraisRequestDTO;
import com.schoolSys.schooolSys.finance.dto.TypeFraisResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TypeFraisMapper extends GenericMapper<TypeFrais, TypeFraisRequestDTO, TypeFraisResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    TypeFrais toEntity(TypeFraisRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(TypeFraisRequestDTO dto, @MappingTarget TypeFrais entity);
}
