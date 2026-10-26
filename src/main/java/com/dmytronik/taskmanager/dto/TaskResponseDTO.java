package com.dmytronik.taskmanager.dto;

import com.dmytronik.taskmanager.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TaskResponseDTO {

     private final Long id;
     private final String title;
     private final String description;
     private final TaskStatus status;
     private final LocalDateTime createdAt;
     private final UserSummaryDTO reporter;
     private final UserSummaryDTO assignee;
}
