package com.schoolSys.schooolSys.course;

import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import com.schoolSys.schooolSys.course.dto.CourseRequestDTO;
import com.schoolSys.schooolSys.course.dto.CourseResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting between {@link Course} entities and DTOs.
 * <p>
 * The {@code teacher} relationship requires a database lookup,
 * so it is ignored here and resolved by the service layer.
 * </p>
 */
@Mapper(componentModel = "spring")
public interface CourseMapper extends GenericMapper<Course, CourseRequestDTO, CourseResponseDTO> {

    @Override
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName", expression = "java(entity.getTeacher() != null ? entity.getTeacher().getFirstName() + \" \" + entity.getTeacher().getLastName() : null)")
    CourseResponseDTO toResponseDTO(Course entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    Course toEntity(CourseRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    void updateEntity(CourseRequestDTO dto, @MappingTarget Course entity);
}
