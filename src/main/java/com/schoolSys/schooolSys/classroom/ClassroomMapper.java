package com.schoolSys.schooolSys.classroom;

import com.schoolSys.schooolSys.classroom.dto.ClassroomRequestDTO;
import com.schoolSys.schooolSys.classroom.dto.ClassroomResponseDTO;
import com.schoolSys.schooolSys.common.mapper.GenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting between {@link Classroom} entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface ClassroomMapper extends GenericMapper<Classroom, ClassroomRequestDTO, ClassroomResponseDTO> {

    @Override
    @Mapping(target = "id", ignore = true)
    Classroom toEntity(ClassroomRequestDTO dto);

    @Override
    @Mapping(target = "id", ignore = true)
    void updateEntity(ClassroomRequestDTO dto, @MappingTarget Classroom classroom);
}
