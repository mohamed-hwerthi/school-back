package com.schoolSys.schooolSys.enrollment;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.course.Course;
import com.schoolSys.schooolSys.course.CourseRepository;
import com.schoolSys.schooolSys.enrollment.dto.EnrollmentRequestDTO;
import com.schoolSys.schooolSys.enrollment.dto.EnrollmentResponseDTO;
import com.schoolSys.schooolSys.student.Student;
import com.schoolSys.schooolSys.student.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing {@link Enrollment} entities.
 * <p>
 * An enrollment links a {@link Student} to a {@link Course}.
 * All operations are scoped to the current tenant's schema.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;

    /**
     * Returns all enrollments in the current school's schema.
     *
     * @return list of enrollment DTOs
     */
    public List<EnrollmentResponseDTO> findAll() {
        return enrollmentRepository.findAll().stream()
                .map(enrollmentMapper::toResponseDTO)
                .toList();
    }

    /**
     * Finds an enrollment by ID.
     *
     * @param id the enrollment ID
     * @return the enrollment DTO
     * @throws ResourceNotFoundException if the enrollment does not exist
     */
    public EnrollmentResponseDTO findById(UUID id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
        return enrollmentMapper.toResponseDTO(enrollment);
    }

    /**
     * Creates a new enrollment linking a student to a course.
     *
     * @param dto the creation request
     * @return the created enrollment DTO
     * @throws ResourceNotFoundException if the student or course does not exist
     */
    @Transactional
    public EnrollmentResponseDTO create(EnrollmentRequestDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", dto.getCourseId()));

        Enrollment enrollment = enrollmentMapper.toEntity(dto);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        return enrollmentMapper.toResponseDTO(enrollmentRepository.save(enrollment));
    }

    /**
     * Updates an existing enrollment.
     *
     * @param id  the enrollment ID
     * @param dto the update request
     * @return the updated enrollment DTO
     * @throws ResourceNotFoundException if the enrollment, student, or course does not exist
     */
    @Transactional
    public EnrollmentResponseDTO update(UUID id, EnrollmentRequestDTO dto) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", dto.getStudentId()));
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", dto.getCourseId()));

        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(dto.getEnrollmentDate());
        enrollment.setStatus(dto.getStatus());
        return enrollmentMapper.toResponseDTO(enrollmentRepository.save(enrollment));
    }

    /**
     * Deletes an enrollment by ID.
     *
     * @param id the enrollment ID
     * @throws ResourceNotFoundException if the enrollment does not exist
     */
    @Transactional
    public void delete(UUID id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Enrollment", id);
        }
        enrollmentRepository.deleteById(id);
    }
}
