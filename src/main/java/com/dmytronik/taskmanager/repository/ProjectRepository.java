package com.dmytronik.taskmanager.repository;

import com.dmytronik.taskmanager.model.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @EntityGraph(attributePaths = "owner")
    List<Project> findByOwnerId(Long ownerId);

    @EntityGraph(attributePaths = "owner")
    @Override
    List<Project> findAll();

    @EntityGraph(attributePaths = "owner")
    @Override
    Optional<Project> findById(Long id);
}
