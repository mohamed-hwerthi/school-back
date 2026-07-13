package com.schoolSys.schooolSys.student;

import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import com.schoolSys.schooolSys.student.dto.StudentRequestDTO;
import com.schoolSys.schooolSys.student.dto.StudentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StudentMapper extends GenericMapper<Student, StudentRequestDTO, StudentResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "matricule", ignore = true)
    Student toEntity(StudentRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "matricule", ignore = true)
    void updateEntity(StudentRequestDTO dto, @MappingTarget Student student);
}
