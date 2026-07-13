package com.schoolSys.schooolSys.classroom;

import java.util.UUID;

import com.schoolSys.schooolSys.classroom.dto.ClassroomRequestDTO;
import com.schoolSys.schooolSys.classroom.dto.ClassroomResponseDTO;
import com.schoolSys.schooolSys.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing {@link Classroom} entities.
 * <p>
 * All operations are scoped to the current tenant's schema.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ClassroomMapper classroomMapper;

    /**
     * Returns all classrooms in the current school's schema.
     *
     * @return list of classroom DTOs
     */
    public List<ClassroomResponseDTO> findAll() {
        return classroomRepository.findAll().stream()
                .map(classroomMapper::toResponseDTO)
                .toList();
    }

    /**
     * Finds a classroom by ID.
     *
     * @param id the classroom ID
     * @return the classroom DTO
     * @throws ResourceNotFoundException if the classroom does not exist
     */
    public ClassroomResponseDTO findById(UUID id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", id));
        return classroomMapper.toResponseDTO(classroom);
    }

    /**
     * Creates a new classroom.
     *
     * @param dto the creation request
     * @return the created classroom DTO
     */
    @Transactional
    public ClassroomResponseDTO create(ClassroomRequestDTO dto) {
        Classroom classroom = classroomMapper.toEntity(dto);
        return classroomMapper.toResponseDTO(classroomRepository.save(classroom));
    }

    /**
     * Updates an existing classroom.
     *
     * @param id  the classroom ID
     * @param dto the update request
     * @return the updated classroom DTO
     * @throws ResourceNotFoundException if the classroom does not exist
     */
    @Transactional
    public ClassroomResponseDTO update(UUID id, ClassroomRequestDTO dto) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", id));
        classroomMapper.updateEntity(dto, classroom);
        return classroomMapper.toResponseDTO(classroomRepository.save(classroom));
    }

    /**
     * Deletes a classroom by ID.
     *
     * @param id the classroom ID
     * @throws ResourceNotFoundException if the classroom does not exist
     */
    @Transactional
    public void delete(UUID id) {
        if (!classroomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Classroom", id);
        }
        classroomRepository.deleteById(id);
    }
}
