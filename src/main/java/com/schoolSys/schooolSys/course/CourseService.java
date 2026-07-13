package com.schoolSys.schooolSys.course;

import java.util.UUID;

import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import com.schoolSys.schooolSys.course.dto.CourseRequestDTO;
import com.schoolSys.schooolSys.course.dto.CourseResponseDTO;
import com.schoolSys.schooolSys.teacher.Teacher;
import com.schoolSys.schooolSys.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing {@link Course} entities.
 * <p>
 * Courses can optionally be assigned to a {@link Teacher}.
 * All operations are scoped to the current tenant's schema.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final CourseMapper courseMapper;

    /**
     * Returns all courses in the current school's schema.
     *
     * @return list of course DTOs
     */
    public List<CourseResponseDTO> findAll() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toResponseDTO)
                .toList();
    }

    /**
     * Finds a course by ID.
     *
     * @param id the course ID
     * @return the course DTO
     * @throws ResourceNotFoundException if the course does not exist
     */
    public CourseResponseDTO findById(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        return courseMapper.toResponseDTO(course);
    }

    /**
     * Creates a new course, optionally assigned to a teacher.
     *
     * @param dto the creation request
     * @return the created course DTO
     * @throws ResourceNotFoundException if the specified teacher does not exist
     */
    @Transactional
    public CourseResponseDTO create(CourseRequestDTO dto) {
        Course course = courseMapper.toEntity(dto);
        resolveTeacher(dto.getTeacherId(), course);
        return courseMapper.toResponseDTO(courseRepository.save(course));
    }

    /**
     * Updates an existing course.
     *
     * @param id  the course ID
     * @param dto the update request
     * @return the updated course DTO
     * @throws ResourceNotFoundException if the course or teacher does not exist
     */
    @Transactional
    public CourseResponseDTO update(UUID id, CourseRequestDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        courseMapper.updateEntity(dto, course);
        resolveTeacher(dto.getTeacherId(), course);
        return courseMapper.toResponseDTO(courseRepository.save(course));
    }

    /**
     * Deletes a course by ID.
     *
     * @param id the course ID
     * @throws ResourceNotFoundException if the course does not exist
     */
    @Transactional
    public void delete(UUID id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", id);
        }
        courseRepository.deleteById(id);
    }

    /**
     * Looks up and assigns a teacher to the course if a teacher ID is provided.
     */
    private void resolveTeacher(UUID teacherId, Course course) {
        if (teacherId != null) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", teacherId));
            course.setTeacher(teacher);
        }
    }
}
