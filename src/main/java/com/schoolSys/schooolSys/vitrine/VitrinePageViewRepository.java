package com.schoolSys.schooolSys.vitrine;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VitrinePageViewRepository extends JpaRepository<VitrinePageView, UUID> {

    long countByCreatedAtAfter(LocalDateTime since);

    @Query("SELECT v.pageSlug, COUNT(v) FROM VitrinePageView v " +
           "WHERE v.createdAt > :since GROUP BY v.pageSlug ORDER BY COUNT(v) DESC")
    List<Object[]> countByPageSlugSince(@Param("since") LocalDateTime since);

    @Query("SELECT FUNCTION('DATE', v.createdAt), COUNT(v) FROM VitrinePageView v " +
           "WHERE v.createdAt > :since GROUP BY FUNCTION('DATE', v.createdAt) ORDER BY FUNCTION('DATE', v.createdAt)")
    List<Object[]> countByDaySince(@Param("since") LocalDateTime since);
}
