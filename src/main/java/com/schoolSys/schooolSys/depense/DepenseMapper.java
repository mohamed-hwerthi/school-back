package com.schoolSys.schooolSys.depense;

import com.schoolSys.schooolSys.depense.dto.DepenseResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DepenseMapper {

    @Mapping(source = "categorie.id", target = "categorieId")
    @Mapping(source = "categorie.nom", target = "categorieNom")
    DepenseResponseDTO toResponseDTO(Depense depense);

    List<DepenseResponseDTO> toResponseDTOList(List<Depense> depenses);
}
