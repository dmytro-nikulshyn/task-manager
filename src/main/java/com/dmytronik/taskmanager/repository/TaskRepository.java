package com.dmytronik.taskmanager.repository;

import com.dmytronik.taskmanager.model.Task;
import com.dmytronik.taskmanager.model.TaskStatus;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"reporter", "assignee"})
    @Override
    @NonNull
    List<Task> findAll(@NonNull Sort sort);

    @EntityGraph(attributePaths = {"reporter", "assignee"})
    @Override
    @NonNull
    Optional<Task> findById(@NonNull Long id);

    boolean existsByProjectIdAndAssigneeIdAndStatusIn(Long projectId, Long assigneeId, Collection<TaskStatus> statuses);

    @EntityGraph(attributePaths = {"reporter", "assignee"})
    List<Task> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.project.id = :projectId")
    void deleteAllByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);

}
