package com.dmytronik.taskmanager.controller;

import com.dmytronik.taskmanager.dto.ProjectRequestDTO;
import com.dmytronik.taskmanager.dto.ProjectResponseDTO;
import com.dmytronik.taskmanager.security.CustomUserPrincipal;
import com.dmytronik.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProjectResponseDTO> getAllProjects() {
        return projectService.getAllProjects();
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO projectRequestDTO,
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        ProjectResponseDTO created = projectService.createProject(projectRequestDTO, customUserPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ProjectResponseDTO getProjectById(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        return projectService.getProjectById(id, customUserPrincipal.getId());
    }

    @GetMapping("/owner/{ownerId}")
    public List<ProjectResponseDTO> getProjectsByOwnerId(
            @PathVariable Long ownerId, @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        return projectService.getProjectsByOwnerId(ownerId, customUserPrincipal.getId());
    }

    @PutMapping("/{id}")
    public ProjectResponseDTO updateProject(
            @PathVariable Long id, @Valid @RequestBody ProjectRequestDTO projectRequestDTO,
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        return projectService.updateProject(id, projectRequestDTO, customUserPrincipal.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        projectService.deleteProject(id, customUserPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

}
