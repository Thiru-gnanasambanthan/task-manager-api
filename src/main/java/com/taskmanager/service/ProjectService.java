package com.taskmanager.service;

import com.taskmanager.dto.ProjectRequest;
import com.taskmanager.dto.ProjectResponse;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.model.Project;
import com.taskmanager.model.User;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectResponse createProject(ProjectRequest request) {
        User owner = getCurrentUser();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        projectRepository.save(project);
        return toResponse(project);
    }

    public Page<ProjectResponse> getMyProjects(Pageable pageable) {
        User owner = getCurrentUser();
        return projectRepository.findByOwnerIdAndDeletedAtIsNull(owner.getId(), pageable)
                .map(this::toResponse);
    }

    public ProjectResponse getProject(UUID projectId) {
        Project project = findOwnedProject(projectId);
        return toResponse(project);
    }

    public ProjectResponse updateProject(UUID projectId, ProjectRequest request) {
        Project project = findOwnedProject(projectId);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        projectRepository.save(project);
        return toResponse(project);
    }

    public void deleteProject(UUID projectId) {
        Project project = findOwnedProject(projectId);
        project.setDeletedAt(java.time.Instant.now()); // soft delete
        projectRepository.save(project);
    }

    /** Fetches a project AND verifies the current user owns it - throws otherwise. */
    private Project findOwnedProject(UUID projectId) {
        User owner = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        if (!project.getOwner().getId().equals(owner.getId())) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
            // Note: we say "not found" rather than "forbidden" on purpose -
            // this avoids confirming to an attacker that a project with this
            // ID exists but belongs to someone else.
        }
        return project;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getId(),
                project.getCreatedAt()
        );
    }
}