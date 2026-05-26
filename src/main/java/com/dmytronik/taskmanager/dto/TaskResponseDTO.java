package com.dmytronik.taskmanager.dto;

import com.dmytronik.taskmanager.model.Status;

import java.time.LocalDateTime;

public class TaskResponseDTO {

     private final Long id;
     private final String title;
     private final String description;
     private final Status status;
     private final LocalDateTime createdAt;

     public TaskResponseDTO(Long id, String title, String description, Status status, LocalDateTime createdAt) {
          this.id = id;
          this.title = title;
          this.description = description;
          this.status = status;
          this.createdAt = createdAt;
     }

     public Long getId() {
          return id;
     }

     public String getTitle() {
          return title;
     }

     public String getDescription() {
          return description;
     }

     public Status getStatus() {
          return status;
     }

     public LocalDateTime getCreatedAt() {
          return createdAt;
     }
}
