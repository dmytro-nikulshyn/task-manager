package com.dmytronik.taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskNotFoundOrAccessException extends RuntimeException{
    public TaskNotFoundOrAccessException(Long id) {
        super("Task with id " + id + " not found or access denied");
    }
}
