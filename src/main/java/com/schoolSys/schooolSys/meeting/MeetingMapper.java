package com.schoolSys.schooolSys.meeting;

import com.schoolSys.schooolSys.meeting.dto.MeetingRequestDTO;
import com.schoolSys.schooolSys.meeting.dto.MeetingResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MeetingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Meeting toEntity(MeetingRequestDTO dto);

    @Mapping(target = "enseignantName", ignore = true)
    @Mapping(target = "parentName", ignore = true)
    @Mapping(target = "studentName", ignore = true)
    MeetingResponseDTO toResponse(Meeting entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(MeetingRequestDTO dto, @MappingTarget Meeting entity);
}
