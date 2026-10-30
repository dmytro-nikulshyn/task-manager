package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.exception.ProjectNotFoundOrAccessException;
import com.dmytronik.taskmanager.exception.UserNotFoundException;
import com.dmytronik.taskmanager.model.Project;
import com.dmytronik.taskmanager.model.Role;
import com.dmytronik.taskmanager.model.User;
import com.dmytronik.taskmanager.repository.ProjectRepository;
import com.dmytronik.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectAccessChecker {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // VIEW: знайти + перевірити
    public Project getProjectWithViewAccess(Long projectId, Long currentUserId) {
        Project project = findProjectOrThrow(projectId);
        checkViewAccess(project, currentUserId);
        return project;
    }

    // VIEW: перевірити готовий (member/owner/admin)
    public void checkViewAccess(Project project, Long currentUserId) {
        User currentUser = findUserOrThrow(currentUserId);
        if (!project.getMembers().contains(currentUser)
                && !project.getOwner().getId().equals(currentUserId)
                && currentUser.getRole() != Role.ADMIN) {
            throw new ProjectNotFoundOrAccessException(project.getId());
        }
    }

    // EDIT: знайти + перевірити
    public Project getProjectWithEditAccess(Long projectId, Long currentUserId) {
        Project project = findProjectOrThrow(projectId);
        checkEditAccess(project, currentUserId);
        return project;
    }

    // EDIT: перевірити готовий (owner/admin)
    public void checkEditAccess(Project project, Long currentUserId) {
        User currentUser = findUserOrThrow(currentUserId);
        if (!project.getOwner().getId().equals(currentUserId)
                && currentUser.getRole() != Role.ADMIN) {
            throw new ProjectNotFoundOrAccessException(project.getId());
        }
    }

    // приватні хелпери (DRY)
    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundOrAccessException(projectId));
    }

    private User findUserOrThrow(Long currentUserId) {
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
    }
}