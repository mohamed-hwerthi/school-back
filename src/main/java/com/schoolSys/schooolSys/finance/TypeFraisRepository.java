package com.schoolSys.schooolSys.finance;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TypeFraisRepository extends JpaRepository<TypeFrais, UUID> {

    List<TypeFrais> findByActifTrue();
}
