package com.schoolSys.schooolSys.classroom;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link Classroom} entities.
 */
public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
}
