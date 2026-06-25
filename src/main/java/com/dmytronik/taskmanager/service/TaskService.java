package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.dto.TaskRequestDTO;
import com.dmytronik.taskmanager.dto.TaskResponseDTO;
import com.dmytronik.taskmanager.dto.UserSummaryDTO;
import com.dmytronik.taskmanager.exception.ProjectNotFoundOrAccessException;
import com.dmytronik.taskmanager.exception.TaskNotFoundException;
import com.dmytronik.taskmanager.exception.UserNotFoundException;
import com.dmytronik.taskmanager.model.Project;
import com.dmytronik.taskmanager.model.Task;
import com.dmytronik.taskmanager.model.User;
import com.dmytronik.taskmanager.repository.ProjectRepository;
import com.dmytronik.taskmanager.repository.TaskRepository;
import com.dmytronik.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<TaskResponseDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public TaskResponseDTO getTaskById(Long id) {
        return convertToDTO(findTaskOrThrow(id));
    }

    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO, Long currentUserId) {
        Task task = convertToEntity(taskRequestDTO);

        Long projectId = taskRequestDTO.getProjectId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundOrAccessException(projectId));
        task.setProject(project);

        User reporter = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
        task.setReporter(reporter);

        return convertToDTO(taskRepository.save(task));
    }

    public TaskResponseDTO updateTask(Long id, TaskRequestDTO taskRequestDTO) {
        Task task = findTaskOrThrow(id);
        task.setTitle(taskRequestDTO.getTitle());
        task.setDescription(taskRequestDTO.getDescription());
        task.setStatus(taskRequestDTO.getStatus());
        return convertToDTO(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        findTaskOrThrow(id);
        taskRepository.deleteById(id);
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    // Entity -> ResponseDTO:
    private TaskResponseDTO convertToDTO(Task task) {
        return new TaskResponseDTO(
                task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getCreatedAt(),
                new UserSummaryDTO(task.getReporter().getId(), task.getReporter().getUsername()),
                task.getAssignee() != null
                        ? new UserSummaryDTO(task.getAssignee().getId(), task.getAssignee().getUsername())
                        : null
        );
    }

    // RequestDTO -> Entity:
    private Task convertToEntity(TaskRequestDTO dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        return task;
    }
}
