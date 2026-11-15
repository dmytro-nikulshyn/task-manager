package com.dmytronik.taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AssigneeNotFoundException extends RuntimeException {
    public AssigneeNotFoundException(Long assigneeId) {
        super("User " + assigneeId + " is not a member of this project");
    }
}
