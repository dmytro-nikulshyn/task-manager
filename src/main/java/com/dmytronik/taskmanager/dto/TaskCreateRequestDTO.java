package com.dmytronik.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskCreateRequestDTO {

    @NotBlank(message = "Title cannot be empty")
    private String title;
    private String description;
    @NotNull(message = "Project cannot be empty")
    private Long projectId;
}
