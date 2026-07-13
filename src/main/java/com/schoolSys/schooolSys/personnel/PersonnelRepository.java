package com.schoolSys.schooolSys.personnel;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data repository for {@link Personnel} entities.
 */
public interface PersonnelRepository extends JpaRepository<Personnel, UUID> {

    @Query("SELECT p FROM Personnel p WHERE "
            + "(cast(:search as string) IS NULL OR "
            + "  lower(p.firstName) LIKE lower(concat('%', cast(:search as string), '%')) OR "
            + "  lower(p.lastName) LIKE lower(concat('%', cast(:search as string), '%')) OR "
            + "  lower(coalesce(p.email, '')) LIKE lower(concat('%', cast(:search as string), '%')) OR "
            + "  lower(coalesce(p.fonction, '')) LIKE lower(concat('%', cast(:search as string), '%'))) "
            + "AND (cast(:fonction as string) IS NULL OR lower(p.fonction) = lower(cast(:fonction as string))) "
            + "AND (cast(:statut as string) IS NULL OR lower(p.statut) = lower(cast(:statut as string)))")
    Page<Personnel> findFiltered(@Param("search") String search,
                                 @Param("fonction") String fonction,
                                 @Param("statut") String statut,
                                 Pageable pageable);
}
