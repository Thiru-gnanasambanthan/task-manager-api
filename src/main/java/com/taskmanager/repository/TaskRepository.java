package com.taskmanager.repository;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findByOwnerIdAndDeletedAtIsNull(UUID ownerId, Pageable pageable);
    Page<Task> findByProjectIdAndDeletedAtIsNull(UUID projectId, Pageable pageable);
    Page<Task> findByProjectIdAndStatusAndDeletedAtIsNull(
            UUID projectId, TaskStatus status, Pageable pageable);
}