package com.schoolSys.schooolSys.enrollment;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link Enrollment} entities.
 */
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
}
