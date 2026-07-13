package com.schoolSys.schooolSys.classroom.dto;

import lombok.Data;

/**
 * DTO for creating or updating a classroom.
 */
@Data
public class ClassroomRequestDTO {
    private String name;
    private Integer capacity;
    private String location;
    private String type;
    private Integer floor;
    private String equipment;
    private String status;
}
