package com.dmytronik.taskmanager.controller;

import com.dmytronik.taskmanager.dto.TaskRequestDTO;
import com.dmytronik.taskmanager.dto.TaskResponseDTO;
import com.dmytronik.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponseDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        TaskResponseDTO created = taskService.createTask(taskRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public TaskResponseDTO getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PutMapping("/{id}")
    public TaskResponseDTO updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequestDTO taskRequestDTO) {
        return taskService.updateTask(id, taskRequestDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
