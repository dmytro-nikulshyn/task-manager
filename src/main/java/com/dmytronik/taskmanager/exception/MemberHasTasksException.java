package com.dmytronik.taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MemberHasTasksException extends RuntimeException {
    public MemberHasTasksException(Long userId, Long projectId) {
        super("User with id " + userId + " has assigned tasks in project " + projectId
                + " and cannot be removed. Reassign the tasks first.");
    }
}
