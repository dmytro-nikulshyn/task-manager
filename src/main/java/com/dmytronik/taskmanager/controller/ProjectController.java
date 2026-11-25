package com.dmytronik.taskmanager.controller;

import com.dmytronik.taskmanager.dto.ProjectCreateRequestDTO;
import com.dmytronik.taskmanager.dto.ProjectMemberRequestDTO;
import com.dmytronik.taskmanager.dto.ProjectResponseDTO;
import com.dmytronik.taskmanager.dto.ProjectUpdateRequestDTO;
import com.dmytronik.taskmanager.security.CustomUserPrincipal;
import com.dmytronik.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectCreateRequestDTO projectCreateRequestDTO,
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        ProjectResponseDTO created = projectService.createProject(projectCreateRequestDTO, customUserPrincipal.getId());
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
            @PathVariable Long id, @Valid @RequestBody ProjectUpdateRequestDTO projectUpdateRequestDTO,
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        return projectService.updateProject(id, projectUpdateRequestDTO, customUserPrincipal.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id, @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        projectService.deleteProject(id, customUserPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    public ProjectResponseDTO addMember(
            @PathVariable Long id,
            @Valid @RequestBody ProjectMemberRequestDTO projectMemberRequestDTO,
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        return projectService.addMember(id, projectMemberRequestDTO.getUserId(), customUserPrincipal.getId());
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ProjectResponseDTO removeMember(
            @PathVariable Long id, @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        return projectService.removeMember(id, userId, customUserPrincipal.getId());
    }

}
