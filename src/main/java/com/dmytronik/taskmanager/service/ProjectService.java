package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.dto.ProjectResponseDTO;
import com.dmytronik.taskmanager.dto.UserSummaryDTO;
import com.dmytronik.taskmanager.exception.ProjectNotFoundException;
import com.dmytronik.taskmanager.model.Project;
import com.dmytronik.taskmanager.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<ProjectResponseDTO> findAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public ProjectResponseDTO findProjectById(Long id) {
        return convertToDTO(projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id))
        );
    }

    private ProjectResponseDTO convertToDTO(Project project) {
        return new ProjectResponseDTO(
                project.getId(), project.getTitle(), project.getDescription(),
                project.getStatus(), project.getCreatedAt(),
                new UserSummaryDTO(project.getOwner().getId(), project.getOwner().getUsername()
                )
        );
    }
}
