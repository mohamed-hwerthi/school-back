package com.schoolSys.schooolSys.personnel;

import com.schoolSys.schooolSys.personnel.dto.PersonnelRequestDTO;
import com.schoolSys.schooolSys.personnel.dto.PersonnelResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface PersonnelMapper {

    @Mapping(target = "dateNaissance", source = "dateNaissance", qualifiedByName = "localDateToString")
    @Mapping(target = "dateEmbauche", source = "dateEmbauche", qualifiedByName = "localDateToString")
    PersonnelResponseDTO toResponseDTO(Personnel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateEmbauche", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "dateNaissance", source = "dateNaissance", qualifiedByName = "stringToLocalDate")
    Personnel toEntity(PersonnelRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateEmbauche", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "dateNaissance", source = "dateNaissance", qualifiedByName = "stringToLocalDate")
    void updateEntity(PersonnelRequestDTO dto, @MappingTarget Personnel personnel);

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String date) {
        return date != null && !date.isBlank() ? LocalDate.parse(date) : null;
    }
}
