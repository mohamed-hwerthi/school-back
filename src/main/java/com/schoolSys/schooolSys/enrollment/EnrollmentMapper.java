package com.schoolSys.schooolSys.enrollment;

import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import com.schoolSys.schooolSys.enrollment.dto.EnrollmentRequestDTO;
import com.schoolSys.schooolSys.enrollment.dto.EnrollmentResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting between {@link Enrollment} entities and DTOs.
 * <p>
 * The {@code student} and {@code course} relationships require database lookups,
 * so they are ignored here and resolved by the service layer.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface EnrollmentMapper extends GenericMapper<Enrollment, EnrollmentRequestDTO, EnrollmentResponseDTO> {

    @Override
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(entity.getStudent().getFirstName() + \" \" + entity.getStudent().getLastName())")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseName", source = "course.name")
    EnrollmentResponseDTO toResponseDTO(Enrollment entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "course", ignore = true)
    Enrollment toEntity(EnrollmentRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "course", ignore = true)
    void updateEntity(EnrollmentRequestDTO dto, @MappingTarget Enrollment entity);
}
