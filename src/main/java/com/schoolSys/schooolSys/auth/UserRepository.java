package com.schoolSys.schooolSys.auth;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Users of a given tenant, excluding a role — scopes non-super-admin list views. */
    Page<User> findByTenantIdAndRoleNot(String tenantId, UserRole role, Pageable pageable);
}
