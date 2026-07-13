package com.schoolSys.schooolSys.emploidutemps;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreneauRepository extends JpaRepository<Creneau, UUID> {

    List<Creneau> findByType(String type);
}
