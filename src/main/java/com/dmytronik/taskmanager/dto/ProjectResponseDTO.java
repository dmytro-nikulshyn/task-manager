package com.dmytronik.taskmanager.dto;

import com.dmytronik.taskmanager.model.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@AllArgsConstructor
public class ProjectResponseDTO {

    private final Long id;
    private final String title;
    private final String description;
    private final ProjectStatus status;
    private final LocalDateTime createdAt;
    private final UserSummaryDTO owner;
    private final Set<UserSummaryDTO> members;
}
