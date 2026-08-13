package com.taskmanager.service;

import com.taskmanager.dto.TaskRequest;
import com.taskmanager.dto.TaskResponse;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.model.Project;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.model.User;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskResponse createTask(TaskRequest request) {
        User owner = getCurrentUser();
        Project project = findOwnedProject(request.getProjectId(), owner);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .project(project)
                .owner(owner)
                .build();

        taskRepository.save(task);
        return toResponse(task);
    }

    public Page<TaskResponse> getMyTasks(Pageable pageable) {
        User owner = getCurrentUser();
        return taskRepository.findByOwnerIdAndDeletedAtIsNull(owner.getId(), pageable)
                .map(this::toResponse);
    }

    public Page<TaskResponse> getTasksByProject(UUID projectId, Pageable pageable) {
        User owner = getCurrentUser();
        findOwnedProject(projectId, owner); // verifies access, discards result
        return taskRepository.findByProjectIdAndDeletedAtIsNull(projectId, pageable)
                .map(this::toResponse);
    }

    public Page<TaskResponse> getTasksByProjectAndStatus(UUID projectId, TaskStatus status, Pageable pageable) {
        User owner = getCurrentUser();
        findOwnedProject(projectId, owner);
        return taskRepository.findByProjectIdAndStatusAndDeletedAtIsNull(projectId, status, pageable)
                .map(this::toResponse);
    }

    public TaskResponse getTask(UUID taskId) {
        Task task = findOwnedTask(taskId);
        return toResponse(task);
    }

    public TaskResponse updateTask(UUID taskId, TaskRequest request) {
        Task task = findOwnedTask(taskId);
        User owner = getCurrentUser();

        // Allow moving a task to a different project the user owns
        if (!task.getProject().getId().equals(request.getProjectId())) {
            Project newProject = findOwnedProject(request.getProjectId(), owner);
            task.setProject(newProject);
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        taskRepository.save(task);
        return toResponse(task);
    }

    public void deleteTask(UUID taskId) {
        Task task = findOwnedTask(taskId);
        task.setDeletedAt(Instant.now());
        taskRepository.save(task);
    }

    private Task findOwnedTask(UUID taskId) {
        User owner = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        if (!task.getOwner().getId().equals(owner.getId())) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return task;
    }

    private Project findOwnedProject(UUID projectId, User owner) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        if (!project.getOwner().getId().equals(owner.getId())) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }
        return project;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getOwner().getId(),
                task.getCreatedAt()
        );
    }
}