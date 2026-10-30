package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.dto.ProjectRequestDTO;
import com.dmytronik.taskmanager.dto.ProjectResponseDTO;
import com.dmytronik.taskmanager.dto.UserSummaryDTO;
import com.dmytronik.taskmanager.exception.ForbiddenOperationException;
import com.dmytronik.taskmanager.exception.MemberHasTasksException;
import com.dmytronik.taskmanager.exception.MemberNotFoundException;
import com.dmytronik.taskmanager.exception.UserNotFoundException;
import com.dmytronik.taskmanager.model.Project;
import com.dmytronik.taskmanager.model.Role;
import com.dmytronik.taskmanager.model.User;
import com.dmytronik.taskmanager.repository.ProjectRepository;
import com.dmytronik.taskmanager.repository.TaskRepository;
import com.dmytronik.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectAccessChecker projectAccessChecker;

    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO projectRequestDTO, Long currentUserId) {
        Project project = convertToEntity(projectRequestDTO);
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
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
        Project project = projectAccessChecker.getProjectWithViewAccess(id, currentUserId);
        return convertToDTO(project);
    }

    @Transactional
    public ProjectResponseDTO updateProject(Long id, ProjectRequestDTO projectRequestDTO, Long currentUserId) {
        Project project = projectAccessChecker.getProjectWithEditAccess(id, currentUserId);
        project.setTitle(projectRequestDTO.getTitle());
        project.setDescription(projectRequestDTO.getDescription());
        project.setStatus(projectRequestDTO.getStatus());
        return convertToDTO(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id, Long currentUserId) {
        projectAccessChecker.getProjectWithEditAccess(id, currentUserId);
        projectRepository.deleteById(id);
    }

    @Transactional
    public ProjectResponseDTO addMember(Long projectId, Long userId, Long currentUserId) {
        Project project = projectAccessChecker.getProjectWithEditAccess(projectId, currentUserId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberNotFoundException(userId));
        project.getMembers().add(user);
        return convertToDTO(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponseDTO removeMember(Long projectId, Long userId, Long currentUserId) {
        Project project = projectAccessChecker.getProjectWithEditAccess(projectId, currentUserId);
        if (userId.equals(project.getOwner().getId())) {
            throw new ForbiddenOperationException("Project owner cannot be removed from members");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MemberNotFoundException(userId));
        if (taskRepository.existsByProjectIdAndAssigneeId(projectId, userId)) {
            throw new MemberHasTasksException(userId, projectId);
        }
        project.getMembers().remove(user);
        return convertToDTO(projectRepository.save(project));
    }

    private ProjectResponseDTO convertToDTO(Project project) {
        List<UserSummaryDTO> memberDTOs = project.getMembers().stream()
                .sorted(Comparator.comparing(User::getUsername))
                .map(member -> new UserSummaryDTO(member.getId(), member.getUsername()))
                .toList();
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

}
