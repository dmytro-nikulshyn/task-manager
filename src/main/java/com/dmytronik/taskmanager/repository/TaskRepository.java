package com.dmytronik.taskmanager.repository;

import com.dmytronik.taskmanager.model.Task;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"reporter", "assignee"})
    @Override
    @NonNull
    List<Task> findAll();

    @EntityGraph(attributePaths = {"reporter", "assignee"})
    @Override
    @NonNull
    Optional<Task> findById(@NonNull Long id);
}
