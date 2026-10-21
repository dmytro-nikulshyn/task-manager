package com.dmytronik.taskmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectMemberRequestDTO {

    @NotNull(message = "User id is required")
    private Long userId;
}
