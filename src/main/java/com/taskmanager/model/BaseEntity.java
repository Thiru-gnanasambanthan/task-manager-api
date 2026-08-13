package com.taskmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Common fields every entity in this project shares:
 * - UUID primary key (instead of auto-increment long) - safer to expose in URLs,
 *   doesn't leak how many rows exist.
 * - createdAt / updatedAt, auto-populated by Spring Data JPA auditing.
 * - deletedAt, used for soft delete - rows are never physically removed,
 *   just marked as deleted so history/audit trail stays intact.
 *
 * Every entity (User, Project, Task, Comment) extends this instead of
 * repeating these fields four times.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    /** Null = active. Non-null = soft-deleted at this timestamp. */
    @Column
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
