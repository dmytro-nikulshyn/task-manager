package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.dto.TaskRequestDTO;
import com.dmytronik.taskmanager.dto.TaskResponseDTO;
import com.dmytronik.taskmanager.exception.TaskNotFoundException;
import com.dmytronik.taskmanager.model.Task;
import com.dmytronik.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponseDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public TaskResponseDTO getTaskById(Long id) {
        return convertToDTO(findTaskOrThrow(id));
    }

    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        Task task = convertToEntity(taskRequestDTO);
        return convertToDTO(taskRepository.save(task));
    }

    public TaskResponseDTO updateTask(Long id, TaskRequestDTO taskRequestDTO) {
        findTaskOrThrow(id);
        Task task = convertToEntity(taskRequestDTO);
        task.setId(id);
        taskRepository.save(task);
        return convertToDTO(taskRepository.findById(id).get());
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
                task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getCreatedAt());
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
