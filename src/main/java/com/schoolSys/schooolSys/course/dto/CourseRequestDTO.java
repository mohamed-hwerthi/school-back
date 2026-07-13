package com.schoolSys.schooolSys.course.dto;

import java.util.UUID;

import lombok.Data;

/**
 * DTO for creating or updating a course.
 */
@Data
public class CourseRequestDTO {
    private String name;
    private String description;
    private String code;
    /** Optional — the ID of the teacher to assign. */
    private UUID teacherId;
}
