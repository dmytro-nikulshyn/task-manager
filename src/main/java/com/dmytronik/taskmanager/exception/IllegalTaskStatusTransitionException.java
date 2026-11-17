package com.dmytronik.taskmanager.exception;

import com.dmytronik.taskmanager.model.TaskStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IllegalTaskStatusTransitionException extends RuntimeException {
    public IllegalTaskStatusTransitionException(Long taskId, TaskStatus currentStatus, TaskStatus requestedStatus) {
        super("Task " + taskId + " cannot change status from " + currentStatus + " to " + requestedStatus);
    }
}
