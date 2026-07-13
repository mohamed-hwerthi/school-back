package com.schoolSys.schooolSys.appelparent;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppelParentRepository extends JpaRepository<AppelParent, UUID> {

    List<AppelParent> findByEleveIdOrderByDateAppelDesc(UUID eleveId);

    List<AppelParent> findAllByOrderByDateAppelDesc();
}
