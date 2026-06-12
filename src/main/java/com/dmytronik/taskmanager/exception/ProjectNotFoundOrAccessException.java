package com.dmytronik.taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProjectNotFoundOrAccessException extends RuntimeException {
    public ProjectNotFoundOrAccessException(Long id) {
        super("Project with id " + id + " not found or access denied");
    }
}
