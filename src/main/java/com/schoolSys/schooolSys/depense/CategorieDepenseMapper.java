package com.schoolSys.schooolSys.depense;

import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import com.schoolSys.schooolSys.depense.dto.CategorieDepenseRequestDTO;
import com.schoolSys.schooolSys.depense.dto.CategorieDepenseResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategorieDepenseMapper extends GenericMapper<CategorieDepense, CategorieDepenseRequestDTO, CategorieDepenseResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CategorieDepense toEntity(CategorieDepenseRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(CategorieDepenseRequestDTO dto, @MappingTarget CategorieDepense entity);
}
