package com.dmytronik.taskmanager.dto;

import com.dmytronik.taskmanager.model.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectResponseDTO {

    private Long id;
    private String title;
    private String description;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private UserSummaryDTO owner;
}
