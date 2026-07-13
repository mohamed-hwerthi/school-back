package com.schoolSys.schooolSys.parent;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentStudentRepository extends JpaRepository<ParentStudent, UUID> {

    List<ParentStudent> findByParentUserId(UUID parentUserId);

    List<ParentStudent> findByStudentId(UUID studentId);
}
