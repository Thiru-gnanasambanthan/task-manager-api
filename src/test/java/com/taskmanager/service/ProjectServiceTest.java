package com.taskmanager.service;

import com.taskmanager.dto.ProjectRequest;
import com.taskmanager.dto.ProjectResponse;
import com.taskmanager.model.User;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit test: no Spring context, no database - just the service logic
 * with its dependencies mocked out. Fast, isolated, tests business logic only.
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("hashed")
                .name("Test User")
                .build();
        testUser.setId(UUID.randomUUID());

        // Simulate "someone is logged in" for SecurityContextHolder,
        // since our service reads the current user from there.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        testUser.getEmail(), null, java.util.List.of()));
    }

    @Test
    void createProject_savesProjectOwnedByCurrentUser() {
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));

        ProjectRequest request = new ProjectRequest("New Project", "A description");

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.getName()).isEqualTo("New Project");
        assertThat(response.getOwnerId()).isEqualTo(testUser.getId());
    }
}