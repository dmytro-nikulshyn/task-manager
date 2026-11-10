package com.dmytronik.taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TaskAlreadyCancelledException extends RuntimeException {
    public TaskAlreadyCancelledException(Long taskId) {
        super("Task " + taskId + " is already CANCELLED and can't be updated");
    }
}
