package com.schoolSys.schooolSys.teacher;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data repository for {@link Teacher} entities.
 */
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

    Optional<Teacher> findByEmail(String email);

    @Query("SELECT t FROM Teacher t WHERE "
            + "(cast(:search as string) IS NULL OR "
            + "  lower(t.firstName) LIKE lower(concat('%', cast(:search as string), '%')) OR "
            + "  lower(t.lastName) LIKE lower(concat('%', cast(:search as string), '%')) OR "
            + "  lower(coalesce(t.email, '')) LIKE lower(concat('%', cast(:search as string), '%')) OR "
            + "  lower(coalesce(t.specialization, '')) LIKE lower(concat('%', cast(:search as string), '%'))) "
            + "AND (cast(:statut as string) IS NULL OR lower(t.statut) = lower(cast(:statut as string)))")
    Page<Teacher> findFiltered(@Param("search") String search,
                               @Param("statut") String statut,
                               Pageable pageable);
}
