package com.schoolSys.schooolSys.teacher;

import com.schoolSys.schooolSys.teacher.dto.TeacherRequestDTO;
import com.schoolSys.schooolSys.teacher.dto.TeacherResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface TeacherMapper {

    @Mapping(target = "dateNaissance", source = "dateNaissance", qualifiedByName = "localDateToString")
    @Mapping(target = "dateEmbauche", source = "dateEmbauche", qualifiedByName = "localDateToString")
    TeacherResponseDTO toResponseDTO(Teacher entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateEmbauche", ignore = true)
    @Mapping(target = "dateNaissance", source = "dateNaissance", qualifiedByName = "stringToLocalDate")
    Teacher toEntity(TeacherRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateEmbauche", ignore = true)
    @Mapping(target = "dateNaissance", source = "dateNaissance", qualifiedByName = "stringToLocalDate")
    void updateEntity(TeacherRequestDTO dto, @MappingTarget Teacher teacher);

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String date) {
        return date != null && !date.isBlank() ? LocalDate.parse(date) : null;
    }
}
