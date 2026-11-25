package com.dmytronik.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectCreateRequestDTO {

    @NotBlank(message = "Title cannot be empty")
    private String title;
    private String description;
}
