package com.dmytronik.taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotProjectMemberException extends RuntimeException {
    public NotProjectMemberException(Long userId) {
        super("User " + userId + " is not a member of this project");
    }
}
