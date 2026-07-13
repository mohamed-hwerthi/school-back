package com.schoolSys.schooolSys.enrollment.dto;

import java.util.UUID;

import com.schoolSys.schooolSys.enrollment.Enrollment;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO for creating or updating an enrollment.
 */
@Data
public class EnrollmentRequestDTO {
    private UUID studentId;
    private UUID courseId;
    private LocalDate enrollmentDate;
    private Enrollment.EnrollmentStatus status;
}
