package com.dmytronik.taskmanager.service;

import com.dmytronik.taskmanager.exception.ForbiddenOperationException;
import com.dmytronik.taskmanager.exception.TaskNotFoundOrAccessException;
import com.dmytronik.taskmanager.exception.UserNotFoundException;
import com.dmytronik.taskmanager.model.Task;
import com.dmytronik.taskmanager.model.User;
import com.dmytronik.taskmanager.repository.TaskRepository;
import com.dmytronik.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAccessChecker {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectAccessChecker projectAccessChecker;

    public Task getTaskWithViewAccess(Long taskId, Long currentUserId) {
        Task task = findTaskOrThrow(taskId);
        if (!projectAccessChecker.canView(task.getProject(), currentUserId)) {
            throw new TaskNotFoundOrAccessException(taskId);
        }
        return task;
    }

    public Task getTaskWithEditAccess(Long taskId, Long currentUserId) {
        Task task = getTaskWithViewAccess(taskId, currentUserId);
        User currentUser = findUserOrThrow(currentUserId);
        if (currentUser.isAdmin()
                || task.getProject().isOwner(currentUserId)
                || task.isAssignee(currentUserId)
                || (!task.isAssigned() && task.isReporter(currentUserId))) {
            return task;
        }
        throw new ForbiddenOperationException("You do not have permission to edit this task");
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundOrAccessException(id));
    }

    private User findUserOrThrow(Long currentUserId) {
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
    }
}
