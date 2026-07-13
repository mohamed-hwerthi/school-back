package com.schoolSys.schooolSys.cantine;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    List<Menu> findBySemaineOrderByDateMenuAsc(Integer semaine);

    List<Menu> findByDateMenuBetweenOrderByDateMenuAsc(LocalDate start, LocalDate end);

    List<Menu> findByDateMenu(LocalDate date);

    List<Menu> findByTypeRegime(String typeRegime);

    @Query("SELECT m.platPrincipal, COUNT(m) FROM Menu m " +
            "WHERE m.dateMenu BETWEEN :start AND :end " +
            "GROUP BY m.platPrincipal ORDER BY COUNT(m) DESC")
    List<Object[]> topPlatsBetween(@Param("start") LocalDate start, @Param("end") LocalDate end, Pageable pageable);
}
