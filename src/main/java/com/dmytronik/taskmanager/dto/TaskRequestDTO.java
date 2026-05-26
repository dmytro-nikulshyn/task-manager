package com.dmytronik.taskmanager.dto;

import com.dmytronik.taskmanager.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TaskRequestDTO {

    @NotBlank(message = "Title cannot be empty")
    private String title;
    private String description;
    @NotNull(message = "Status cannot be null")
    private Status status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
