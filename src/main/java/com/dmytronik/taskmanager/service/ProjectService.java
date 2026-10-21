package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.dto.ProjectRequestDTO;
import com.dmytronik.taskmanager.dto.ProjectResponseDTO;
import com.dmytronik.taskmanager.dto.UserSummaryDTO;
import com.dmytronik.taskmanager.exception.ForbiddenOperationException;
import com.dmytronik.taskmanager.exception.MemberNotFoundException;
import com.dmytronik.taskmanager.exception.ProjectNotFoundOrAccessException;
import com.dmytronik.taskmanager.exception.UserNotFoundException;
import com.dmytronik.taskmanager.model.Project;
import com.dmytronik.taskmanager.model.Role;
import com.dmytronik.taskmanager.model.User;
import com.dmytronik.taskmanager.repository.ProjectRepository;
import com.dmytronik.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO projectRequestDTO, Long ownerId) {
        Project project = convertToEntity(projectRequestDTO);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));
        project.setOwner(owner);
        project.getMembers().add(owner);
        return convertToDTO(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getProjectsByOwnerId(Long ownerId, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(ownerId)) {
            throw new ForbiddenOperationException("You do not have permission to view these projects");
        }
        return projectRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(Long id, Long currentUserId) {
        Project project = getProjectWithAccessCheck(id, currentUserId);
        return convertToDTO(project);
    }

    @Transactional
    public ProjectResponseDTO updateProject(Long id, ProjectRequestDTO projectRequestDTO, Long currentUserId) {
        Project project = getProjectWithAccessCheck(id, currentUserId);

        project.setTitle(projectRequestDTO.getTitle());
        project.setDescription(projectRequestDTO.getDescription());
        project.setStatus(projectRequestDTO.getStatus());
        return convertToDTO(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id, Long currentUserId) {
        getProjectWithAccessCheck(id, currentUserId);
        projectRepository.deleteById(id);
    }

    @Transactional
    public ProjectResponseDTO addMember(Long projectId, Long userId, Long currentUserId) {
        Project project = getProjectWithAccessCheck(projectId, currentUserId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberNotFoundException(userId));
        project.getMembers().add(user);
        return convertToDTO(projectRepository.save(project));
    }

    private ProjectResponseDTO convertToDTO(Project project) {
        Set<UserSummaryDTO> memberDTOs = project.getMembers().stream()
                .map(member -> new UserSummaryDTO(member.getId(), member.getUsername()))
                .collect(Collectors.toSet());
        return new ProjectResponseDTO(
                project.getId(), project.getTitle(), project.getDescription(),
                project.getStatus(), project.getCreatedAt(),
                new UserSummaryDTO(project.getOwner().getId(), project.getOwner().getUsername()),
                memberDTOs);
    }

    private Project convertToEntity(ProjectRequestDTO projectRequestDTO) {
        Project project = new Project();
        project.setTitle(projectRequestDTO.getTitle());
        project.setDescription(projectRequestDTO.getDescription());
        project.setStatus(projectRequestDTO.getStatus());
        return project;
    }

    private Project getProjectWithAccessCheck(Long id, Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundOrAccessException(id));
        if (currentUser.getRole() != Role.ADMIN && !currentUserId.equals(project.getOwner().getId())) {
            throw new ProjectNotFoundOrAccessException(id);
        }
        return project;
    }
}
