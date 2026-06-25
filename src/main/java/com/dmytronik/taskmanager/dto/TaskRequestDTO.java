package com.dmytronik.taskmanager.dto;

import com.dmytronik.taskmanager.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskRequestDTO {

    @NotBlank(message = "Title cannot be empty")
    private String title;
    private String description;
    @NotNull(message = "Status cannot be null")
    private Status status;
    @NotNull(message = "Project cannot be empty")
    private Long projectId;
}
