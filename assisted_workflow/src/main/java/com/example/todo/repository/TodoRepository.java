package com.example.todo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.todo.model.Priority;
import com.example.todo.model.Status;
import com.example.todo.model.Todo;

/**
 * Repository for CRUD and query operations on {@link Todo} entities.
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    /**
     * Finds todos matching the given status.
     *
     * @param status status filter
     * @return todos with the specified status
     */
    List<Todo> findByStatus(Status status);

    /**
     * Finds todos matching the given priority.
     *
     * @param priority priority filter
     * @return todos with the specified priority
     */
    List<Todo> findByPriority(Priority priority);

    /**
     * Returns all todos ordered by priority descending, then creation time descending.
     *
     * @return ordered todo list
     */
    List<Todo> findAllByOrderByPriorityDescCreatedAtDesc();
}
