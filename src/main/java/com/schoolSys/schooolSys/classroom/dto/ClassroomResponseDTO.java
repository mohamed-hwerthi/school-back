package com.schoolSys.schooolSys.classroom.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

/**
 * DTO returned when reading classroom information.
 */
@Data
@Builder
public class ClassroomResponseDTO {
    private UUID id;
    private String name;
    private Integer capacity;
    private String location;
    private String type;
    private Integer floor;
    private String equipment;
    private String status;
}
