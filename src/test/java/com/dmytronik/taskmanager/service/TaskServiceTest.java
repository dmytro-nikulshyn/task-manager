package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.dto.TaskRequestDTO;
import com.dmytronik.taskmanager.dto.TaskResponseDTO;
import com.dmytronik.taskmanager.exception.TaskNotFoundException;
import com.dmytronik.taskmanager.model.Status;
import com.dmytronik.taskmanager.model.Task;
import com.dmytronik.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasks_shouldReturnEmptyList_whenNoTasksExist() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(List.of());

        // Act
        List<TaskResponseDTO> result = taskService.getAllTasks();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getAllTasks_shouldReturnListOfDTOs_whenTasksExist() {
        // Arrange: створи Task об'єкт з даними
        //          налаштуй mock щоб повертав список з цим Task
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task 1");
        task.setDescription("Task 1");
        task.setStatus(Status.TODO);

        when(taskRepository.findAll()).thenReturn(List.of(task));

        // Act: виклич getAllTasks()
        List<TaskResponseDTO> result = taskService.getAllTasks();

        // Assert: перевір що результат має розмір 1
        //         і що title співпадає
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    void getTaskById_shouldReturnDTO_whenTaskExists() {
        // Arrange: створи Task з id=1L
        //          налаштуй mock для findById(1L)
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task 1");
        task.setDescription("Task 1");
        task.setStatus(Status.TODO);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // Act: виклич getTaskById(1L)
        TaskResponseDTO taskResponseDTO = taskService.getTaskById(1L);

        // Assert: перевір що повернутий DTO має правильний id і title
        assertThat(taskResponseDTO.getId()).isEqualTo(1L);
        assertThat(taskResponseDTO.getTitle()).isEqualTo("Task 1");
    }

    @Test
    void getTaskById_shouldThrowException_whenTaskDoesNotExist() {

        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(1L)).isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void createTask_shouldReturnDTO_whenTaskCreated() {
        TaskRequestDTO taskRequestDTO = new TaskRequestDTO();
        taskRequestDTO.setTitle("Task 1");
        taskRequestDTO.setDescription("Task 1");
        taskRequestDTO.setStatus(Status.TODO);

        Task taskCreated = new Task();
        taskCreated.setId(1L);
        taskCreated.setTitle("Task 1");
        taskCreated.setDescription("Task 1");
        taskCreated.setStatus(Status.TODO);
        when(taskRepository.save(any(Task.class))).thenReturn(taskCreated);

        TaskResponseDTO taskResponseDTO = taskService.createTask(taskRequestDTO);

        assertThat(taskResponseDTO.getId()).isEqualTo(1L);
        assertThat(taskResponseDTO.getTitle()).isEqualTo("Task 1");
        assertThat(taskResponseDTO.getStatus()).isEqualTo(Status.TODO);
    }
}
