package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.dto.TaskAssigneeRequestDTO;
import com.dmytronik.taskmanager.dto.TaskRequestDTO;
import com.dmytronik.taskmanager.dto.TaskResponseDTO;
import com.dmytronik.taskmanager.dto.UserSummaryDTO;
import com.dmytronik.taskmanager.exception.AssigneeNotFoundException;
import com.dmytronik.taskmanager.exception.ProjectNotFoundOrAccessException;
import com.dmytronik.taskmanager.exception.TaskNotFoundOrAccessException;
import com.dmytronik.taskmanager.exception.UserNotFoundException;
import com.dmytronik.taskmanager.model.Project;
import com.dmytronik.taskmanager.model.Role;
import com.dmytronik.taskmanager.model.Task;
import com.dmytronik.taskmanager.model.User;
import com.dmytronik.taskmanager.repository.ProjectRepository;
import com.dmytronik.taskmanager.repository.TaskRepository;
import com.dmytronik.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectAccessChecker projectAccessChecker;

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id, Long currentUserId) {
        Task task = findTaskOrThrow(id);
        projectAccessChecker.checkViewAccess(task.getProject(), currentUserId);
        return convertToDTO(task);
    }

    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO, Long currentUserId) {
        Long projectId = taskRequestDTO.getProjectId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundOrAccessException(projectId));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
        if (!project.getMembers().contains(currentUser) && currentUser.getRole() != Role.ADMIN) {
            throw new ProjectNotFoundOrAccessException(projectId);
        }
        Task task = convertToEntity(taskRequestDTO);
        task.setProject(project);
        task.setReporter(currentUser);
        return convertToDTO(taskRepository.save(task));
    }

    @Transactional
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

    @Transactional
    public TaskResponseDTO assignTask(Long id, TaskAssigneeRequestDTO taskAssigneeRequestDTO) {
        Task task = findTaskOrThrow(id);
        Long assigneeId = taskAssigneeRequestDTO.getAssigneeId();
        User assignee = (assigneeId != null)
                ? userRepository.findById(assigneeId).orElseThrow(() -> new AssigneeNotFoundException(assigneeId))
                : null;
        task.setAssignee(assignee);
        return convertToDTO(taskRepository.save(task));
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundOrAccessException(id));
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
