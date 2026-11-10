package com.dmytronik.taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTaskStatusException extends RuntimeException {
    public InvalidTaskStatusException(String message) {
        super(message);
    }
}
