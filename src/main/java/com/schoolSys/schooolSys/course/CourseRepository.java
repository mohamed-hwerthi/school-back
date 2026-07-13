package com.schoolSys.schooolSys.course;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link Course} entities.
 */
public interface CourseRepository extends JpaRepository<Course, UUID> {
}
