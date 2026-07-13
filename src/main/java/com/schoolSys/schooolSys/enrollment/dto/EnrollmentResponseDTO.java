package com.schoolSys.schooolSys.enrollment.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.enrollment.Enrollment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO returned when reading enrollment information.
 */
@Data
@Builder
public class EnrollmentResponseDTO {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private UUID courseId;
    private String courseName;
    private LocalDate enrollmentDate;
    private Enrollment.EnrollmentStatus status;
}
