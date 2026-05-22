package com.dmytronik.taskmanager.repository;

import com.dmytronik.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
