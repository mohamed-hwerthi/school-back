package com.schoolSys.schooolSys.course.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

/**
 * DTO returned when reading course information.
 */
@Data
@Builder
public class CourseResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private String code;
    private UUID teacherId;
    private String teacherName;
}
