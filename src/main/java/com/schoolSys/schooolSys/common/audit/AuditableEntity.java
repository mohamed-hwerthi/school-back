package com.schoolSys.schooolSys.common.audit;

import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base class for sensitive entities that must record which user created and
 * last modified each row (SEC-025).
 * <p>
 * The {@code created_by} / {@code updated_by} columns are populated
 * automatically by Spring Data JPA auditing from the security context — see
 * {@link JpaAuditingConfig}. Timestamps ({@code created_at} / {@code updated_at})
 * remain managed by each entity, so they are intentionally not declared here.
 * </p>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AuditableEntity {

    /** Id of the user who created the row; null for system or anonymous writes. */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    /** Id of the user who last modified the row. */
    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;
}
