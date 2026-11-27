package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.dto.ProjectCreateRequestDTO;
import com.dmytronik.taskmanager.dto.ProjectResponseDTO;
import com.dmytronik.taskmanager.dto.UserSummaryDTO;
import com.dmytronik.taskmanager.exception.UserNotFoundException;
import com.dmytronik.taskmanager.model.Project;
import com.dmytronik.taskmanager.model.ProjectStatus;
import com.dmytronik.taskmanager.model.User;
import com.dmytronik.taskmanager.repository.ProjectRepository;
import com.dmytronik.taskmanager.repository.TaskRepository;
import com.dmytronik.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    private static final Long PROJECT_ID = 1L;
    private static final Long CURRENT_USER_ID = 2L;

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectAccessChecker projectAccessChecker;
    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProject_shouldReturnDTO_whenProjectCreated() {
        User currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);

        ProjectCreateRequestDTO projectCreateRequestDTO = new ProjectCreateRequestDTO();
        projectCreateRequestDTO.setTitle("Project 1");
        projectCreateRequestDTO.setDescription("Project 1 description");

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(currentUser));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocationOnMock -> {
            Project savedProject = invocationOnMock.getArgument(0);
            savedProject.setId(PROJECT_ID);
            return savedProject;
        });

        ProjectResponseDTO projectResponseDTO = projectService.createProject(projectCreateRequestDTO, CURRENT_USER_ID);

        assertThat(projectResponseDTO.getId()).isEqualTo(PROJECT_ID);
        assertThat(projectResponseDTO.getTitle()).isEqualTo("Project 1");
        assertThat(projectResponseDTO.getDescription()).isEqualTo("Project 1 description");
        assertThat(projectResponseDTO.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        assertThat(projectResponseDTO.getOwner().getId()).isEqualTo(CURRENT_USER_ID);
        assertThat(projectResponseDTO.getMembers()).extracting(UserSummaryDTO::getId).containsExactly(CURRENT_USER_ID);
    }

    @Test
    void createProject_shouldThrowUserNotFound_whenUserNotFound() {
        ProjectCreateRequestDTO projectCreateRequestDTO = new ProjectCreateRequestDTO();

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(projectCreateRequestDTO, CURRENT_USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }
}
