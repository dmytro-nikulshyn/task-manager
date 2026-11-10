package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.dto.TaskCreateRequestDTO;
import com.dmytronik.taskmanager.dto.TaskResponseDTO;
import com.dmytronik.taskmanager.exception.TaskNotFoundOrAccessException;
import com.dmytronik.taskmanager.model.Project;
import com.dmytronik.taskmanager.model.Task;
import com.dmytronik.taskmanager.model.TaskStatus;
import com.dmytronik.taskmanager.model.User;
import com.dmytronik.taskmanager.repository.TaskRepository;
import com.dmytronik.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskAccessChecker taskAccessChecker;
    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasks_shouldReturnEmptyList_whenNoTasksExist() {
        // Arrange
        when(taskRepository.findAll(any(Sort.class))).thenReturn(List.of());

        // Act
        List<TaskResponseDTO> result = taskService.getAllTasks();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getAllTasks_shouldReturnListOfDTOs_whenTasksExist() {
        // Arrange: створення Task об'єкт з даними
        //          налаштування mock щоб повертав список з цим Task
        User reporter = new User();
        reporter.setId(1L);
        reporter.setUsername("dmytronik");

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task 1");
        task.setDescription("Task 1");
        task.setStatus(TaskStatus.TODO);
        task.setReporter(reporter);

        when(taskRepository.findAll(any(Sort.class))).thenReturn(List.of(task));

        // Act: виклик getAllTasks()
        List<TaskResponseDTO> result = taskService.getAllTasks();

        // Assert: перевірка що результат має розмір 1
        //         і що title співпадає
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    void getTaskById_shouldReturnDTO_whenTaskExists() {
        // Arrange: створення Task з id=1L
        //          налаштування mock для getTaskWithViewAccess(taskId, currentUserId)
        Long taskId = 1L;
        Long currentUserId = 2L;

        User reporter = new User();
        reporter.setId(currentUserId);
        reporter.setUsername("dmytronik");

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Task 1");
        task.setDescription("Task 1");
        task.setStatus(TaskStatus.TODO);
        task.setReporter(reporter);

        when(taskAccessChecker.getTaskWithViewAccess(taskId, currentUserId)).thenReturn(task);

        // Act: виклик getTaskById(taskId, currentUserId)
        TaskResponseDTO taskResponseDTO = taskService.getTaskById(taskId, currentUserId);

        // Assert: перевірка що повернутий DTO має правильний id і title
        assertThat(taskResponseDTO.getId()).isEqualTo(taskId);
        assertThat(taskResponseDTO.getTitle()).isEqualTo("Task 1");
    }

    @Test
    void getTaskById_shouldThrowException_whenTaskNotFoundOrAccessDenied() {
        Long taskId = 1L;
        Long currentUserId = 2L;

        when(taskAccessChecker.getTaskWithViewAccess(taskId, currentUserId))
                .thenThrow(new TaskNotFoundOrAccessException(taskId));

        assertThatThrownBy(() -> taskService.getTaskById(taskId, currentUserId))
                .isInstanceOf(TaskNotFoundOrAccessException.class);
    }

    @Test
    void createTask_shouldReturnDTO_whenTaskCreated() {
        // Arrange — input data
        Long currentUserId = 1L;
        Long projectId = 2L;
        Long taskId = 3L;

        TaskCreateRequestDTO taskCreateRequestDTO = new TaskCreateRequestDTO();
        taskCreateRequestDTO.setTitle("Task 1");
        taskCreateRequestDTO.setDescription("Task 1");
        taskCreateRequestDTO.setProjectId(projectId);

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setUsername("dmytronik");

        Project project = new Project();
        project.setId(projectId);

        // Arrange — mock
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(taskAccessChecker.getProjectForTaskCreation(projectId, currentUserId)).thenReturn(project);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setId(taskId);
            return savedTask;
        });

        // Act
        TaskResponseDTO taskResponseDTO = taskService.createTask(taskCreateRequestDTO, currentUserId);

        // Assert
        assertThat(taskResponseDTO.getId()).isEqualTo(taskId);
        assertThat(taskResponseDTO.getTitle()).isEqualTo("Task 1");
        assertThat(taskResponseDTO.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(taskResponseDTO.getReporter().getId()).isEqualTo(currentUserId);

    }
}
