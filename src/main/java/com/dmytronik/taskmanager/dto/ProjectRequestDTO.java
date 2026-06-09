package com.dmytronik.taskmanager.dto;

import com.dmytronik.taskmanager.model.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectRequestDTO {

    @NotBlank(message = "Title cannot be empty")
    private String title;
    private String description;
    @NotNull(message = "Status cannot be null")
    private ProjectStatus status;
}
