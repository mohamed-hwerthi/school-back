package com.schoolSys.schooolSys.finance;

import com.schoolSys.schooolSys.finance.dto.PaiementResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaiementMapper {

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.firstName", target = "studentFirstName")
    @Mapping(source = "student.lastName", target = "studentLastName")
    @Mapping(source = "typeFrais.id", target = "typeFraisId")
    @Mapping(source = "typeFrais.nom", target = "typeFraisNom")
    PaiementResponseDTO toResponseDTO(Paiement paiement);

    List<PaiementResponseDTO> toResponseDTOList(List<Paiement> paiements);
}
